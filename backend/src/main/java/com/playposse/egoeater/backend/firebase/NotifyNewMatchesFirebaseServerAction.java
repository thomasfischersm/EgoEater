package com.playposse.egoeater.backend.firebase;

import org.json.JSONObject;

import java.io.IOException;

/**
 * A Firebase server action to notify all devices that the mission data has been updated.
 */

public class NotifyNewMatchesFirebaseServerAction extends FirebaseServerAction {

    private static final String DATA_TYPE = "notifyNewMatches";
    private static final String COLLAPSE_KEY =
            "notifyNewMatchesCollapseKey";

    public static String notifyNewMatches() throws IOException {
        JSONObject rootNode = new JSONObject();
        rootNode.put(TYPE_KEY, DATA_TYPE);

        return sendMessageToAllDevices(
                rootNode,
                FirebasePriority.normal,
                COLLAPSE_KEY);
    }
}
