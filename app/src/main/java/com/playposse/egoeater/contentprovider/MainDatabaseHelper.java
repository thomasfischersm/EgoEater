package com.playposse.egoeater.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MessageTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.RatingTable;

/**
 * A helper class that accesses the SQLLite instance.
 */
public class MainDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "egoEaterDb";
    private static final int ORIGINAL_DB_VERSION = 1;
    private static final int ADD_PROFILE_ACTIVE_VERSION = 2;
    private static final int DB_VERSION = 2;

    public MainDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ProfileIdTable.SQL_CREATE_TABLE);
        db.execSQL(ProfileTable.SQL_CREATE_TABLE);
        db.execSQL(RatingTable.SQL_CREATE_TABLE);
        db.execSQL(PipelineTable.SQL_CREATE_TABLE);
        db.execSQL(PipelineLogTable.SQL_CREATE_TABLE);
        db.execSQL(MatchTable.SQL_CREATE_TABLE);
        db.execSQL(MessageTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ((oldVersion < ADD_PROFILE_ACTIVE_VERSION)) {
            db.execSQL("ALTER TABLE " + ProfileTable.TABLE_NAME
                    + " ADD COLUMN " + ProfileTable.IS_ACTIVE_COLUMN + " boolean;");
        }
    }
}
