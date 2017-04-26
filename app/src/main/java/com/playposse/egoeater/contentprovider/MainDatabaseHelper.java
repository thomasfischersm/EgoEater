package com.playposse.egoeater.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A helper class that accesses the SQLLite instance.
 */
public class MainDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "egoEaterDb";
    private static final int DB_VERSION = 1;

    public MainDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EgoEaterContract.ProfileIdTable.SQL_CREATE_TABLE);
        db.execSQL(EgoEaterContract.ProfileTable.SQL_CREATE_TABLE);
        db.execSQL(EgoEaterContract.RatingTable.SQL_CREATE_TABLE);
        db.execSQL(EgoEaterContract.PipelineTable.SQL_CREATE_TABLE);
        db.execSQL(EgoEaterContract.PipelineLogTable.SQL_CREATE_TABLE);
        db.execSQL(EgoEaterContract.MatchTable.SQL_CREATE_TABLE);
        db.execSQL(EgoEaterContract.MessageTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
