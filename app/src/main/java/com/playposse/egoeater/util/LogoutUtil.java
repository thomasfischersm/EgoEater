package com.playposse.egoeater.util;

import android.app.Activity;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A class that contains a shared method to log out.
 */
public class LogoutUtil {

    public static void logout(Activity activity) {
        EgoEaterPreferences.clearSessionId(activity);

        GlobalRouting.onLogout(activity);
    }
}
