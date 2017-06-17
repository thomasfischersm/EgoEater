package com.playposse.egoeater.activity;

import android.app.Activity;
import android.support.annotation.Nullable;

/**
 * Static place to remember what the current activity is. Android doesn't support knowing the
 * current activity. So, this place keeps track of the current activity.
 * <p>
 * <p>This was specifically created to know if a notification makes sense of if a user is already
 * on the right activity. E.g., when a user receives a message and is already on the messaging
 * activity, it doesn't make sense to show a notification.
 */
public final class CurrentActivity {

    private static Class<? extends Activity> currentActivity;
    private static Long messagingPartnerId;

    private CurrentActivity() {
    }

    @Nullable
    public static Class<? extends Activity> getCurrentActivity() {
        return currentActivity;
    }

    static void setCurrentActivity(Class<? extends Activity> currentActivity) {
        CurrentActivity.currentActivity = currentActivity;
    }

    static void clearActivity() {
        currentActivity = null;
        messagingPartnerId = null;
    }

    public static Long getMessagingPartnerId() {
        return messagingPartnerId;
    }

    static void setMessagingPartnerId(Long messagingPartnerId) {
        CurrentActivity.messagingPartnerId = messagingPartnerId;
    }
}
