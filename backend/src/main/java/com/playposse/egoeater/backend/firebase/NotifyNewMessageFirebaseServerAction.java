package com.playposse.egoeater.backend.firebase;

import org.json.JSONObject;

import java.io.IOException;

/**
 * A Firebase message that tells a user that someone has sent him/her a message. If the message is
 * small enough, it is included in the Firebase message itself.
 */
public class NotifyNewMessageFirebaseServerAction extends FirebaseServerAction {

    private static final String DATA_TYPE = "notifyNewMessage";

    private static final String SENDER_PROFILE_ID_KEY = "senderProfileId";
    private static final String IS_MESSAGE_INCLUDED_KEY = "isMessageIncluded";
    private static final String MESSAGE_INDEX_KEY = "messageIndex";
    private static final String MESSAGE_CONTENT_KEY = "messageContent";

    public static String notifyNewMessage(
            long senderProfileId,
            String recipientFirebaseToken,
            int messageIndex,
            String message) throws IOException {

        // Check message size.
        boolean isMessageIncluded = message.length() < MAX_FIREBASE_MESSAGE_SIZE;
        message = isMessageIncluded ? message : null;

        // Build payload.
        JSONObject rootNode = new JSONObject();
        rootNode.put(TYPE_KEY, DATA_TYPE);
        rootNode.put(SENDER_PROFILE_ID_KEY, senderProfileId);
        rootNode.put(IS_MESSAGE_INCLUDED_KEY, isMessageIncluded);
        rootNode.put(MESSAGE_INDEX_KEY, messageIndex);
        rootNode.put(MESSAGE_CONTENT_KEY, message);

        return sendMessageToDevice(
                recipientFirebaseToken,
                rootNode,
                FirebasePriority.high);
    }
}
