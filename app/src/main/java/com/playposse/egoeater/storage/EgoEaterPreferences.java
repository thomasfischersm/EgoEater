package com.playposse.egoeater.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class that makes application preferences accessible.
 */
public final class EgoEaterPreferences {

    private static final String LOG_CAT = EgoEaterPreferences.class.getSimpleName();

    public static final String PREFS_NAME = "EgoEaterPreferences";

    private static final String USER_ID_KEY = "userId";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String NAME_KEY = "name";
    private static final String FIREBASE_TOKEN_KEY = "firebaseToken";

    private static final String NULL_STRING = "-1";
    public static final int NULL_VALUE = -1;

    public static void setUser(Context context, UserBean userBean) {
        setLong(context, USER_ID_KEY, userBean.getUserId());
        setLong(context, SESSION_ID_KEY, userBean.getSessionId());
        setString(context, FIRST_NAME_KEY, userBean.getFirstName());
        setString(context, LAST_NAME_KEY, userBean.getFirstName());
        setString(context, NAME_KEY, userBean.getFirstName());
    }

    public static UserBean getUser(Context context) {
        return new UserBean()
                .setUserId(getLong(context, USER_ID_KEY))
                .setSessionId(getLong(context, SESSION_ID_KEY))
                .setFirstName(getString(context, FIRST_NAME_KEY))
                .setLastName(getString(context, LAST_NAME_KEY))
                .setName(getString(context, NAME_KEY));
    }

    public static Long getSessionId(Context context) {
        return getLong(context, SESSION_ID_KEY);
    }

    public static void setFirebaseToken(Context context, String firebaseToken) {
        setString(context, FIREBASE_TOKEN_KEY, firebaseToken);
    }

    public static String getFirebaseToken(Context context) {
        return getString(context, FIREBASE_TOKEN_KEY);
    }

    private static String getString(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String str = sharedPreferences.getString(key, NULL_STRING);
        return (!NULL_STRING.equals(str)) ? str : null;
    }

    private static void setString(Context context, String key, String value) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (value != null) {
            sharedPreferences.edit().putString(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    private static Integer getInt(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Integer value = sharedPreferences.getInt(key, -1);
        return (value != -1) ? value : null;
    }

    private static void setInt(Context context, String key, Integer value) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (value != null) {
            sharedPreferences.edit().putInt(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    private static boolean getBoolean(Context context, String key, boolean defaultValue) {
        try {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, defaultValue);
        } catch (ClassCastException ex) {
            setBoolean(context, key, defaultValue);
            return false;
        }
    }

    private static void setBoolean(Context context, String key, boolean value) {
        Log.i(LOG_CAT, "Setting preference boolean for key " + key + " to " + value);
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putBoolean(key, value)
                .commit();
    }

    private static Set<Long> getLongSet(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(key, null);

        if ((set == null) || (set.size() == 0)) {
            return new HashSet<>();
        }

        Set<Long> result = new HashSet<>(set.size());
        for (String value : set) {
            result.add(Long.valueOf(value));
        }
        return result;
    }

    private static void setLongSet(Context context, String key, Set<Long> set) {
        Set<String> stringSet = new HashSet<>(set.size());
        for (Long value : set) {
            stringSet.add(value.toString());
        }

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet(key, stringSet).commit();
    }

    private static void addValueToLongSet(Context context, String key, Long value) {
        Set<Long> set = getLongSet(context, key);
        set.add(value);
        setLongSet(context, key, set);
    }

    private static Set<String> getStringSet(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> stringSet = sharedPreferences.getStringSet(key, null);
        if (stringSet != null) {
            return stringSet;
        } else {
            return new HashSet<>();
        }
    }

    private static void setStringSet(Context context, String key, Set<String> set) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet(key, set).commit();
    }

    private static void addValueToStringSet(Context context, String key, String value) {
        Set<String> set = getStringSet(context, key);
        set.add(value);
        setStringSet(context, key, set);
    }

    private static Long getLong(Context context, String key) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Long value = sharedPreferences.getLong(key, NULL_VALUE);
        return (value != NULL_VALUE) ? value : null;
    }

    private static void setLong(Context context, String key, Long value) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (value != null) {
            sharedPreferences.edit().putLong(key, value).commit();
        } else {
            sharedPreferences.edit().remove(key).commit();
        }
    }
}

