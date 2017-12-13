package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.AdminStatisticsBean;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.Rating;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action, that is only accessible to admins, which returns statistics about the app users.
 */
public class GetAdminStatisticsServerAction extends AbstractServerAction {

    public static AdminStatisticsBean getAdminStatistics(long sessionId)
            throws BadRequestException {

        long start = System.currentTimeMillis();

        // Ensure that the user is and admin.
        loadAdmin(sessionId);

        // Load the counts.
        int totalUserCount =
                ofy().load()
                        .type(EgoEaterUser.class)
                        .count();
        int activeUserCount =
                ofy().load()
                        .type(EgoEaterUser.class)
                        .filter("isActive =", true)
                        .count();
        int conversationCount =
                ofy().load()
                        .type(Conversation.class)
                        .count();
        int matchesCount =
                ofy().load()
                        .type(Match.class)
                        .count();
        int ratingsCount =
                ofy().load()
                        .type(Rating.class)
                        .count();

        long end = System.currentTimeMillis();
        return new AdminStatisticsBean(
                totalUserCount,
                activeUserCount,
                conversationCount,
                matchesCount,
                ratingsCount,
                end - start);
    }
}
