package com.playposse.egoeater.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple enhancement of a {@link ContentProvider} that makes reduces boilerplate code for
 * adding tableMap.
 */
public abstract class BasicContentProvider extends ContentProvider {

    private static final String LOG_TAG = BasicContentProvider.class.getSimpleName();

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteOpenHelper databaseHelper;
    private Map<Integer, Table> tableMap = new HashMap<>();

    protected void addTable(
            int key,
            String authority,
            String path,
            Uri notificationUri,
            String tableName) {

        tableMap.put(key, new Table(key, authority, path, notificationUri, tableName));

        uriMatcher.addURI(authority, path, key);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = createDatabaseHelper();
        return true;
    }

    protected abstract SQLiteOpenHelper createDatabaseHelper();


    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {

        ContentResolver contentResolver = getContext().getContentResolver();
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        int key = uriMatcher.match(uri);
        if (tableMap.containsKey(key)) {
            Table table = tableMap.get(key);

            Cursor cursor = database.query(
                    table.getTableName(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder);
            cursor.setNotificationUri(contentResolver, table.getNotificationUri());
            return cursor;
        } else {
            return null;
        }
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (getContext() == null) {
            Log.e(LOG_TAG, "insert: getContext was null!");
            return null;
        }

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentResolver contentResolver = getContext().getContentResolver();

        int key = uriMatcher.match(uri);
        if (tableMap.containsKey(key)) {
            Table table = tableMap.get(key);

            long id = database.insert(table.getTableName(), null, values);
            contentResolver.notifyChange(table.getNotificationUri(), null);
            return ContentUris.withAppendedId(table.getNotificationUri(), id);
        } else {
            return null;
        }
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        if (getContext() == null) {
            Log.e(LOG_TAG, "update: Context was unexpectedly null");
            return -1;
        }
        ContentResolver contentResolver = getContext().getContentResolver();

        int key = uriMatcher.match(uri);
        if (tableMap.containsKey(key)) {
            Table table = tableMap.get(key);

            int count = database.update(table.getTableName(), values, selection, selectionArgs);
            contentResolver.notifyChange(table.getNotificationUri(), null);
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public int delete(
            @NonNull Uri uri,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {

        if (getContext() == null) {
            Log.e(LOG_TAG, "delete: getContext was null!");
            return 0;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int key = uriMatcher.match(uri);
        if (tableMap.containsKey(key)) {
            Table table = tableMap.get(key);

            int count = database.delete(table.getTableName(), selection, selectionArgs);
            contentResolver.notifyChange(table.getNotificationUri(), null);
            return count;
        } else {
            return 0;
        }
    }

    /**
     * A simple data structure to hold all the information about a table together.
     */
    private class Table {

        private final int key;
        private final String authority;
        private final String path;
        private final Uri notificationUri;
        private final String tableName;

        private Table(
                int key,
                String authority,
                String path,
                Uri notificationUri,
                String tableName) {

            this.key = key;
            this.authority = authority;
            this.path = path;
            this.notificationUri = notificationUri;
            this.tableName = tableName;
        }

        private int getKey() {
            return key;
        }

        private String getAuthority() {
            return authority;
        }

        private String getPath() {
            return path;
        }

        private Uri getNotificationUri() {
            return notificationUri;
        }

        private String getTableName() {
            return tableName;
        }
    }
}
