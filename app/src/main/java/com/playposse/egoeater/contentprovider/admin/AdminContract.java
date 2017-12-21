package com.playposse.egoeater.contentprovider.admin;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A contract for {@link AdminContentProvider}.
 */
public class AdminContract {

    public static final String AUTHORITY = "com.playposse.egoeater.provider.admin";

    private static final String CONTENT_SCHEME = "content";

    private AdminContract() {
    }

    private static Uri createContentUri(String path) {
        return new Uri.Builder()
                .scheme(CONTENT_SCHEME)
                .encodedAuthority(AUTHORITY)
                .appendPath(path)
                .build();
    }

    /**
     * Stores users
     */
    public static class EgoEaterUserTable implements BaseColumns {

        public static final String PATH = "egoEaterUser";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "ego_eater_user";

        public static final String ID_COLUMN = _ID.toUpperCase();
        public static final String EGO_EATER_USER_ID = "ego_eater_user_id";
        public static final String FB_PROFILE_ID_COLUMN = "fb_profile_id";
        public static final String LAST_LOGIN_COLUMN = "last_login";
        public static final String CREATED_COLUMN = "created";
        public static final String IS_ACTIVE_COLUMN = "is_active";
        public static final String FIRST_NAME_COLUMN = "first_name";
        public static final String LAST_NAME_COLUMN = "last_name";
        public static final String EMAIL_COLUMN = "email";
        public static final String PROFILE_TEXT_COLUMN = "profile_text";
        public static final String LATITUDE_COLUMN = "latitude";
        public static final String LONGITUDE_COLUMN = "longitude";
        public static final String CITY_COLUMN = "city";
        public static final String STATE_COLUMN = "state";
        public static final String COUNTRY_COLUMN = "country";
        public static final String BIRTHDAY_COLUMN = "birthday";
        public static final String BIRTHDAY_OVERRIDE_COLUMN = "birthday_override";
        public static final String GENDER_COLUMN = "gender";
        public static final String PROFILE_PHOTO_0_COLUMN = "profile_photo_0";
        public static final String PROFILE_PHOTO_1_COLUMN = "profile_photo_1";
        public static final String PROFILE_PHOTO_2_COLUMN = "profile_photo_2";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                EGO_EATER_USER_ID,
                FB_PROFILE_ID_COLUMN,
                LAST_LOGIN_COLUMN,
                CREATED_COLUMN,
                IS_ACTIVE_COLUMN,
                FIRST_NAME_COLUMN,
                LAST_NAME_COLUMN,
                EMAIL_COLUMN,
                PROFILE_TEXT_COLUMN,
                LATITUDE_COLUMN,
                LONGITUDE_COLUMN,
                CITY_COLUMN,
                STATE_COLUMN,
                COUNTRY_COLUMN,
                BIRTHDAY_COLUMN,
                BIRTHDAY_OVERRIDE_COLUMN,
                GENDER_COLUMN,
                PROFILE_PHOTO_0_COLUMN,
                PROFILE_PHOTO_1_COLUMN,
                PROFILE_PHOTO_2_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE ego_eater_user "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "ego_eater_user_id INTEGER, "
                        + "fb_profile_id TEXT, "
                        + "last_login INTEGER, "
                        + "created TEXT, "
                        + "rule_set INTEGER, "
                        + "is_active BOOLEAN, "
                        + "first_name TEXT, "
                        + "last_name TEXT, "
                        + "email TEXT, "
                        + "profile_text TEXT, "
                        + "latitude NUMERIC, "
                        + "longitude NUMERIC, "
                        + "city TEXT, "
                        + "state TEXT, "
                        + "country TEXT, "
                        + "birthday TEXT, "
                        + "birthday_override TEXT, "
                        + "gender TEXT, "
                        + "profile_photo_0 TEXT, "
                        + "profile_photo_1 TEXT, "
                        + "profile_photo_2 TEXT)";
    }


    /**
     * Stores messages
     */
    public static class MessageTable implements BaseColumns {

        public static final String PATH = "message";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "message";

        public static final String ID_COLUMN = _ID.toUpperCase();
        public static final String CONVERSATION_ID = "conversation_id";
        public static final String SENDER_PROFILE_ID_COLUMN = "sender_profile_id";
        public static final String RECIPIENT_PROFILE_ID_COLUMN = "recipient_profile_id";
        public static final String MESSAGE_INDEX_COLUMN = "message_index";
        public static final String MESSAGE_CONTENT_COLUMN = "message_content";
        public static final String RECEIVED_COLUMN = "received";
        public static final String CREATED_COLUMN = "created";


        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                CONVERSATION_ID,
                SENDER_PROFILE_ID_COLUMN,
                RECIPIENT_PROFILE_ID_COLUMN,
                MESSAGE_INDEX_COLUMN,
                MESSAGE_CONTENT_COLUMN,
                RECEIVED_COLUMN,
                CREATED_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE message "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "conversation_id INTEGER, "
                        + "sender_profile_id INTEGER, "
                        + "recipient_profile_id INTEGER, "
                        + "message_index INTEGER, "
                        + "message_content TEXT, "
                        + "received BOOLEAN, "
                        + "created INTEGER, "
                        + "FOREIGN KEY(sender_profile_id) REFERENCES ego_eater_user(ego_eater_user_id),"
                        + "FOREIGN KEY(recipient_profile_id) REFERENCES ego_eater_user(ego_eater_user_id))";
    }
}
