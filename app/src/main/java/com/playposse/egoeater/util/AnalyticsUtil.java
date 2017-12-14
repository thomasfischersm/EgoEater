package com.playposse.egoeater.util;

import android.app.Application;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.playposse.egoeater.EgoEaterApplication;

import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.ratingEvent;

/**
 * Helper class to make reporting information to Google Analytics less verbose.
 */
public class AnalyticsUtil {

    public static enum AnalyticsCategory {
        firebaseEvent,
        ratingEvent,
        fuckOffEvent,
        reportAbuseEvent,
        connectivityLost,
        connectivityRestored,
        enteredOtherProfileOption,
    }

    public static void reportEvent(
            Application defaultApp,
            AnalyticsCategory category,
            String action) {

        EgoEaterApplication app = (EgoEaterApplication) defaultApp;
        Tracker tracker = app.getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name())
                .setAction(action)
                .build());

        tracker.enableAdvertisingIdCollection(true);
        tracker.enableExceptionReporting(true);
    }

    public static void reportScreenName(Application defaultApp, String screenName) {
        EgoEaterApplication app = (EgoEaterApplication) defaultApp;
        Tracker tracker = app.getDefaultTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void reportRating(Application app) {
        AnalyticsUtil.reportEvent(app, ratingEvent, "");
    }
}
