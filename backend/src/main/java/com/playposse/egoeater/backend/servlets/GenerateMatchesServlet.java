package com.playposse.egoeater.backend.servlets;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.firebase.NotifyNewMatchesFirebaseServerAction;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.IntermediateMatching;
import com.playposse.egoeater.backend.schema.IntermediateUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.MatchesServletLog;
import com.playposse.egoeater.backend.schema.Ranking;
import com.playposse.egoeater.backend.util.ObjectifyWaiter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A {@link HttpServlet} that is triggered by the AppEngine CRON service on a daily basis to generate new
 * matches.
 */
public class GenerateMatchesServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(GenerateMatchesServlet.class.getName());

    private static final int STATUS_OK = 0;
    private static final int STATUS_ERROR = 1;
    private static final String CRON_HTTP_HEADER_PARAMETER = "X-Appengine-Cron";
    private static final int START_RANK = 1;

    private static final int MAX_MATCHES_COUNT = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // Create matches.
        try {
            ObjectifyWaiter waiter = new ObjectifyWaiter();
            cleanTables(waiter);
            populateIntermediateFirstPass(waiter);
            populateIntermediateSecondPass(waiter);
            createMatches(waiter);
            NotifyNewMatchesFirebaseServerAction.notifyNewMatches();
        } catch (Throwable ex) {
            logExecution(req, start, ex.getMessage());
            throw ex;
        }

        // Log execution
        logExecution(req, start, null);
        log.info("*** Done");
    }

    private void logExecution(HttpServletRequest req, long start, @Nullable String errorMessage) {
        log.info("*** Log execution");
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        boolean isRunByCron = isRunByCron(req);
        int status = (errorMessage == null) ? STATUS_OK : STATUS_ERROR;
        MatchesServletLog log = new MatchesServletLog(duration, status, isRunByCron, errorMessage);
        ofy().save().entity(log);
    }

    private static boolean isRunByCron(HttpServletRequest req) {
        String header = req.getHeader(CRON_HTTP_HEADER_PARAMETER);
        return Boolean.TRUE.toString().equalsIgnoreCase(header);
    }

    private static void cleanTables(ObjectifyWaiter waiter) {
        log.info("*** Cleaning tables");
        cleanIntermediateMatches(waiter);
        cleanIntermediateUsers(waiter);
        cleanOldMatches(waiter);
        waiter.flush();
    }

    private static void cleanIntermediateMatches(ObjectifyWaiter waiter) {
        QueryResultIterable<Key<IntermediateMatching>> iterable = ofy().load()
                .type(IntermediateMatching.class)
                .keys()
                .iterable();

        waiter.addResult(
                ofy().delete()
                        .keys(iterable));
        log.info("Done cleaning intermediate matching tables.");
    }

    private static void cleanIntermediateUsers(ObjectifyWaiter waiter) {
        QueryResultIterable<Key<IntermediateUser>> iterable = ofy().load()
                .type(IntermediateUser.class)
                .keys()
                .iterable();

        waiter.addResult(
                ofy().delete()
                        .keys(iterable));
        log.info("Done cleaning intermediate user tables.");
    }

    /**
     * Populates the intermediate table with rows. The rankBack and matchScore are left empty.
     *
     * @param waiter
     */
    private static void populateIntermediateFirstPass(ObjectifyWaiter waiter) {
        log.info("*** First pass");
        QueryResultIterator<Ranking> iterator = ofy().load()
                .type(Ranking.class)
                .order("profileId")
                .order("-winsLossesSum")
                .iterator();

        Ref<EgoEaterUser> profileId = null;
        int rank = START_RANK;
        List<Ref<EgoEaterUser>> lockedProfileRefs = null;
        Map<Long, Integer> profileIdToRankMap = new HashMap<>();
        while (iterator.hasNext()) {
            Ranking ranking = iterator.next();
            Ref<EgoEaterUser> ratedProfileId = ranking.getRatedProfileId();

            // Skip over inactive users.
            if (!isActive(ranking.getProfileId())) {
                log.info("Skipping over user because of inActive: "
                        + ranking.getProfileId().getKey().getId());
                continue;
            }

            // Detect if ranking of the next user is starting.
            if (!ranking.getProfileId().equals(profileId)) {
                if (profileId != null) {
                    saveIntermediateUser(
                            profileId,
                            rank,
                            lockedProfileRefs,
                            profileIdToRankMap,
                            waiter);
                }
                profileId = ranking.getProfileId();
                rank = START_RANK;
                profileIdToRankMap = new HashMap<>();
                lockedProfileRefs = getLockedProfileRefs(profileId);
            } else {
                rank++;
            }

            profileIdToRankMap.put(ratedProfileId.getKey().getId(), rank);

            // TODO: Check for fuck off to avoid creating these entries.
            // TODO: Check if a user is already at the max fixed matches and avoid creating entries.

            // Skip saving for half the records because matches go both ways.
            if (profileId.getKey().getId() > ratedProfileId.getKey().getId()) {
                continue;
            }

            // If we hit a locked match, skip this.
            if (lockedProfileRefs.contains(ratedProfileId)) {
                continue;
            }

            // Create intermediateMatching entry.
            IntermediateMatching intermediateMatching =
                    new IntermediateMatching(profileId, ratedProfileId, rank);
            waiter.addResult(
                    ofy().save()
                            .entity(intermediateMatching));
        }

        if (profileId != null) {
            saveIntermediateUser(profileId, rank, lockedProfileRefs, profileIdToRankMap, waiter);
        }

        waiter.flush();
    }

    private static void saveIntermediateUser(
            Ref<EgoEaterUser> profileId,
            int rank, List<Ref<EgoEaterUser>> lockedProfileRefs,
            Map<Long, Integer> profileIdToRankMap,
            ObjectifyWaiter waiter) {

        log.info("Stored " + rank + " intermediateMatching rows for " + profileId);
        IntermediateUser intermediateUser =
                new IntermediateUser(profileId, profileIdToRankMap, lockedProfileRefs);
        waiter.addResult(
                ofy().save()
                        .entity(intermediateUser));
    }

    /**
     * Returns profiles of locked matches the user already has.
     * <p>
     * <p>This method assumes that all non-fixed matches have already been removed from the Match
     * entity. By avoiding to filter on the isFixed property, we avoid having to have an index on
     * that field and a composite index.
     */
    private static List<Ref<EgoEaterUser>> getLockedProfileRefs(Ref<EgoEaterUser> profileRef) {
        List<Match> lockedMatches = ofy().load()
                .type(Match.class)
                .filter("userARef =", profileRef) // TODO: Test if this condition works.
                .list();

        List<Ref<EgoEaterUser>> lockedProfilesRef = new ArrayList<>();
        for (Match match : lockedMatches) {
            Ref<EgoEaterUser> userBRef = match.getUserBRef();
            lockedProfilesRef.add(userBRef);
        }
        return lockedProfilesRef;
    }

    /**
     * Goes through the intermediate entities again to add the rankBack and sum.
     * <p>
     * <p>The match score formula is: Formula: max + (min / (max + 1))
     *
     * @param waiter
     */
    private static void populateIntermediateSecondPass(ObjectifyWaiter waiter) {
        log.info("*** Second pass");
        QueryResultIterable<IntermediateMatching> iterable =
                ofy().load()
                        .type(IntermediateMatching.class)
                        .order("ratedProfileId")
                        .iterable();

        Ref<EgoEaterUser> ratedProfileId = null;
        IntermediateUser intermediateRatedUser = null;
        log.info("Second pass is a go: " + iterable.iterator().hasNext());
        for (IntermediateMatching intermediateMatching : iterable) {
            long profileIdLong = intermediateMatching.getProfileId().getKey().getId();
            log.info("2nd pass: id: " + intermediateMatching.getId()
                    + " profile id " + profileIdLong);

            // When the next user starts, load that user's information.
            if (!intermediateMatching.getRatedProfileId().equals(ratedProfileId)) {
                log.info("- Looking up rated profile: " + intermediateMatching.getRatedProfileId());
                ratedProfileId = intermediateMatching.getRatedProfileId();
                long ratedProfileIdLong = ratedProfileId.getKey().getId();
                intermediateRatedUser =
                        ofy().load()
                                .type(IntermediateUser.class)
                                .id(ratedProfileIdLong)
                                .now();
                if (intermediateRatedUser == null) {
                    // The rated user hasn't ranked anybody yet.
                    log.info("- Profile " + ratedProfileId + " hasn't ranked anybody yet.");
                    continue;
                }
            } else if (intermediateRatedUser == null) {
                // We couldn't find the intermediateRatedUser in the last loop iteration. So, keep
                // skipping IntermediateMatching rows until a new user shows up.
                log.info("- Skipping row because profile " + ratedProfileId
                        + " has been previously determined not have ranked anybody.");
                continue;
            }

            Integer rankBack = intermediateRatedUser.getProfileIdToRankMap().get(profileIdLong);
            if (rankBack == null) {
                // The rated user hasn't ranked this user back yet.
                log.info("- Profile " + ratedProfileId + " hasn't ranked this user yet.");
                continue;
            }
            intermediateMatching.setRankBack(rankBack);

            int max = Math.max(intermediateMatching.getRank(), intermediateMatching.getRankBack());
            int min = Math.min(intermediateMatching.getRank(), intermediateMatching.getRankBack());
            intermediateMatching.setMatchScore(max + (min / (max + 1.0)));
            waiter.addResult(
                    ofy().save()
                            .entity(intermediateMatching));
            log.info("- Updated second pass.");
        }

        waiter.flush();
    }

    private static void cleanOldMatches(ObjectifyWaiter waiter) {
        QueryResultIterable<Key<Match>> iterable = ofy().load()
                .type(Match.class)
                .filter("isLocked =", false)
                .keys()
                .iterable();

        waiter.addResult(
                ofy().delete()
                        .keys(iterable));
    }

    /**
     * Reads the information from the intermediate table to create the matching.
     *
     * TODO: Implement logic, so that only one match row is created. The profile with the smaller id
     * is always left.
     */
    private static void createMatches(ObjectifyWaiter waiter) {
        log.info("*** Create matches");
        QueryResultIterable<IntermediateMatching> iterable = ofy().load()
                .type(IntermediateMatching.class)
                .order("matchScore")
                .iterable();

        log.info("Create matches is a go: " + iterable.iterator().hasNext());
        for (IntermediateMatching intermediateMatching : iterable) {
            Ref<EgoEaterUser> userARef = intermediateMatching.getProfileId();
            long userALongId = userARef.getKey().getId();
            IntermediateUser userA = ofy().load()
                    .type(IntermediateUser.class)
                    .id(userALongId)
                    .now();
            if ((userA == null) || (userA.getMatchesCount() >= MAX_MATCHES_COUNT)) {
                continue;
            }

            Ref<EgoEaterUser> userBRef = intermediateMatching.getRatedProfileId();
            long userBLongId = userBRef.getKey().getId();
            IntermediateUser userB = ofy().load()
                    .type(IntermediateUser.class)
                    .id(userBLongId)
                    .now();
            if ((userB == null) || (userB.getMatchesCount() >= MAX_MATCHES_COUNT)) {
                continue;
            }

            // Create match.
            waiter.addResult(
                    ofy().save()
                            .entity(new Match(userARef, userBRef)));

            // Create match in the other direction.
            waiter.addResult(
                    ofy().save()
                            .entity(new Match(userBRef, userARef)));

            // Update match counts.
            userA.setMatchesCount(userA.getMatchesCount() + 1);
            waiter.addResult(
                    ofy().save()
                            .entity(userA));
            userB.setMatchesCount(userB.getMatchesCount() + 1);
            waiter.addResult(
                    ofy().save()
                            .entity(userB));
        }
    }

    private static boolean isActive(Ref<EgoEaterUser> profileId) {
        EgoEaterUser user = ofy().load().key(profileId.getKey()).now();
        return user.isActive();
    }
}
