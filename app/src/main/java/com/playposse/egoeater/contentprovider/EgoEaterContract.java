package com.playposse.egoeater.contentprovider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A contract class for the {@link EgoEaterContentProvider}.
 */
public class EgoEaterContract {

    public static final String AUTHORITY = "com.playposse.egoeater.provider";

    private static final String CONTENT_SCHEME = "content";

    /**
     * Stores profile ids of users near the current user.
     */
    public static final class ProfileIdTable implements BaseColumns {

        public static final String PATH = "profileIds";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "PROFILE_ID";

        public static final String ID_COLUMN = "_id";
        public static final String PROFILE_ID_COLUMN = "profile_id";
        public static final String IS_PROFILE_LOADED_COLUMN = "is_profile_loaded";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                PROFILE_ID_COLUMN,
                IS_PROFILE_LOADED_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE PROFILE_ID "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "PROFILE_ID INTEGER, "
                        + "IS_PROFILE_LOADED BOOLEAN DEFAULT FALSE)";
    }

    /**
     * Caches profile information about users.
     */
    public static final class ProfileTable implements BaseColumns {

        public static final String PATH = "profiles";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "PROFILE";

        public static final String ID_COLUMN = "_id";
        public static final String PROFILE_ID_COLUMN = "profile_id";
        public static final String FIRST_NAME_COLUMN = "first_name";
        public static final String LAST_NAME_COLUMN = "last_name";
        public static final String NAME_COLUMN = "name";
        public static final String PROFILE_TEXT_COLUMN = "profile_text";
        public static final String DISTANCE_COLUMN = "distance";
        public static final String CITY_COLUMN = "city";
        public static final String STATE_COLUMN = "state";
        public static final String COUNTRY_COLUMN = "country";
        public static final String AGE_COLUMN = "age";
        public static final String GENDER_COLUMN = "gender";
        public static final String PHOTO_URL_0_COLUMN = "photo_url_0";
        public static final String PHOTO_URL_1_COLUMN = "photo_url_1";
        public static final String PHOTO_URL_2_COLUMN = "photo_url_2";
        public static final String WINS_COLUMN = "wins";
        public static final String LOSSES_COLUMN = "losses";
        public static final String WINS_LOSSES_SUM_COLUMN = "wins_losses_sum";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                PROFILE_ID_COLUMN,
                FIRST_NAME_COLUMN,
                LAST_NAME_COLUMN,
                NAME_COLUMN,
                PROFILE_TEXT_COLUMN,
                DISTANCE_COLUMN,
                CITY_COLUMN,
                STATE_COLUMN,
                COUNTRY_COLUMN,
                AGE_COLUMN,
                GENDER_COLUMN,
                PHOTO_URL_0_COLUMN,
                PHOTO_URL_1_COLUMN,
                PHOTO_URL_2_COLUMN,
                WINS_COLUMN,
                LOSSES_COLUMN,
                WINS_LOSSES_SUM_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE PROFILE "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "PROFILE_ID INTEGER, "
                        + "FIRST_NAME TEXT, "
                        + "LAST_NAME TEXT, "
                        + "NAME TEXT, "
                        + "PROFILE_TEXT TEXT, "
                        + "DISTANCE INTEGER, "
                        + "CITY TEXT, "
                        + "STATE TEXT, "
                        + "COUNTRY TEXT, "
                        + "AGE INTEGER, "
                        + "GENDER TEXT, "
                        + "PHOTO_URL_0 TEXT, "
                        + "PHOTO_URL_1 TEXT, "
                        + "PHOTO_URL_2 TEXT, "
                        + "WINS INTEGER DEFAULT 0, "
                        + "LOSSES INTEGER DEFAULT 0,"
                        + "WINS_LOSSES_SUM INTEGER DEFAULT 0)";
    }

    private static Uri createContentUri(String path) {
        return new Uri.Builder()
                .scheme(CONTENT_SCHEME)
                .encodedAuthority(AUTHORITY)
                .appendPath(path)
                .build();
    }

    /**
     * Stores the outcome of the user's choice between two profiles.
     */
    public static final class RatingTable implements BaseColumns {

        public static final String PATH = "ratings";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "RATING";

        public static final String ID_COLUMN = "_id";
        public static final String WINNER_ID_COLUMN = "winner_id";
        public static final String LOSER_ID_COLUMN = "loser_id";
        public static final String CREATED_COLUMN = "created";
        public static final String IS_SYNCED_TO_CLOUD_COLUMN = "is_synced_to_cloud";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                WINNER_ID_COLUMN,
                LOSER_ID_COLUMN,
                CREATED_COLUMN,
                IS_SYNCED_TO_CLOUD_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE RATING "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "WINNER_ID INTEGER, "
                        + "LOSER_ID INTEGER, "
                        + "CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + "IS_SYNCED_TO_CLOUD BOOLEAN DEFAULT FALSE)";
    }

    public static final class PipelineTable implements BaseColumns {

        public static final String PATH = "pipeline";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "PIPELINE";

        public static final String ID_COLUMN = "_id";
        public static final String PROFILE_0_ID_COLUMN = "profile_0_id";
        public static final String PROFILE_1_ID_COLUMN = "profile_1_id";
        public static final String ARE_PHOTOS_CACHED_COLUMN = "are_photos_cached";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                PROFILE_0_ID_COLUMN,
                PROFILE_1_ID_COLUMN,
                ARE_PHOTOS_CACHED_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE PIPELINE "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "PROFILE_0_ID INTEGER, "
                        + "PROFILE_1_ID INTEGER, "
                        + "ARE_PHOTOS_CACHED BOOLEAN DEFAULT FALSE)";
    }

    public static final class DeleteDuplicateProfiles {

        public static final String PATH = "deleteDuplicateProfiles";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        static final String SQL = "delete from " + ProfileTable.TABLE_NAME
                + " where rowid not in (select max(rowid) from " + ProfileTable.TABLE_NAME
                + " group by " + ProfileTable.PROFILE_ID_COLUMN + ");";
    }
}
