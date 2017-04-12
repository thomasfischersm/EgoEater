package com.playposse.egoeater;

import android.app.Application;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Implementation of {@link Application} for Bag Zombie app.
 */
public class EgoEaterApplication extends Application {

    private Tracker tracker;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        getApplicationContext().deleteDatabase("egoEaterDb");
//    }

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
}