package com.playposse.egoeater.util;

import android.app.Application;
import android.content.Context;

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
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.messageSentEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.ratingEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reportAbuseEvent;

/**
 * Helper class to make reporting information to Google Analytics less verbose.
 */
public class AnalyticsUtil {

    private static final String SENDER_ID_ATTRIBUTE = "senderId";
    private static final String RECIPIENT_ID_ATTRIBUTE = "recipientId";
    private static final String OTHER_ANSWER_ATTRIBUTE = "otherAnswer";
    private static final String MESSAGE_NAME_ATTRIBUTE = "messageName";

    enum AnalyticsCategory {
        firebaseEvent,
        ratingEvent,
        fuckOffEvent,
        reportAbuseEvent,
        connectivityLost,
        connectivityRestored,
        enteredOtherProfileOption,
        loginEvent,
        messageSentEvent,
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
                .putCustomAttribute(MESSAGE_NAME_ATTRIBUTE, messageName));
    }

    public static void reportFuckOffEvent(Application app, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(app, fuckOffEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(fuckOffEvent.name())
                .putCustomAttribute(SENDER_ID_ATTRIBUTE, senderId)
                .putCustomAttribute(RECIPIENT_ID_ATTRIBUTE, recipientId));
    }

    public static void reportAbuseEvent(Application app, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(app, reportAbuseEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(reportAbuseEvent.name())
                .putCustomAttribute(SENDER_ID_ATTRIBUTE, senderId)
                .putCustomAttribute(RECIPIENT_ID_ATTRIBUTE, recipientId));
    }

    public static void reportOtherProfileOption(Application app, String otherAnswer) {
        AnalyticsUtil.reportEvent(
                app,
                enteredOtherProfileOption,
                otherAnswer);

        Answers.getInstance().logCustom(new CustomEvent(enteredOtherProfileOption.name())
                .putCustomAttribute(OTHER_ANSWER_ATTRIBUTE, otherAnswer));
    }

    public static void reportConnectivityLost(Application app) {
        AnalyticsUtil.reportEvent(app, connectivityLost, "");

        Answers.getInstance().logCustom(new CustomEvent(connectivityLost.name()));
    }

    public static void reportConnectivityRestored(Application app) {
        AnalyticsUtil.reportEvent(app, connectivityRestored, "");

        Answers.getInstance().logCustom(new CustomEvent(connectivityRestored.name()));
    }

    public static void reportMessageSent(Context context, Long senderId, long recipientId) {
        Application app = (Application) context.getApplicationContext();

        AnalyticsUtil.reportEvent(app, messageSentEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(messageSentEvent.name())
                .putCustomAttribute(SENDER_ID_ATTRIBUTE, senderId)
                .putCustomAttribute(RECIPIENT_ID_ATTRIBUTE, recipientId));
    }
}
