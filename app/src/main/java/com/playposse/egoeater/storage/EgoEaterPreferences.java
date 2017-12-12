package com.playposse.egoeater.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.data.profilewizard.ProfileUserData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class that makes application preferences accessible.
 */
public final class EgoEaterPreferences {

    private static final String LOG_CAT = EgoEaterPreferences.class.getSimpleName();

    private static final String PREFS_NAME = "EgoEaterPreferences";

    private static final String USER_ID_KEY = "userId";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String FB_PROFILE_ID_KEY = "fbProfileId";
    private static final String IS_ACTIVE_KEY = "isActive";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String NAME_KEY = "name";
    private static final String FIREBASE_TOKEN_KEY = "firebaseToken";
    private static final String PROFILE_PHOTO_URL_KEY = "profilePhotoUrl";
    private static final String HAS_FIRST_PROFILE_PHOTO_KEY = "hasFirstProfilePhoto";
    private static final String HAS_SEEN_INTRO_DECK_KEY = "hasSeenIntroDeck";
    private static final String PROFILE_TEXT_KEY = "profileText";
    private static final String LONGITUDE_KEY = "longitude";
    private static final String LATITUDE_KEY = "latitude";
    private static final String CITY_KEY = "city";
    private static final String STATE_KEY = "state";
    private static final String COUNTRY_KEY = "country";
    private static final String BIRTHDAY_KEY = "birthday";
    private static final String GENDER_KEY = "gender";
    private static final String QUERY_RADIUS_KEY = "queryRadius";
    private static final String FUCK_OFF_USERS_KEY = "fuckOffUsers";
    private static final String PISSED_OFF_USERS_KEY = "pissedOffUsers";
    private static final String PROFILE_BUILDER_LAST_USER_DATA = "profileBuilderLastUserData";
    private static final String PROFILE_BUILDER_LAST_PROFILE_TEXT = "profileBuilderlastProfileText";
    private static final String HAS_SEEN_COMPARISON_INFO_KEY = "hasSeenComparisonInfo";
    private static final String HAS_FIRST_PROFILE_BEEN_SELECTED_KEY = "hasFirstProfileBeenSelected";

    private static final boolean IS_ACTIVE_DEFAULT_VALUE = true;
    private static final boolean HAS_FIRST_PROFILE_PHOTO_DEFAULT_VALUE = false;
    private static final boolean HAS_SEEN_INTRO_DECK_DEFAULT_VALUE = false;
    private static final boolean HAS_SEEN_COMPARISON_INFO_DEFAULT_VALUE = false;
    private static final boolean HAS_FIRST_PROFILE_BEEN_SELECTED_KEY_DEFAULT_VALUE = false;
    private static final int MAX_PROFILE_PHOTO_COUNT = 3;
    private static final int QUERY_RADIUS_DEFAULT = 0;

    private static final String NULL_STRING = "-1";
    private static final int NULL_VALUE = -1;

    public static void setUser(Context context, UserBean userBean) {
        setLong(context, USER_ID_KEY, userBean.getUserId());
        setLong(context, SESSION_ID_KEY, userBean.getSessionId());
        setString(context, FB_PROFILE_ID_KEY, userBean.getFbProfileId());
        setBoolean(context, IS_ACTIVE_KEY, userBean.getActive());
        setString(context, FIRST_NAME_KEY, userBean.getFirstName());
        setString(context, LAST_NAME_KEY, userBean.getFirstName());
        setString(context, NAME_KEY, userBean.getFirstName());
        setString(context, PROFILE_TEXT_KEY, userBean.getProfileText());
        setDouble(context, LONGITUDE_KEY, userBean.getLongitude());
        setDouble(context, LATITUDE_KEY, userBean.getLatitude());
        setString(context, CITY_KEY, userBean.getCity());
        setString(context, STATE_KEY, userBean.getState());
        setString(context, COUNTRY_KEY, userBean.getCountry());
        setString(context, BIRTHDAY_KEY, userBean.getBirthday());
        setString(context, GENDER_KEY, userBean.getGender());

        boolean hasAtLeastOneProfilePhoto = false;
        List<String> photoUrls = userBean.getProfilePhotoUrls();
        if (photoUrls != null) {
            for (int i = 0; i < MAX_PROFILE_PHOTO_COUNT; i++) {
                if (i < photoUrls.size()) {
                    setString(context, PROFILE_PHOTO_URL_KEY + i, photoUrls.get(i));
                    hasAtLeastOneProfilePhoto = true;
                } else {
                    setString(context, PROFILE_PHOTO_URL_KEY + i, null);
                }
            }
        }

        if (hasAtLeastOneProfilePhoto) {
            setHasFirstProfilePhoto(context, true);
        }
    }

    public static UserBean getUser(Context context) {
        UserBean userBean = new UserBean()
                .setUserId(getLong(context, USER_ID_KEY))
                .setSessionId(getLong(context, SESSION_ID_KEY))
                .setFbProfileId(getString(context, FB_PROFILE_ID_KEY))
                .setActive(isActive(context))
                .setFirstName(getString(context, FIRST_NAME_KEY))
                .setLastName(getString(context, LAST_NAME_KEY))
                .setName(getString(context, NAME_KEY))
                .setProfileText(getString(context, PROFILE_TEXT_KEY))
                .setLongitude(getDouble(context, LONGITUDE_KEY))
                .setLatitude(getDouble(context, LATITUDE_KEY))
                .setCity(getString(context, CITY_KEY))
                .setState(getString(context, STATE_KEY))
                .setCountry(getString(context, COUNTRY_KEY))
                .setBirthday(getString(context, BIRTHDAY_KEY))
                .setGender(getString(context, GENDER_KEY));

        ArrayList<String> profilePhotoUrls = new ArrayList<>();
        userBean.setProfilePhotoUrls(profilePhotoUrls);
        for (int i = 0; i < MAX_PROFILE_PHOTO_COUNT; i++) {
            String photoUrl = getString(context, PROFILE_PHOTO_URL_KEY + i);
            if (photoUrl != null) {
                profilePhotoUrls.add(photoUrl);
            } else {
                break;
            }
        }

        return userBean;
    }

    public static void clearSessionId(Context context) {
        setLong(context, SESSION_ID_KEY, null);
    }

    public static Long getSessionId(Context context) {
        return getLong(context, SESSION_ID_KEY);
    }

    public static String getFbProfileId(Context context) {
        return getString(context, FB_PROFILE_ID_KEY);
    }

    public static void setFirebaseToken(Context context, String firebaseToken) {
        setString(context, FIREBASE_TOKEN_KEY, firebaseToken);
    }

    public static String getFirebaseToken(Context context) {
        return getString(context, FIREBASE_TOKEN_KEY);
    }

    public static void setActive(Context context, boolean isActive) {
        setBoolean(context, IS_ACTIVE_KEY, isActive);
    }

    public static boolean isActive(Context context) {
        return getBoolean(context, IS_ACTIVE_KEY, IS_ACTIVE_DEFAULT_VALUE);
    }

    public static String getProfilePhotoUrl0(Context context) {
        return getString(context, PROFILE_PHOTO_URL_KEY + 0);
    }

    public static void setProfilePhotoUrl0(Context context, String photoUrl) {
        setString(context, PROFILE_PHOTO_URL_KEY + 0, photoUrl);
    }

    public static String getProfilePhotoUrl1(Context context) {
        return getString(context, PROFILE_PHOTO_URL_KEY + 1);
    }

    public static void setProfilePhotoUrl1(Context context, String photoUrl) {
        setString(context, PROFILE_PHOTO_URL_KEY + 1, photoUrl);
    }

    public static String getProfilePhotoUrl2(Context context) {
        return getString(context, PROFILE_PHOTO_URL_KEY + 2);
    }

    public static void setProfilePhotoUrl2(Context context, String photoUrl) {
        setString(context, PROFILE_PHOTO_URL_KEY + 2, photoUrl);
    }

    public static void setHasFirstProfilePhoto(Context context, boolean hasFirstProfilePhoto) {
        setBoolean(context, HAS_FIRST_PROFILE_PHOTO_KEY, hasFirstProfilePhoto);
    }

    public static boolean hasFirstProfilePhoto(Context context) {
        return getBoolean(
                context,
                HAS_FIRST_PROFILE_PHOTO_KEY,
                HAS_FIRST_PROFILE_PHOTO_DEFAULT_VALUE);
    }

    public static String getProfileText(Context context) {
        return getString(context, PROFILE_TEXT_KEY);
    }

    public static void setHasSeenIntroDeck(Context context, boolean hasSeenIntroDeck) {
        setBoolean(context, HAS_SEEN_INTRO_DECK_KEY, hasSeenIntroDeck);
    }

    public static boolean hasSeenIntroDeck(Context context) {
        return getBoolean(
                context,
                HAS_SEEN_INTRO_DECK_KEY,
                HAS_SEEN_INTRO_DECK_DEFAULT_VALUE);
    }

    public static Double getLongitude(Context context) {
        return getDouble(context, LONGITUDE_KEY);
    }

    public static Double getLatitude(Context context) {
        return getDouble(context, LATITUDE_KEY);
    }

    public static int getQueryRadius(Context context) {
        Integer queryRadius = getInt(context, QUERY_RADIUS_KEY);
        return (queryRadius != null) ? queryRadius : QUERY_RADIUS_DEFAULT;
    }

    public static void setQueryRadius(Context context, int queryRadius) {
        setInt(context, QUERY_RADIUS_KEY, queryRadius);
    }

    public static Set<Long> getFuckedOffUsers(Context context) {
        return getLongSet(context, FUCK_OFF_USERS_KEY);
    }

    public static void addFuckOffUser(Context context, long profileId) {
        addValueToLongSet(context, FUCK_OFF_USERS_KEY, profileId);
    }

    public static Set<Long> getPissedOffUsers(Context context) {
        return getLongSet(context, PISSED_OFF_USERS_KEY);
    }

    public static void addPissedOffUser(Context context, long profileId) {
        addValueToLongSet(context, PISSED_OFF_USERS_KEY, profileId);
    }

    public static ProfileUserData getProfileBuilderLastUserData(Context context)
            throws JSONException {

        String json = getString(context, PROFILE_BUILDER_LAST_USER_DATA);

        if (json == null) {
            return new ProfileUserData();
        }

        return ProfileUserData.read(json);
    }

    public static void setProfileBuilderLastUserData(
            Context context,
            ProfileUserData profileUserData) throws JSONException {

        setString(context, PROFILE_BUILDER_LAST_USER_DATA, profileUserData.toJson());
    }

    public static String getProfileBuilderLastProfileText(Context context) {
        return getString(context, PROFILE_BUILDER_LAST_PROFILE_TEXT);
    }

    public static void setProfileBuilderLastProfileText(Context context, String profileText) {
        setString(context, PROFILE_BUILDER_LAST_PROFILE_TEXT, profileText);
    }

    public static boolean hasSeenComparisonInfo(Context context) {
        return getBoolean(
                context,
                HAS_SEEN_COMPARISON_INFO_KEY,
                HAS_SEEN_COMPARISON_INFO_DEFAULT_VALUE);
    }

    public static void setHasSeenComparisonInfo(Context context, boolean hasSeenComparisonInfo) {
        setBoolean(context, HAS_SEEN_COMPARISON_INFO_KEY, hasSeenComparisonInfo);
    }

    public static boolean hasFirstProfileBeenSelected(Context context) {
        return getBoolean(
                context,
                HAS_FIRST_PROFILE_BEEN_SELECTED_KEY,
                HAS_FIRST_PROFILE_BEEN_SELECTED_KEY_DEFAULT_VALUE);
    }

    public static void setFirstProfileBeenSelected(
            Context context,
            boolean hasFirstProfileBeenSelected) {

        setBoolean(context, HAS_FIRST_PROFILE_BEEN_SELECTED_KEY, hasFirstProfileBeenSelected);
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

    private static Double getDouble(Context context, String key) {
        String str = getString(context, key);
        return (str != null) ? Double.parseDouble(str) : null;
    }

    private static void setDouble(Context context, String key, Double value) {
        if (value != null) {
            setString(context, key, value.toString());
        } else {
            setString(context, key, null);
        }
    }

    /**
     * Throws away all the local preference data.
     */
    public static void reset(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .clear()
                .commit();
    }
}

