package com.playposse.egoeater.backend.firebase;

import org.json.JSONObject;

import java.io.IOException;

/**
 * A Firebase server action that notifies the other party of a conversation that his/her message
 * has been read.
 */
public class NotifyMessageReadFirebaseServerAction extends FirebaseServerAction {

    private static final String DATA_TYPE = "notifyMessageRead";

    private static final String RECIPIENT_PROFILE_ID_KEY = "recipientProfileId";
    private static final String MESSAGE_INDEX_KEY = "messageIndex";

    public static String notifyMessageRead(
            long recipientProfileId,
            String senderFirebaseToken,
            int messageIndex) throws IOException {

        // Build payload.
        JSONObject rootNode = new JSONObject();
        rootNode.put(TYPE_KEY, DATA_TYPE);
        rootNode.put(RECIPIENT_PROFILE_ID_KEY, recipientProfileId);
        rootNode.put(MESSAGE_INDEX_KEY, messageIndex);

        return sendMessageToDevice(
                senderFirebaseToken,
                rootNode,
                FirebasePriority.high);
    }
}
