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

import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchAndProfileQuery;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MaxMessageIndexQuery;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MessageTable;
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
    private static final int MATCH_TABLE_KEY = 7;
    private static final int MATCH_AND_PROFILE_QUERY_KEY = 8;
    private static final int MESSAGE_TABLE_KEY = 9;
    private static final int MAX_MESSAGE_INDEX_QUERY_KEY = 10;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, ProfileIdTable.PATH, PROFILE_ID_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, ProfileTable.PATH, PROFILE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, RatingTable.PATH, RATING_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, PipelineTable.PATH, PIPELINE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, EgoEaterContract.DeleteDuplicateProfiles.PATH, DELETE_DUPLICATE_PROFILES_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, PipelineLogTable.PATH, PIPELINE_LOG_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, MatchTable.PATH, MATCH_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, MatchAndProfileQuery.PATH, MATCH_AND_PROFILE_QUERY_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, MessageTable.PATH, MESSAGE_TABLE_KEY);
        uriMatcher.addURI(EgoEaterContract.AUTHORITY, MaxMessageIndexQuery.PATH, MAX_MESSAGE_INDEX_QUERY_KEY);
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

        String tableName;
        switch (uriMatcher.match(uri)) {
            case PROFILE_ID_TABLE_KEY:
                tableName = ProfileIdTable.TABLE_NAME;
                break;
            case PROFILE_TABLE_KEY:
                tableName = ProfileTable.TABLE_NAME;
                break;
            case RATING_TABLE_KEY:
                tableName = RatingTable.TABLE_NAME;
                break;
            case PIPELINE_TABLE_KEY:
                tableName = PipelineTable.TABLE_NAME;
                break;
            case MATCH_TABLE_KEY:
                tableName = MatchTable.TABLE_NAME;
                break;
            case MATCH_AND_PROFILE_QUERY_KEY:
                return database.rawQuery(
                        MatchAndProfileQuery.SQL,
                        null);
            case MESSAGE_TABLE_KEY:
                tableName = MessageTable.TABLE_NAME;
                break;
            case MAX_MESSAGE_INDEX_QUERY_KEY:
                String[] realSelectionArgs = new String[4];
                realSelectionArgs[0] = selectionArgs[0];
                realSelectionArgs[1] = selectionArgs[1];
                realSelectionArgs[2] = selectionArgs[1];
                realSelectionArgs[3] = selectionArgs[0];
                return database.rawQuery(
                        MaxMessageIndexQuery.SQL,
                        realSelectionArgs);
            default:
                return null;
        }

        return database.query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

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

        String tableName;
        Uri contentUri;
        switch (uriMatcher.match(uri)) {
            case PROFILE_ID_TABLE_KEY:
                tableName = ProfileIdTable.TABLE_NAME;
                contentUri = ProfileIdTable.CONTENT_URI;
                break;
            case PROFILE_TABLE_KEY:
                tableName = ProfileTable.TABLE_NAME;
                contentUri = ProfileTable.CONTENT_URI;
                break;
            case RATING_TABLE_KEY:
                tableName = RatingTable.TABLE_NAME;
                contentUri = RatingTable.CONTENT_URI;
                break;
            case PIPELINE_TABLE_KEY:
                tableName = PipelineTable.TABLE_NAME;
                contentUri = PipelineTable.CONTENT_URI;
                break;
            case PIPELINE_LOG_TABLE_KEY:
                tableName = PipelineLogTable.TABLE_NAME;
                contentUri = PipelineLogTable.CONTENT_URI;
                break;
            case MATCH_TABLE_KEY:
                tableName = MatchTable.TABLE_NAME;
                contentUri = MatchTable.CONTENT_URI;
                break;
            case MESSAGE_TABLE_KEY:
                tableName = MessageTable.TABLE_NAME;
                contentUri = MessageTable.CONTENT_URI;
                break;
            default:
                return null;
        }
        long id = database.insert(tableName, null, values);

        if (uriMatcher.match(uri) == PIPELINE_TABLE_KEY) {
            getContext().getContentResolver().notifyChange(PipelineTable.CONTENT_URI, null);
        } else if (uriMatcher.match(uri) == MESSAGE_TABLE_KEY) {
            getContext().getContentResolver().notifyChange(MessageTable.CONTENT_URI, null);
        }

        return ContentUris.withAppendedId(contentUri, id);
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
            case MATCH_TABLE_KEY:
                return database.delete(MatchTable.TABLE_NAME, selection, selectionArgs);
            case MESSAGE_TABLE_KEY:
                return database.delete(MessageTable.TABLE_NAME, selection, selectionArgs);
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
            case MATCH_TABLE_KEY:
                return database.update(MatchTable.TABLE_NAME, values, selection, selectionArgs);
            case MESSAGE_TABLE_KEY:
                int rowCount = database.update(MessageTable.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MessageTable.CONTENT_URI, null);
                return rowCount;
        }
        return 0;
    }
}
