package com.playposse.egoeater;

import android.app.Application;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.messaging.FirebaseMessaging;

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

        getApplicationContext().deleteDatabase("egoEaterDb");
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

    }
}