package com.playposse.egoeater.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class that accesses the SQLLite instance.
 */
public class MainDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "egoEaterDb";
    private static final int DB_VERSION = 1;

    /**
     * Stores profile ids of users near the current user.
     */
    private static final String SQL_CREATE_PROFILE_ID_TABLE =
            "CREATE TABLE PROFILE_ID "
                    + "(_ID INTEGER PRIMARY KEY, "
                    + "PROFILE_ID INTEGER, "
                    + "IS_PROFILE_LOADED BOOLEAN DEFAULT FALSE)";

    /**
     * Caches profile information about users.
     */
    private static final String SQL_CREATE_PROFILE_TABLE =
            "CREATE TABLE PROFILE "
                    + "(_ID INTEGER PRIMARY KEY, "
                    + "PROFILE_ID INTEGER, "
                    + "FIRST_NAME TEXT, "
                    + "LAST_NAME TEXT, "
                    + "PROFILE_TEXT TEXT, "
                    + "DISTANCE INTEGER, "
                    + "CITY TEXT, "
                    + "STATE TEXT, "
                    + "COUNTRY TEXT, "
                    + "AGE INTEGER, "
                    + "GENDER TEXT, "
                    + "PHOTO_URL_1 TEXT, "
                    + "PHOTO_URL_2 TEXT, "
                    + "PHOTO_URL_3 TEXT, "
                    + "WINS INTEGER DEFAULT 0, "
                    + "LOSSES INTEGER DEFAULT 0)";

    /**
     * Stores the outcome of the user's choice between two profiles.
     */
    private static final String SQL_CREATE_RATING_TABLE =
            "CREATE TABLE RATING "
                    + "(_ID INTEGER PRIMARY KEY, "
                    + "WINNER_ID INTEGER, "
                    + "LOSER_ID INTEGER, "
                    + "CREATED DATETIME, "
                    + "IS_SYNCHED_TO_CLOUD BOOLEAN)";

    /**
     * Stores a pipeline of profiles to compare.
     */
    private static final String SQL_CREATE_PIPELINE_TABLE =
            "CREATE TABLE PIPELINE "
            + "(_ID INTEGER PRIMARY KEY, "
                    + "PROFILE_0_ID INTEGER, "
                    + "PROFILE_1_ID INTEGER, "
                    + "ARE_PHOTOS_CACHED BOOLEAN DEFAULT FALSE)";

    public static final String PROFILE_ID_TABLE = "PROFILE_ID";
    public static final String PROFILE_TABLE = "PROFILE";
    public static final String RATING_TABLE = "RATING";
    public static final String PIPELINE_TABLE = "PIPELINE";

    public MainDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILE_ID_TABLE);
        db.execSQL(SQL_CREATE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_RATING_TABLE);
        db.execSQL(SQL_CREATE_PIPELINE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
