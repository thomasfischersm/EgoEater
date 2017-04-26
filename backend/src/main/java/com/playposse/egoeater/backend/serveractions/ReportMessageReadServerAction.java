package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that reports that the recipient of a message has looked at it.
 */
public class ReportMessageReadServerAction extends AbstractServerAction {

    private static final Logger log = Logger.getLogger(ReportMessageReadServerAction.class.getName());

    public static void reportMessageRead(long sessionId, long otherUserId, int messageIndex)
            throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);
        Ref<EgoEaterUser>[] userRefs = sortUserRefs(egoEaterUser.getId(), otherUserId);

        // Check if conversation exists already.
        List<Conversation> conversations = ofy().load()
                .type(Conversation.class)
                .filter("profileRefA =", userRefs[0])
                .filter("profileRefB =", userRefs[1])
                .list();

        if ((conversations == null) || (conversations.size() == 0)) {
            log.severe("reportMessageRead silently failed because no conversation between "
                    + egoEaterUser.getId() + " and " + otherUserId + " exist.");
            return;
        }

        Conversation conversation = conversations.get(0);
        if ((messageIndex < 0) || (messageIndex >= conversation.getMessages().size())) {
            log.severe("reportMessageRead silently failed because conversation between "
                    + egoEaterUser.getId() + " and " + otherUserId + " doesn't have messageIndex "
                    + messageIndex + ".");
        }

        // Mark message as read.
        conversation.getMessages().get(messageIndex).setReceived(true);
        ofy().save()
                .entity(conversation);
    }
}
