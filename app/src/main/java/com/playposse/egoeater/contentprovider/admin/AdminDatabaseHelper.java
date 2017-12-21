package com.playposse.egoeater.contentprovider.admin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.playposse.egoeater.contentprovider.admin.AdminContract.EgoEaterUserTable;
import com.playposse.egoeater.contentprovider.admin.AdminContract.MessageTable;

/**
 * A helper class that manages the SQLLite database.
 */
public class AdminDatabaseHelper extends SQLiteOpenHelper {

    private  static final String DB_NAME = "egoEaterAdmin";

    private static final int DB_VERSION = 1;

    public AdminDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EgoEaterUserTable.SQL_CREATE_TABLE);
        db.execSQL(MessageTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to upgrade. This is the first version.
    }
}
