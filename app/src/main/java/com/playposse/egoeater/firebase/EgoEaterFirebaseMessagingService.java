package com.playposse.egoeater.firebase;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.firebase.actions.FirebaseClientAction;
import com.playposse.egoeater.firebase.actions.NotifyMessageReadClientAction;
import com.playposse.egoeater.firebase.actions.NotifyNewMatchesClientAction;
import com.playposse.egoeater.firebase.actions.NotifyNewMessageClientAction;
import com.playposse.egoeater.firebase.actions.NotifyProfileUpdatedClientAction;
import com.playposse.egoeater.firebase.actions.NotifyUnmatchClientAction;
import com.playposse.egoeater.util.AnalyticsUtil;

import java.util.Map;

/**
 * An implementation of {@link FirebaseMessagingService} that receives messages from AppEngine. The
 * messages initiate the pairing process of a student and buddy.
 */
public class EgoEaterFirebaseMessagingService extends FirebaseMessagingService {

    private static final String LOG_CAT = EgoEaterFirebaseMessagingService.class.getSimpleName();

    private static final String TYPE_KEY = "type";
    private static final String NOTIFY_NEW_MATCHES_TYPE = "notifyNewMatches";
    private static final String NOTIFY_NEW_MESSAGE_TYPE = "notifyNewMessage";
    private static final String NOTIFY_MESSAGE_READ_DATA_TYPE = "notifyMessageRead";
    private static final String NOTIFY_UNMATCH_DATA_TYPE = "notifyUnmatch";
    private static final String NOTIFY_PROFILE_UPDATED_DATA_TYPE = "notifyProfileUpdated";

    private static final String ALL_DEVICES_TOPIC = "allDevices";
    private static final String PROFILE_UPDATE_TOPIC_PREFIX = "/topics/profile-";


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
            Log.e(LOG_CAT, "onMessageReceived: Received a Firebase message without data!");
            return;
        }

        if (data.get(TYPE_KEY) == null) {
            Log.e(LOG_CAT, "onMessageReceived: Received a Firebase message without a type!");
            return;
        }

        switch (data.get(TYPE_KEY)) {
            case NOTIFY_NEW_MATCHES_TYPE:
                execute(new NotifyNewMatchesClientAction(remoteMessage));
                break;
            case NOTIFY_NEW_MESSAGE_TYPE:
                execute(new NotifyNewMessageClientAction(remoteMessage));
                break;
            case NOTIFY_MESSAGE_READ_DATA_TYPE:
                execute(new NotifyMessageReadClientAction(remoteMessage));
                break;
            case NOTIFY_UNMATCH_DATA_TYPE:
                execute(new NotifyUnmatchClientAction(remoteMessage));
                break;
            case NOTIFY_PROFILE_UPDATED_DATA_TYPE:
                execute(new NotifyProfileUpdatedClientAction(remoteMessage));
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
        AnalyticsUtil.reportFirebaseMessageReceived(
                getApplication(),
                action.getClass().getSimpleName());
    }

    public static void subscribeToTopicsOnAppStart(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                subscribeToAllDevicesTopic();
                subscribeToAllProfileUpdates(context);
            }
        }).start();
    }

    public static void subscribeToAllDevicesTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(ALL_DEVICES_TOPIC);
    }

    public static void subscribeToProfileUpdates(long profileId) {
        String topic = PROFILE_UPDATE_TOPIC_PREFIX + Long.toString(profileId);
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    private static void subscribeToAllProfileUpdates(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                ProfileTable.CONTENT_URI,
                new String[]{ProfileTable.PROFILE_ID_COLUMN},
                null,
                null,
                null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long profileId = cursor.getLong(0);
                    subscribeToProfileUpdates(profileId);
                }
            } finally {
                cursor.close();
            }
        }
    }
}
