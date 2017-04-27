package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Message;

import java.util.List;

/**
 * A server action that returns the maximum message index. This is a quick check for the device to
 * see if it has the latest messages.
 */
public class GetMaxMessageIndexServerAction extends AbstractServerAction {

    public static int getMaxMessageIndex(long sessionId, long partnerProfileId)
            throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Get max message index.
        Conversation conversation =
                getConversationByProfileIds(egoEaterUser.getId(), partnerProfileId);

        if (conversation != null) {
            List<Message> messages = conversation.getMessages();
            return messages.get(messages.size() - 1).getIndex();
        } else {
            return -1;
        }
    }
}
