package com.playposse.egoeater;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import static com.playposse.egoeater.firebase.EgoEaterFirebaseMessagingService.ALL_DEVICES_TOPIC;

/**
 * Implementation of {@link Application} for Bag Zombie app.
 */
public class EgoEaterApplication extends Application {

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseMessaging.getInstance().subscribeToTopic(ALL_DEVICES_TOPIC);

        // Trigger Firebase to retrieve a token before it is needed during sign in.
        FirebaseInstanceId.getInstance().getToken();

        getApplicationContext().deleteDatabase("egoEaterDb");
//        createTestMatches();
//        EgoEaterPreferences.reset(getApplicationContext());
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
