package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Rating;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that reports a ranking.
 */
public class ReportRankingServerAction extends AbstractServerAction {

    public static void reportRanking(long sessionId, long winnerId, long loserId)
            throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Store rating.
        Rating rating = new Rating(egoEaterUser.getId(), winnerId, loserId);
        ofy().save().entity(rating);
    }
}
