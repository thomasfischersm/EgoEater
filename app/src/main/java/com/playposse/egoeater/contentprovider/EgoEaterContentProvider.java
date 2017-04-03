package com.playposse.egoeater.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.playposse.egoeater.contentprovider.MainDatabaseHelper.PIPELINE_TABLE;
import static com.playposse.egoeater.contentprovider.MainDatabaseHelper.PROFILE_ID_TABLE;
import static com.playposse.egoeater.contentprovider.MainDatabaseHelper.PROFILE_TABLE;
import static com.playposse.egoeater.contentprovider.MainDatabaseHelper.RATING_TABLE;

/**
 * A {@link ContentProvider} to cache data locally in the app.
 */
public class EgoEaterContentProvider extends ContentProvider {

    private static final int PROFILE_ID_TABLE_KEY = 1;
    private static final int PROFILE_TABLE_KEY = 2;
    private static final int RATING_TABLE_KEY = 3;
    private static final int PIPELINE_TABLE_KEY = 4;

    private static final String AUTHORITY = "com.playposse.egoeater.provider";

    private static final String PROFILE_ID_PATH = "profileIds";
    private static final String PROFILE_PATH = "profiles";
    private static final String RATING_PATH = "ratings";
    private static final String PIPELINE_PATH = "pipeline";

    private static final Uri PROFILE_ID_URI = new Uri.Builder()
            .encodedAuthority(AUTHORITY)
            .appendPath(PROFILE_ID_PATH)
            .build();
    private static final Uri PROFILE_URI = new Uri.Builder()
            .encodedAuthority(AUTHORITY)
            .appendPath(PROFILE_PATH)
            .build();
    private static final Uri RATING_URI = new Uri.Builder()
            .encodedAuthority(AUTHORITY)
            .appendPath(RATING_PATH)
            .build();
    private static final Uri PIPELINE_URI = new Uri.Builder()
            .encodedAuthority(AUTHORITY)
            .appendPath(PIPELINE_PATH)
            .build();

    public static final String PATH_SEPARATOR = "/";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, PROFILE_ID_PATH, PROFILE_ID_TABLE_KEY);
        uriMatcher.addURI(AUTHORITY, PROFILE_PATH, PROFILE_TABLE_KEY);
        uriMatcher.addURI(AUTHORITY, RATING_PATH, RATING_TABLE_KEY);
        uriMatcher.addURI(AUTHORITY, PIPELINE_PATH, PIPELINE_TABLE_KEY);
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
                        PROFILE_ID_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case PROFILE_TABLE_KEY:
                return database.query(
                        PROFILE_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case RATING_TABLE_KEY:
                return database.query(
                        RATING_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            case PIPELINE_TABLE_KEY:
                return database.query(
                        PIPELINE_TABLE,
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
                long id = database.insert(PROFILE_ID_TABLE, null, values);
                return Uri.parse(PROFILE_ID_URI.toString() + PATH_SEPARATOR + id);
            case PROFILE_TABLE_KEY:
                long profileId = database.insert(PROFILE_TABLE, null, values);
                return Uri.parse(PROFILE_URI.toString() + PATH_SEPARATOR + profileId);
            case RATING_TABLE_KEY:
                long ratingId = database.insert(RATING_TABLE, null, values);
                return Uri.parse(RATING_URI.toString() + PATH_SEPARATOR + ratingId);
            case PIPELINE_TABLE_KEY:
                long pipelineId = database.insert(PIPELINE_TABLE, null, values);
                return Uri.parse(PIPELINE_URI.toString() + PATH_SEPARATOR + pipelineId);
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
                return database.delete(PROFILE_ID_TABLE, selection, selectionArgs);
            case PROFILE_TABLE_KEY:
                return database.delete(PROFILE_TABLE, selection, selectionArgs);
            case RATING_TABLE_KEY:
                return database.delete(RATING_TABLE, selection, selectionArgs);
            case PIPELINE_TABLE_KEY:
                return database.delete(PIPELINE_TABLE, selection, selectionArgs);
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
                return database.update(PROFILE_ID_TABLE, values, selection, selectionArgs);
            case PROFILE_TABLE_KEY:
                return database.update(PROFILE_TABLE, values, selection, selectionArgs);
            case RATING_TABLE_KEY:
                return database.update(RATING_TABLE, values, selection, selectionArgs);
            case PIPELINE_TABLE_KEY:
                return database.update(PIPELINE_TABLE, values, selection, selectionArgs);
        }
        return 0;
    }
}
