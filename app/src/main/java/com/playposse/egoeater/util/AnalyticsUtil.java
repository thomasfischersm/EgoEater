package com.playposse.egoeater.util;

import android.app.Application;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.RatingEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.playposse.egoeater.EgoEaterApplication;

import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.connectivityLost;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.connectivityRestored;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.enteredOtherProfileOption;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.firebaseEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.fuckOffEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.loginEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.ratingEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reportAbuseEvent;

/**
 * Helper class to make reporting information to Google Analytics less verbose.
 */
public class AnalyticsUtil {

    enum AnalyticsCategory {
        firebaseEvent,
        ratingEvent,
        fuckOffEvent,
        reportAbuseEvent,
        connectivityLost,
        connectivityRestored,
        enteredOtherProfileOption,
        loginEvent,
    }

    private static void reportEvent(
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

    public static void reportRating(Application app, long winnerId) {
        AnalyticsUtil.reportEvent(app, ratingEvent, "");

        Answers.getInstance().logRating(new RatingEvent()
                .putContentId(Long.toString(winnerId)));
    }

    public static void reportLogin(Application app) {
        AnalyticsUtil.reportEvent(app, loginEvent, "");

        Answers.getInstance().logLogin(new LoginEvent()
                .putSuccess(true));
    }

    public static void reportFirebaseMessageReceived(Application app, String messageName) {
        AnalyticsUtil.reportEvent(app, firebaseEvent, messageName);

        Answers.getInstance().logCustom(new CustomEvent(firebaseEvent.name())
                .putCustomAttribute("messageName", messageName));
    }

    public static void reportFuckOffEvent(Application app, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(app, fuckOffEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(fuckOffEvent.name())
                .putCustomAttribute("senderId", senderId)
                .putCustomAttribute("recipientId", recipientId));
    }

    public static void reportAbuseEvent(Application app, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(app, reportAbuseEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(reportAbuseEvent.name())
                .putCustomAttribute("senderId", senderId)
                .putCustomAttribute("recipientId", recipientId));
    }

    public static void reportOtherProfileOption(Application app, String otherAnswer) {
        AnalyticsUtil.reportEvent(
                app,
                enteredOtherProfileOption,
                otherAnswer);

        Answers.getInstance().logCustom(new CustomEvent(enteredOtherProfileOption.name())
                .putCustomAttribute("otherAnswer", otherAnswer));
    }

    public static void reportConnectivityLost(Application app) {
        AnalyticsUtil.reportEvent(app, connectivityLost, "");

        Answers.getInstance().logCustom(new CustomEvent(connectivityLost.name()));
    }

    public static void reportConnectivityRestored(Application app) {
        AnalyticsUtil.reportEvent(app, connectivityRestored, "");

        Answers.getInstance().logCustom(new CustomEvent(connectivityRestored.name()));
    }
}
