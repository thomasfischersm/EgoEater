package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.beans.MessageBean;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Message;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that returns the conversation between two users.
 */
public class GetConversationServerAction extends AbstractServerAction {

    public static List<MessageBean> getConversation(long sessionId, long otherUserId)
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
            return null;
        }

        // Convert Objectify entities to transport beans.
        Conversation conversation = conversations.get(0);
        List<MessageBean> messageBeans = new ArrayList<>(conversation.getMessages().size());
        for (Message message : conversation.getMessages()) {
            messageBeans.add(new MessageBean(message));
        }

        return messageBeans;
    }
}
