package com.playposse.egoeater;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.playposse.egoeater.activity.CropPhotoActivity;
import com.playposse.egoeater.activity.EditProfileActivity;
import com.playposse.egoeater.activity.LoginActivity;
import com.playposse.egoeater.activity.RatingActivity;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.StringUtil;

/**
 * A global class that routes the user to the right {@link android.app.Activity} when key events
 * happen.
 */
public class GlobalRouting {

    private static final String LOG_TAG = GlobalRouting.class.getSimpleName();

    /**
     * Routes the user when the app starts.
     * <p>
     * <ul>
     * <li>If the user has never logged in, go to the {@link LoginActivity}.</li>
     * <li>If the user hasn't picked a profile photo yet, go to the
     * {@link CropPhotoActivity}.</li>
     * <li>If the user has a picked a profile photo, go to the ranking activity.</li>
     * </ul>
     */
    public static void onStartup(Context context) {
        if (EgoEaterPreferences.getSessionId(context) == null) {
            context.startActivity(new Intent(context, LoginActivity.class));
        } else {
            onLoginComplete(context);
        }
    }

    /**
     * Routes the user when login has completed.
     */
    public static void onLoginComplete(Context context) {
        Log.i(LOG_TAG, "onLoginComplete: GlobalRouting.onLoginComplete has been called.");
        if (!EgoEaterPreferences.hasFirstProfilePhoto(context)) {
            context.startActivity(new Intent(context, CropPhotoActivity.class));
        } else if (StringUtil.isEmpty(EgoEaterPreferences.getProfileText(context))) {
            context.startActivity(new Intent(context, EditProfileActivity.class));
        } else {
            context.startActivity(new Intent(context, RatingActivity.class));
        }
    }

    /**
     * Routes the user when an error with a call to the cloud happens. This will reset everything.
     */
    public static void onCloudError(Context context) {
        EgoEaterPreferences.clearSessionId(context);
    }
}
