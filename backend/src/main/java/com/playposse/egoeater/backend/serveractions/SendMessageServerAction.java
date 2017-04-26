package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.firebase.NotifyNewMessageFirebaseServerAction;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.Message;
import com.playposse.egoeater.backend.util.RefUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that sends a message between two users.
 */
public class SendMessageServerAction extends AbstractServerAction {

    private static final Logger log = Logger.getLogger(SendMessageServerAction.class.getName());

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
            lockMatch(userRefs[0], userRefs[1]);
            lockMatch(userRefs[1], userRefs[0]); // TODO: Should only need one lockMatch in the future.
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
            Ref<EgoEaterUser> userARef,
            Ref<EgoEaterUser> userBRef,
            Ref<EgoEaterUser> senderUserRef,
            String message) {

        return new Conversation(userARef, userBRef, senderUserRef, message);
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

    private static void lockMatch(Ref<EgoEaterUser> userARef, Ref<EgoEaterUser> userBRef) {
        List<Match> matches = ofy().load()
                .type(Match.class)
                .filter("userARef =", userARef)
                .filter("userBRef =", userBRef)
                .list();

        if (matches.size() != 1) {
            log.warning("Got an unexpected amount of matches for " + userARef.getKey().getId()
                    + " and " + userBRef.getKey().getId() + ": " + matches.size());
            return;
        }

        Match match = matches.get(0);
        match.setLocked(true);
        ofy().save()
                .entity(match);
    }
}
