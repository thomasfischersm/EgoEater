package com.playposse.egoeater.util;

import android.app.Activity;
import android.content.Intent;

import com.playposse.egoeater.activity.LoginActivity;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A class that contains a shared method to log out.
 */
public class LogoutUtil {

    public static void logout(Activity activity) {
        EgoEaterPreferences.clearSessionId(activity);
        activity.finish();
        activity.startActivity(new Intent(activity, LoginActivity.class));
    }
}
