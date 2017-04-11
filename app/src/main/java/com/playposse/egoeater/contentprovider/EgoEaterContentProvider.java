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

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.ProfileIdTable.PATH, PROFILE_ID_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.ProfileTable.PATH, PROFILE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.RatingTable.PATH, RATING_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.PipelineTable.PATH, PIPELINE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.DeleteDuplicateProfiles.PATH, DELETE_DUPLICATE_PROFILES_KEY);
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
                        EgoEaterContract.ProfileIdTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case PROFILE_TABLE_KEY:
                return database.query(
                        EgoEaterContract.ProfileTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case RATING_TABLE_KEY:
                return database.query(
                        EgoEaterContract.RatingTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case PIPELINE_TABLE_KEY:
                return database.query(
                        EgoEaterContract.PipelineTable.TABLE_NAME,
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
                long id = database.insert(EgoEaterContract.ProfileIdTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(EgoEaterContract.ProfileIdTable.CONTENT_URI, id);
            case PROFILE_TABLE_KEY:
                long profileId = database.insert(EgoEaterContract.ProfileTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(EgoEaterContract.ProfileTable.CONTENT_URI, profileId);
            case RATING_TABLE_KEY:
                long ratingId = database.insert(EgoEaterContract.RatingTable.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(EgoEaterContract.RatingTable.CONTENT_URI, ratingId);
            case PIPELINE_TABLE_KEY:
                long pipelineId = database.insert(EgoEaterContract.PipelineTable.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(EgoEaterContract.PipelineTable.CONTENT_URI, null);
                return ContentUris.withAppendedId(EgoEaterContract.PipelineTable.CONTENT_URI, pipelineId);
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
                return database.delete(EgoEaterContract.ProfileIdTable.TABLE_NAME, selection, selectionArgs);
            case PROFILE_TABLE_KEY:
                return database.delete(EgoEaterContract.ProfileTable.TABLE_NAME, selection, selectionArgs);
            case RATING_TABLE_KEY:
                return database.delete(EgoEaterContract.RatingTable.TABLE_NAME, selection, selectionArgs);
            case PIPELINE_TABLE_KEY:
                return database.delete(EgoEaterContract.PipelineTable.TABLE_NAME, selection, selectionArgs);
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
                return database.update(EgoEaterContract.ProfileIdTable.TABLE_NAME, values, selection, selectionArgs);
            case PROFILE_TABLE_KEY:
                return database.update(EgoEaterContract.ProfileTable.TABLE_NAME, values, selection, selectionArgs);
            case RATING_TABLE_KEY:
                return database.update(EgoEaterContract.RatingTable.TABLE_NAME, values, selection, selectionArgs);
            case PIPELINE_TABLE_KEY:
                return database.update(EgoEaterContract.PipelineTable.TABLE_NAME, values, selection, selectionArgs);
        }
        return 0;
    }
}
