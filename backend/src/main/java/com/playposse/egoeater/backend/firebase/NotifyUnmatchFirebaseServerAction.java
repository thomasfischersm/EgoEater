package com.playposse.egoeater.backend.firebase;

import org.json.JSONObject;

import java.io.IOException;

/**
 * A Firebase action that informs the user that he/she has been told to fuck off.
 */
public class NotifyUnmatchFirebaseServerAction extends FirebaseServerAction {

    private static final String DATA_TYPE = "notifyUnmatch";

    private static final String PARTNER_ID_KEY = "partnerId";

    public static String notifyUnmatch(String recipientFirebaseToken, long partnerId)
            throws IOException {

        // Build payload.
        JSONObject rootNode = new JSONObject();
        rootNode.put(TYPE_KEY, DATA_TYPE);
        rootNode.put(PARTNER_ID_KEY, partnerId);

        return sendMessageToDevice(
                recipientFirebaseToken,
                rootNode,
                FirebasePriority.high);
    }
}
