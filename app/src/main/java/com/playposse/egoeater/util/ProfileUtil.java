package com.playposse.egoeater.util;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A utility for common profile operations.
 */
public final class ProfileUtil {

    private ProfileUtil() {}

    public static boolean isReady(Context context) {
        return (EgoEaterPreferences.getProfilePhotoUrl0(context) != null)
                && !StringUtil.isEmpty(EgoEaterPreferences.getProfileText(context));
    }

    public static boolean isAgeMissing(Context context) {
        UserBean user = EgoEaterPreferences.getUser(context);
        int age = DataMunchUtil.getAge(user);
        return age == DataMunchUtil.NULL_AGE;
    }
}
