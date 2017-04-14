package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.EgoEaterEndPoint;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Ranking;
import com.playposse.egoeater.backend.schema.Rating;

import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that reports a ranking.
 */
public class ReportRankingServerAction extends AbstractServerAction {

    private static final Logger log = Logger.getLogger(ReportRankingServerAction.class.getName());

    public static void reportRanking(long sessionId, long winnerId, long loserId)
            throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Store rating.
        Rating rating = new Rating(egoEaterUser.getId(), winnerId, loserId);
        ofy().save().entity(rating);

        // Update winner ranking.
        Ranking winnerRanking = getRanking(egoEaterUser.getId(), winnerId);
        winnerRanking.registerWin();
        ofy().save().entity(winnerRanking);

        // Update loser ranking.
        Ranking loserRanking = getRanking(egoEaterUser.getId(), loserId);
        loserRanking.registerLoss();
        ofy().save().entity(loserRanking);
    }

    private static Ranking getRanking(long profileId, long rateProfileId) {
        List<Ranking> rankings = ofy().load()
                .type(Ranking.class)
                .filter("profileId =", profileId)
                .filter("ratedProfileId =", rateProfileId)
                .list();

        if (rankings.size() == 0) {
            return new Ranking(profileId, rateProfileId);
        } else if (rankings.size() == 1) {
            return rankings.get(0);
        } else {
            log.severe("Got duplicate rankings for " + profileId + " and " + rateProfileId);
            return rankings.get(0);
        }
    }
}
