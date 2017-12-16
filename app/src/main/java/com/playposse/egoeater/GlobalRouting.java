package com.playposse.egoeater;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.playposse.egoeater.activity.CropPhotoActivity;
import com.playposse.egoeater.activity.IntroductionActivity;
import com.playposse.egoeater.activity.LoginActivity;
import com.playposse.egoeater.activity.MatchesActivity;
import com.playposse.egoeater.activity.RatingActivity;
import com.playposse.egoeater.activity.ReactivateAccountActivity;
import com.playposse.egoeater.activity.ViewOwnProfileActivity;
import com.playposse.egoeater.activity.specialcase.MissingAgeActivity;
import com.playposse.egoeater.activity.specialcase.NoLocationActivity;
import com.playposse.egoeater.activity.specialcase.ProfileNotReadyActivity;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.ProfileUtil;
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
        if ((EgoEaterPreferences.getSessionId(context) == null)
                || (EgoEaterPreferences.getUser(context) == null)) {
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
        if (!EgoEaterPreferences.hasSeenIntroDeck(context)) {
            context.startActivity(new Intent(context, IntroductionActivity.class));
        } else if (QueryUtil.hasMatches(context.getContentResolver())) {
            context.startActivity(new Intent(context, MatchesActivity.class));
        } else if (!EgoEaterPreferences.hasFirstProfilePhoto(context)) {
            context.startActivity(new Intent(context, CropPhotoActivity.class));
        } else if (StringUtil.isEmpty(EgoEaterPreferences.getProfileText(context))) {
            context.startActivity(new Intent(context, ViewOwnProfileActivity.class));
        } else {
            onStartComparing(context);
        }
    }

    /**
     * Routes the user when an error with a call to the cloud happens. This will reset everything.
     */
    public static void onCloudError(Context context) {
        EgoEaterPreferences.clearSessionId(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Routes the user to the login activity when the session has expired or is missing. The login
     * activity is started with a special intent parameter to show the user a dialog to explain the
     * session expiration.
     */
    public static void onSessionExpired(Context context) {
        ExtraConstants.startLoginActivityWithSessionExpirationDialog(context);
    }

    public static void onNetworkAvailable(Context context) {
        onStartup(context);
    }

    public static void onLogout(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void onRequiresAccountReactivation(Context context) {
        Intent intent = new Intent(context, ReactivateAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Starts the comparing activity. This is an important hook point for checks to be made that
     * should potentially stop a user from being able to compare profiles.
     */
    public static void onStartComparing(Context context) {
        if (!ProfileUtil.isReady(context)) {
            // Check that bio and profile photo is there.
            context.startActivity(new Intent(context, ProfileNotReadyActivity.class));
            AnalyticsUtil.reportUserBlockedForIncompleteProfile(context);
        } else if (ProfileUtil.isAgeMissing(context)) {
            // Check that age is there.
            context.startActivity(new Intent(context, MissingAgeActivity.class));
            AnalyticsUtil.reportUserBlockedForMissingBirthday(context);
        } else if ((EgoEaterPreferences.getLatitude(context) == null)
                || (EgoEaterPreferences.getLatitude(context) == null)
                || StringUtil.isEmpty(EgoEaterPreferences.getCountry(context))) {
            // Check that location is there.
            context.startActivity(new Intent(context, NoLocationActivity.class));
            AnalyticsUtil.reportUserBlockedForMissingLocation(context);
        } else {
            // Go ahead and send the user to compare profiles.
            context.startActivity(new Intent(context, RatingActivity.class));
        }
    }
}
