package com.playposse.egoeater.contentprovider.admin;

import android.content.ContentProvider;
import android.database.sqlite.SQLiteOpenHelper;

import com.playposse.egoeater.contentprovider.admin.AdminContract.EgoEaterUserTable;
import com.playposse.egoeater.contentprovider.admin.AdminContract.MessageTable;
import com.playposse.egoeater.util.BasicContentProvider;

/**
 * A {@link ContentProvider} that caches cloud data for admins to run complex queries without
 * taxing the cloud.
 */
public class AdminContentProvider extends BasicContentProvider {

    private static final int EGO_EATER_USER_TABLE_KEY = 1;
    private static final int MESSAGE_TABLE_KEY = 2;

    public AdminContentProvider() {
        addTable(
                EGO_EATER_USER_TABLE_KEY,
                AdminContract.AUTHORITY,
                EgoEaterUserTable.PATH,
                EgoEaterUserTable.CONTENT_URI,
                EgoEaterUserTable.TABLE_NAME);

        addTable(
                MESSAGE_TABLE_KEY,
                AdminContract.AUTHORITY,
                MessageTable.PATH,
                MessageTable.CONTENT_URI,
                MessageTable.TABLE_NAME);
    }

    @Override
    protected SQLiteOpenHelper createDatabaseHelper() {
        return new AdminDatabaseHelper(getContext());
    }
}
