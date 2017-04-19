package com.playposse.egoeater.backend.generatematches;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
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
        QueryResultIterable<IntermediateMatching> idIterable =
                ofy().load()
                        .type(IntermediateMatching.class)
                        .iterable();

        ofy().delete()
                .type(IntermediateMatching.class)
                .ids(idIterable)
                .now();
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
                profileIdToRankMap.put(ratedProfileId.getKey().getId(), rank);
            } else {
                rank++;
            }

            // TODO: Check for fuck off to avoid creating these entries.
            // TODO: Check if a user is already at the max fixed matches and avoid creating entries.

            // Create intermediateMatching entry.
            IntermediateMatching intermediateMatching =
                    new IntermediateMatching(profileId, ratedProfileId, rank);
            ofy().save().entity(intermediateMatching);
        }

        ofy().flush();
    }

    /**
     * Goes through the intermediate entities again to add the rankBack and sum.
     *
     * <p>The match score formula is: Formula: max + (min / (max + 1))
     */
    private static void populateIntermediateSecondPass() {
        QueryResultIterator<IntermediateMatching> iterator =
                ofy().load()
                        .type(IntermediateMatching.class)
                        .order("ratedProfileId")
                        .iterator();

        Ref<EgoEaterUser> ratedProfileId = null;
        IntermediateUser intermediateRatedUser = null;
        while (iterator.hasNext()) {
            IntermediateMatching intermediateMatching = iterator.next();
            long profileIdLong = intermediateMatching.getProfileId().getKey().getId();

            // When the next user starts, load that user's information.
            if (!intermediateMatching.getProfileId().equals(ratedProfileId)) {
                ratedProfileId = intermediateMatching.getRatedProfileId();
                long ratedProfileIdLong = ratedProfileId.getKey().getId();
                intermediateRatedUser =
                        ofy().load()
                                .type(IntermediateUser.class)
                                .id(ratedProfileIdLong)
                                .now();
            }

            Integer rankBack = intermediateRatedUser.getProfileIdToRankMap().get(profileIdLong);
            intermediateMatching.setRankBack(rankBack);

            int max = Math.max(intermediateMatching.getRank(), intermediateMatching.getRankBack());
            int min = Math.min(intermediateMatching.getRank(), intermediateMatching.getRankBack());
            intermediateMatching.setMatchScore(max + (min / (max + 1.0)));
            ofy().save().entity(intermediateMatching);
        }

        ofy().flush();
    }

    private static void cleanOldMatches() {
        QueryKeys<Match> keys = ofy().load()
                .type(Match.class)
                .filter("isLocked =", true)
                .keys();

        ofy().delete()
                .type(Match.class)
                .ids(keys)
                .now();
    }

    /**
     * Reads the information from the intermediate table to create the matching.
     */
    private static void createMatches() {
        QueryResultIterator<IntermediateMatching> iterator = ofy().load()
                .type(IntermediateMatching.class)
                .order("matchScore")
                .iterator();

        while (iterator.hasNext()) {
            IntermediateMatching intermediateMatching = iterator.next();

            Ref<EgoEaterUser> userARef = intermediateMatching.getProfileId();
            long userALongId = userARef.getKey().getId();
            IntermediateUser userA = ofy().load()
                    .type(IntermediateUser.class)
                    .id(userALongId)
                    .now();
            if (userA.getMatchesCount() >= MAX_MATCHES_COUNT) {
                continue;
            }

            Ref<EgoEaterUser> userBRef = intermediateMatching.getRatedProfileId();
            long userBLongId = userBRef.getKey().getId();
            IntermediateUser userB = ofy().load()
                    .type(IntermediateUser.class)
                    .id(userBLongId)
                    .now();
            if (userB.getMatchesCount() >= MAX_MATCHES_COUNT) {
                continue;
            }

            // Create match.
            Match match = new Match(userARef, userBRef);
            ofy().save()
                    .entity(match);

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
