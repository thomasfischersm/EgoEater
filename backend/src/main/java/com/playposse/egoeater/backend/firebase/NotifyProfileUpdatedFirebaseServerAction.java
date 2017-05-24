package com.playposse.egoeater.backend.firebase;

import org.json.JSONObject;

import java.io.IOException;

/**
 * A Firebase message that a profile has been updated.
 */
public class NotifyProfileUpdatedFirebaseServerAction extends FirebaseServerAction {

    private static final String PROFILE_UPDATE_TOPIC_PREFIX = "/topics/profile-";

    private static final String DATA_TYPE = "notifyProfileUpdated";

    private static final String PROFILE_ID_KEY = "senderProfileId";

    public static String notifyProfileUpdated(long profileId) throws IOException {

        // Build payload.
        JSONObject rootNode = new JSONObject();
        rootNode.put(TYPE_KEY, DATA_TYPE);
        rootNode.put(PROFILE_ID_KEY, profileId);

        String topic = PROFILE_UPDATE_TOPIC_PREFIX + profileId;
        return sendMessageToDevice(topic, rootNode, FirebasePriority.normal);
    }
}
