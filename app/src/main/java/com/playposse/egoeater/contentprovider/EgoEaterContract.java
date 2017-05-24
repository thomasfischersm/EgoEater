package com.playposse.egoeater.contentprovider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.playposse.egoeater.util.CollectionsUtil;

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

        public static final String ID_COLUMN = _ID;
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

        public static final String ID_COLUMN = _ID;
        public static final String PROFILE_ID_COLUMN = "profile_id";
        public static final String FIRST_NAME_COLUMN = "first_name";
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

        public static final String ID_COLUMN = _ID;
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

        public static final String ID_COLUMN = _ID;
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

    public static final class PipelineLogTable implements BaseColumns {

        public static final String PATH = "pipelineLog";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "PIPELINE_LOG";
        public static final String ID_COLUMN = _ID;
        public static final String CREATED_COLUMN = "created";
        public static final String DURATION_MS_COLUMN = "duration_ms";
        public static final String TRIGGER_REASON_COLUMN = "trigger_reason";

        public static final int NO_MORE_PAIRING_ACTIVITY_TRIGGER = 1;
        public static final int RATING_ACTIVITY_TRIGGER = 2;
        public static final int SIGN_IN_TRIGGER = 3;
        public static final int LOCATION_UPDATE_TRIGGER = 4;

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                CREATED_COLUMN,
                DURATION_MS_COLUMN,
                TRIGGER_REASON_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE PIPELINE_LOG "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + "DURATION_MS INTEGER, "
                        + "TRIGGER_REASON INTEGER)";
    }

    /**
     * A table that holds all the matches.
     */
    public static final class MatchTable implements BaseColumns {

        public static final String PATH = "match";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "MATCH";
        public static final String ID_COLUMN = _ID;
        public static final String MATCH_ID_COLUMN = "match_id";
        public static final String CREATED_COLUMN = "created";
        public static final String PROFILE_ID_COLUMN = "profile_id";
        public static final String IS_LOCKED_COLUMN = "is_locked";
        public static final String HAS_NEW_MESSAGE = "has_new_message";
        public static final String UNREAD_MESSAGES_COUNT = "unread_messages_count";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                MATCH_ID_COLUMN,
                CREATED_COLUMN,
                PROFILE_ID_COLUMN,
                IS_LOCKED_COLUMN,
                HAS_NEW_MESSAGE,
                UNREAD_MESSAGES_COUNT};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE MATCH "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "MATCH_ID INTEGER, "
                        + "CREATED DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + "PROFILE_ID INTEGER,"
                        + "IS_LOCKED BOOLEAN,"
                        + "HAS_NEW_MESSAGE BOOLEAN DEFAULT FALSE,"
                        + "UNREAD_MESSAGES_COUNT DEFAULT 0)";
    }

    /**
     * A table that holds all the messages. Each message of a conversation has its own row. This
     * makes it easy to show the conversation in a RecyclerView. The storage approach on the
     * server is different.
     */
    public static final class MessageTable implements BaseColumns {

        public static final String PATH = "message";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "MESSAGE";
        public static final String ID_COLUMN = _ID;
        public static final String SENDER_PROFILE_ID_COLUMN = "sender_profile_id";
        public static final String RECIPIENT_PROFILE_ID_COLUMN = "recipient_profile_id";
        public static final String MESSAGE_INDEX_COLUMN = "message_index";
        public static final String IS_RECEIVED_COLUMN = "is_received";
        public static final String CREATED_COLUMN = "created";
        public static final String PREVIOUS_MESSAGE_CREATED_COLUMN = "previous_message_created";
        public static final String MESSAGE_CONTENT_COLUMN = "message_content";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                SENDER_PROFILE_ID_COLUMN,
                RECIPIENT_PROFILE_ID_COLUMN,
                MESSAGE_INDEX_COLUMN,
                IS_RECEIVED_COLUMN,
                CREATED_COLUMN,
                PREVIOUS_MESSAGE_CREATED_COLUMN,
                MESSAGE_CONTENT_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE MESSAGE "
                        + "(_ID INTEGER PRIMARY KEY, "
                        + "SENDER_PROFILE_ID INTEGER, "
                        + "RECIPIENT_PROFILE_ID INTEGER, "
                        + "MESSAGE_INDEX INTEGER, "
                        + "IS_RECEIVED BOOLEAN DEFAULT FALSE, "
                        + "CREATED DATETIME, "
                        + "PREVIOUS_MESSAGE_CREATED, "
                        + "MESSAGE_CONTENT TEXT)";
    }

    public static final class DeleteDuplicateProfiles {

        public static final String PATH = "deleteDuplicateProfiles";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        static final String SQL = "delete from " + ProfileTable.TABLE_NAME
                + " where rowid not in (select max(rowid) from " + ProfileTable.TABLE_NAME
                + " group by " + ProfileTable.PROFILE_ID_COLUMN + ");";
    }

    public static final class MatchAndProfileQuery {

        public static final String PATH = "matchAndProfile";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String[] COLUMN_NAMES =
                CollectionsUtil.concatenate(MatchTable.COLUMN_NAMES, ProfileTable.COLUMN_NAMES);

        public static final String SQL = String.format(
                "select * from %1$s a " +
                        "inner join %2$s b on a.profile_id=b.profile_id " +
                        "order by a.created asc",
                MatchTable.TABLE_NAME,
                ProfileTable.TABLE_NAME);
    }

    /**
     * A query that returns the maximum message index for a conversation between two users.
     */
    public static final class MaxMessageIndexQuery {

        public static final String PATH = "maxMessageIndex";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String MAX_MESSAGE_INDEX_COLUMN = "max_message_index";
        public static final String[] COLUMN_NAMES = new String[]{
                MAX_MESSAGE_INDEX_COLUMN};

        public static final String SQL =
                "select max(message_index) as max_message_index " +
                        "from message " +
                        "where ((sender_profile_id = ?) and (recipient_profile_id = ?)) " +
                        "or ((sender_profile_id = ?) and (recipient_profile_id = ?))";
    }
}
