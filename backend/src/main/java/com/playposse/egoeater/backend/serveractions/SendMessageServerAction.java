package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.firebase.NotifyNewMessageFirebaseServerAction;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Message;
import com.playposse.egoeater.backend.util.RefUtil;

import java.io.IOException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that sends a message between two users.
 */
public class SendMessageServerAction extends AbstractServerAction {

    public static void sendMessage(long sessionId, long recipientId, String message)
            throws BadRequestException, IOException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);
        EgoEaterUser recipientUser = loadUserById(recipientId);
        Ref<EgoEaterUser> senderRef = RefUtil.createUserRef(egoEaterUser);
        Ref<EgoEaterUser>[] userRefs = sortUserRefs(egoEaterUser.getId(), recipientId);

        // Check if conversation exists already.
        List<Conversation> conversations = ofy().load()
                .type(Conversation.class)
                .filter("profileRefA =", userRefs[0])
                .filter("profileRefB =", userRefs[1])
                .list();

        // Persist message.
        final Conversation conversation;
        if (conversations.size() > 0) {
            conversation = createConversation(userRefs[0], userRefs[1], senderRef, message);
        } else {
            conversation = updateConversation(conversations.get(0), senderRef, message);
        }
        ofy().save().entity(conversation);

        // Send Firebase message to tell the recipient about the new message.
        int messageIndex = conversation.getMessages().size() - 1;
        NotifyNewMessageFirebaseServerAction.notifyNewMessage(
                egoEaterUser.getId(),
                recipientUser.getFirebaseToken(),
                messageIndex,
                message);
    }

    private static Conversation createConversation(
            Ref<EgoEaterUser> userRefA,
            Ref<EgoEaterUser> userRefB,
            Ref<EgoEaterUser> senderUserRef,
            String message) {

        return new Conversation(userRefA, userRefB, senderUserRef, message);
    }

    private static Conversation updateConversation(
            Conversation conversation,
            Ref<EgoEaterUser> senderUserRef,
            String messageContent) {

        int index = conversation.getMessages().size();
        Message message = new Message(index, senderUserRef, messageContent);
        conversation.getMessages().add(message);
        ofy().save().entity(conversation);
        return conversation;
    }
}
