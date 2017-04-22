package com.playposse.egoeater.firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.firebase.actions.FirebaseClientAction;
import com.playposse.egoeater.firebase.actions.NotifyNewMatchesClientAction;

import java.util.Map;

/**
 * An implementation of {@link FirebaseMessagingService} that receives messages from AppEngine. The
 * messages initiate the pairing process of a student and buddy.
 */
public class EgoEaterFirebaseMessagingService extends FirebaseMessagingService {

    private static final String LOG_CAT = EgoEaterFirebaseMessagingService.class.getSimpleName();

    private static final String TYPE_KEY = "type";
    private static final String NOTIFY_NEW_MATCHES_TYPE = "notifyNewMatches";

    public static final String ALL_DEVICES_TOPIC = "allDevices";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(LOG_CAT, "EgoEaterFirebaseMessagingService.onCreate is called");

        FirebaseMessaging.getInstance().subscribeToTopic(ALL_DEVICES_TOPIC);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(LOG_CAT, "Received Firebase message: " + remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        Log.i(LOG_CAT, "" + data);

        if (data == null) {
            // Ignore. This might be a test message from the Firebase console.
            return;
        }

        switch (data.get(TYPE_KEY)) {
            case NOTIFY_NEW_MATCHES_TYPE:
                execute(new NotifyNewMatchesClientAction(remoteMessage));
                break;
            default:
                Log.w(LOG_CAT, "Received an unknown message type from Firebase: "
                        + data.get(TYPE_KEY));
        }
    }

    private void execute(FirebaseClientAction action) {
        action.execute(this);
        Log.i(LOG_CAT, "Executed Firebase action " + action.getClass().getSimpleName());

        // Report action to Analytics
//        AnalyticsUtil.reportEvent(
//                getApplication(),
//                firebaseEvent,
//                action.getClass().getSimpleName());
    }
}
