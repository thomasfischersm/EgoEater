package com.playposse.egoeater.backend.generatematches;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.QueryKeys;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.IntermediateMatching;
import com.playposse.egoeater.backend.schema.IntermediateUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.MatchesServletLog;
import com.playposse.egoeater.backend.schema.Ranking;

import java.io.IOException;
import java.util.HashMap;
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
            cleanIntermediate();
            populateIntermediateFirstPass();
            populateIntermediateSecondPass();
            cleanOldMatches();
            createMatches();
        } catch (Throwable ex) {
            logExecution(req, start, ex.getMessage());
            throw ex;
        }

        // Log execution
        logExecution(req, start, null);
    }

    private void logExecution(HttpServletRequest req, long start, @Nullable String errorMessage) {
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        boolean isRunByCron = isRunByCron(req);
        int status = (errorMessage == null) ? STATUS_OK : STATUS_ERROR;
        MatchesServletLog log = new MatchesServletLog(duration, status, isRunByCron, errorMessage);
        ofy().save().entity(log).now();
    }

    private static boolean isRunByCron(HttpServletRequest req) {
        String header = req.getHeader(CRON_HTTP_HEADER_PARAMETER);
        return Boolean.TRUE.toString().equalsIgnoreCase(header);
    }

    private static void cleanIntermediate() {
        cleanIntermediateMatches();
        cleanIntermediateUsers();
        ofy().flush();
    }

    private static void cleanIntermediateMatches() {
        QueryResultIterable<Key<IntermediateMatching>> iterable = ofy().load()
                .type(IntermediateMatching.class)
                .keys()
                .iterable();

        ofy().delete()
                .keys(iterable);
        log.info("Done cleaning intermediate matching tables.");
    }

    private static void cleanIntermediateUsers() {
        QueryResultIterable<Key<IntermediateUser>> iterable = ofy().load()
                .type(IntermediateUser.class)
                .keys()
                .iterable();

        ofy().delete()
                .keys(iterable);
        log.info("Done cleaning intermediate user tables.");
    }

    /**
     * Populates the intermediate table with rows. The rankBack and matchScore are left empty.
     */
    private static void populateIntermediateFirstPass() {
        QueryResultIterator<Ranking> iterator = ofy().load()
                .type(Ranking.class)
                .order("profileId")
                .order("-winsLossesSum")
                .iterator();

        Ref<EgoEaterUser> profileId = null;
        int rank = START_RANK;
        Map<Long, Integer> profileIdToRankMap = new HashMap<>();
        while (iterator.hasNext()) {
            Ranking ranking = iterator.next();
            Ref<EgoEaterUser> ratedProfileId = ranking.getRatedProfileId();

            // Detect if ranking of the next user is starting.
            if (!ranking.getProfileId().equals(profileId)) {
                if (profileId != null) {
                    log.info("Stored " + rank + " intermediateMatching rows for " + profileId);
                }
                if (profileId != null) {
                    IntermediateUser intermediateUser =
                            new IntermediateUser(profileId, 0, profileIdToRankMap);
                    ofy().save().entity(intermediateUser);
                }
                profileId = ranking.getProfileId();
                rank = START_RANK;
                profileIdToRankMap = new HashMap<>();
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

            // Create intermediateMatching entry.
            IntermediateMatching intermediateMatching =
                    new IntermediateMatching(profileId, ratedProfileId, rank);
            ofy().save().entity(intermediateMatching);
        }

        if (profileId != null) {
            IntermediateUser intermediateUser =
                    new IntermediateUser(profileId, 0, profileIdToRankMap);
            ofy().save().entity(intermediateUser);
        }

        ofy().flush();
    }

    /**
     * Goes through the intermediate entities again to add the rankBack and sum.
     * <p>
     * <p>The match score formula is: Formula: max + (min / (max + 1))
     */
    private static void populateIntermediateSecondPass() {
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
                    log.info("Profile " + ratedProfileId + " hasn't ranked anybody yet.");
                    continue;
                }
            }

            Integer rankBack = intermediateRatedUser.getProfileIdToRankMap().get(profileIdLong);
            if (rankBack == null) {
                // The rated user hasn't ranked this user back yet.
                log.info("Profile " + ratedProfileId + " hasn't ranked this user yet.");
                continue;
            }
            intermediateMatching.setRankBack(rankBack);

            int max = Math.max(intermediateMatching.getRank(), intermediateMatching.getRankBack());
            int min = Math.min(intermediateMatching.getRank(), intermediateMatching.getRankBack());
            intermediateMatching.setMatchScore(max + (min / (max + 1.0)));
            ofy().save().entity(intermediateMatching);
        }

        ofy().flush();
    }

    private static void cleanOldMatches() {
        QueryResultIterable<Key<Match>> iterable = ofy().load()
                .type(Match.class)
                .filter("isLocked =", false)
                .keys()
                .iterable();

        ofy().delete()
                .type(Match.class)
                .ids(iterable)
                .now();
    }

    /**
     * Reads the information from the intermediate table to create the matching.
     */
    private static void createMatches() {
        QueryResultIterable<IntermediateMatching> iterable = ofy().load()
                .type(IntermediateMatching.class)
                .order("matchScore")
                .iterable();

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
            ofy().save()
                    .entity(new Match(userARef, userBRef));

            // Create match in the other direction.
            ofy().save()
                    .entity(new Match(userBRef, userARef));

            // Update match counts.
            userA.setMatchesCount(userA.getMatchesCount() + 1);
            ofy().save()
                    .entity(userA);
            userB.setMatchesCount(userB.getMatchesCount() + 1);
            ofy().save()
                    .entity(userB);
        }
    }
}
