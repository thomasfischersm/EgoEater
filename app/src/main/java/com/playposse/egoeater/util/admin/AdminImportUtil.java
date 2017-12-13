package com.playposse.egoeater.util.admin;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.AdminEgoEaterUserBean;
import com.playposse.egoeater.clientactions.admin.GetAdminDumpEgoEaterUserClientAction;
import com.playposse.egoeater.contentprovider.admin.AdminContract.EgoEaterUserTable;
import com.playposse.egoeater.contentprovider.admin.AdminDatabaseHelper;
import com.playposse.egoeater.util.DatabaseDumper;

import java.util.List;

/**
 * A utility that imports data from the cloud into the content provider of the admin.
 */
public final class AdminImportUtil {

    private static final String LOG_TAG = AdminImportUtil.class.getSimpleName();

    private AdminImportUtil() {
    }

    public static void refresh(Context context) {
        new RefreshAsyncTask(context).execute();
    }

    private static void resetAdminDb(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        // Truncate all admin tables.
        contentResolver.delete(EgoEaterUserTable.CONTENT_URI, null, null);
    }

    private static void importEgoEaterUser(Context context) throws InterruptedException {
        // Call the cloud.
        List<AdminEgoEaterUserBean> egoEaterUsers =
                new GetAdminDumpEgoEaterUserClientAction(context, null).executeBlocking();
        if (egoEaterUsers == null) {
            // Nothing to do.
            return;
        }

        // Prepare bulk import.
        ContentValues[] contentValuesArray = new ContentValues[egoEaterUsers.size()];
        for (int i = 0; i < egoEaterUsers.size(); i++) {
            AdminEgoEaterUserBean egoEaterUser = egoEaterUsers.get(i);
            ContentValues contentValues = new ContentValues();
            contentValues.put(EgoEaterUserTable.ID_COLUMN, egoEaterUser.getId());
            contentValues.put(EgoEaterUserTable.FB_PROFILE_ID_COLUMN, egoEaterUser.getFbProfileId());
            contentValues.put(EgoEaterUserTable.LAST_LOGIN_COLUMN, egoEaterUser.getLastLogin());
            contentValues.put(EgoEaterUserTable.CREATED_COLUMN, egoEaterUser.getCreated());
            contentValues.put(EgoEaterUserTable.IS_ACTIVE_COLUMN, egoEaterUser.getActive());
            contentValues.put(EgoEaterUserTable.FIRST_NAME_COLUMN, egoEaterUser.getFirstName());
            contentValues.put(EgoEaterUserTable.LAST_NAME_COLUMN, egoEaterUser.getLastName());
            contentValues.put(EgoEaterUserTable.EMAIL_COLUMN, egoEaterUser.getEmail());
            contentValues.put(EgoEaterUserTable.PROFILE_TEXT_COLUMN, egoEaterUser.getProfileText());
            contentValues.put(EgoEaterUserTable.LATITUDE_COLUMN, egoEaterUser.getLatitude());
            contentValues.put(EgoEaterUserTable.LONGITUDE_COLUMN, egoEaterUser.getLongitude());
            contentValues.put(EgoEaterUserTable.CITY_COLUMN, egoEaterUser.getCity());
            contentValues.put(EgoEaterUserTable.STATE_COLUMN, egoEaterUser.getState());
            contentValues.put(EgoEaterUserTable.COUNTRY_COLUMN, egoEaterUser.getCountry());
            contentValues.put(EgoEaterUserTable.BIRTHDAY_COLUMN, egoEaterUser.getBirthday());
            contentValues.put(EgoEaterUserTable.GENDER_COLUMN, egoEaterUser.getGender());
            contentValues.put(EgoEaterUserTable.PROFILE_PHOTO_0_COLUMN, egoEaterUser.getProfilePhoto0());
            contentValues.put(EgoEaterUserTable.PROFILE_PHOTO_1_COLUMN, egoEaterUser.getProfilePhoto1());
            contentValues.put(EgoEaterUserTable.PROFILE_PHOTO_2_COLUMN, egoEaterUser.getProfilePhoto2());

            contentValuesArray[i] = contentValues;
        }

        // Execute bulk import.
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.bulkInsert(EgoEaterUserTable.CONTENT_URI, contentValuesArray);
    }

    /**
     * An {@link AsyncTask} that executes the database refresh.
     */
    private static class RefreshAsyncTask extends AsyncTask<Void, Void, Void> {

        private final Context context;

        private RefreshAsyncTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            resetAdminDb(context);

            try {
                importEgoEaterUser(context);
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "onOptionsItemSelected: ", ex);
                Crashlytics.logException(ex);
            }

            DatabaseDumper.dumpTables(new AdminDatabaseHelper(context));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(context, R.string.admin_refresh_success_toast, Toast.LENGTH_LONG)
                    .show();
        }
    }
}
