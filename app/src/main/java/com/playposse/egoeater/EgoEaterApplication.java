package com.playposse.egoeater;

import android.app.Application;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.firebase.EgoEaterFirebaseMessagingService;
import com.playposse.egoeater.util.NetworkConnectivityBroadcastReceiver;

/**
 * Implementation of {@link Application} for Bag Zombie app.
 */
public class EgoEaterApplication extends MultiDexApplication {

    private Tracker tracker;
    private NetworkConnectivityBroadcastReceiver connectivityBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        EgoEaterFirebaseMessagingService.subscribeToTopicsOnAppStart(getApplicationContext());

        // Trigger Firebase to retrieve a token before it is needed during sign in.
        FirebaseInstanceId.getInstance().getToken();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityBroadcastReceiver = new NetworkConnectivityBroadcastReceiver();
        registerReceiver(connectivityBroadcastReceiver, filter);

//        getApplicationContext().deleteDatabase("egoEaterDb");
//        createTestMatches();
//        EgoEaterPreferences.reset(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (connectivityBroadcastReceiver != null) {
            unregisterReceiver(connectivityBroadcastReceiver);
            connectivityBroadcastReceiver = null;
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }

    /**
     * Creates 10 matches in the local database. Testing only!
     */
    private void createTestMatches() {
        int counter = 1;
        long profileId = getRandomProfileId();

        for (int i = 0; i < 10; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MatchTable.MATCH_ID_COLUMN, counter);
            contentValues.put(MatchTable.PROFILE_ID_COLUMN, profileId);
            contentValues.put(MatchTable.IS_LOCKED_COLUMN, (counter % 3 == 2));
            contentValues.put(MatchTable.HAS_NEW_MESSAGE, (counter % 3 == 1));
            getContentResolver().insert(MatchTable.CONTENT_URI, contentValues);

            counter++;
        }
    }

    private long getRandomProfileId() {
        Cursor cursor = getContentResolver()
                .query(ProfileIdTable.CONTENT_URI, ProfileIdTable.COLUMN_NAMES, null, null, null);

        if (cursor.moveToNext() && cursor.moveToNext()) {
            String idColumnName = ProfileIdTable.PROFILE_ID_COLUMN.toUpperCase();
            return cursor.getLong(cursor.getColumnIndex(idColumnName));
        } else {
            return -1;
        }
    }
}
