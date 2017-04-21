package com.playposse.egoeater.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.RatingTable;

/**
 * A {@link ContentProvider} to cache data locally in the app.
 */
public class EgoEaterContentProvider extends ContentProvider {

    private static final String LOG_TAG = EgoEaterContentProvider.class.getSimpleName();

    private static final int PROFILE_ID_TABLE_KEY = 1;
    private static final int PROFILE_TABLE_KEY = 2;
    private static final int RATING_TABLE_KEY = 3;
    private static final int PIPELINE_TABLE_KEY = 4;
    private static final int DELETE_DUPLICATE_PROFILES_KEY = 5;
    private static final int PIPELINE_LOG_TABLE_KEY = 6;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, ProfileIdTable.PATH, PROFILE_ID_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, ProfileTable.PATH, PROFILE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, RatingTable.PATH, RATING_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, PipelineTable.PATH, PIPELINE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.DeleteDuplicateProfiles.PATH, DELETE_DUPLICATE_PROFILES_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, PipelineLogTable.PATH, PIPELINE_LOG_TABLE_KEY);
    }

    private MainDatabaseHelper mainDatabaseHelper;

    @Override
    public boolean onCreate() {
        mainDatabaseHelper = new MainDatabaseHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {

        SQLiteDatabase database = mainDatabaseHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case PROFILE_ID_TABLE_KEY:
                return database.query(
                        ProfileIdTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case PROFILE_TABLE_KEY:
                return database.query(
                        ProfileTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case RATING_TABLE_KEY:
                return database.query(
                        RatingTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case PIPELINE_TABLE_KEY:
                return database.query(
                        PipelineTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = mainDatabaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case PROFILE_ID_TABLE_KEY:
                long id = database.insert(ProfileIdTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(ProfileIdTable.CONTENT_URI, id);
            case PROFILE_TABLE_KEY:
                long profileId = database.insert(ProfileTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(ProfileTable.CONTENT_URI, profileId);
            case RATING_TABLE_KEY:
                long ratingId = database.insert(RatingTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(RatingTable.CONTENT_URI, ratingId);
            case PIPELINE_TABLE_KEY:
                long pipelineId = database.insert(PipelineTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(PipelineTable.CONTENT_URI, null);
                return ContentUris.withAppendedId(PipelineTable.CONTENT_URI, pipelineId);
            case PIPELINE_LOG_TABLE_KEY:
                long pipelineLogId = database.insert(PipelineLogTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(PipelineLogTable.CONTENT_URI, pipelineLogId);
        }

        return null;
    }

    @Override
    public int delete(
            @NonNull Uri uri,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mainDatabaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case PROFILE_ID_TABLE_KEY:
                return database.delete(ProfileIdTable.TABLE_NAME, selection, selectionArgs);
            case PROFILE_TABLE_KEY:
                return database.delete(ProfileTable.TABLE_NAME, selection, selectionArgs);
            case RATING_TABLE_KEY:
                return database.delete(RatingTable.TABLE_NAME, selection, selectionArgs);
            case PIPELINE_TABLE_KEY:
                return database.delete(PipelineTable.TABLE_NAME, selection, selectionArgs);
            case DELETE_DUPLICATE_PROFILES_KEY:
                Log.i(LOG_TAG, "delete: Deleting duplicate profiles");
                database.execSQL(EgoEaterContract.DeleteDuplicateProfiles.SQL);
                return -1;
        }

        return 0;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mainDatabaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case PROFILE_ID_TABLE_KEY:
                return database.update(ProfileIdTable.TABLE_NAME, values, selection, selectionArgs);
            case PROFILE_TABLE_KEY:
                return database.update(ProfileTable.TABLE_NAME, values, selection, selectionArgs);
            case RATING_TABLE_KEY:
                return database.update(RatingTable.TABLE_NAME, values, selection, selectionArgs);
            case PIPELINE_TABLE_KEY:
                return database.update(PipelineTable.TABLE_NAME, values, selection, selectionArgs);
        }
        return 0;
    }
}
