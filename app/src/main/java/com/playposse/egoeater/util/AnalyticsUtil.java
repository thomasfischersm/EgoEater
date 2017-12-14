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
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.deactivateAccountEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.enteredOtherProfileOption;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.firebaseEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.fuckOffEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.loginEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.messageSentEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.photoUploadedEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.profileBuilderOpenedEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.ratingEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reactivateAccountEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reportAbuseEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.updateBirthdayOverrideEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.userBlockedForIncompleteProfileEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.userBlockedForMissingBirthdayEvent;

/**
 * Helper class to make reporting information to Google Analytics less verbose.
 */
public class AnalyticsUtil {

    private static final String SENDER_ID_ATTRIBUTE = "senderId";
    private static final String RECIPIENT_ID_ATTRIBUTE = "recipientId";
    private static final String OTHER_ANSWER_ATTRIBUTE = "otherAnswer";
    private static final String MESSAGE_NAME_ATTRIBUTE = "messageName";
    private static final String PHOTO_INDEX_ATTRIBUTE = "photoIndex";

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
        profileBuilderOpenedEvent,
        photoUploadedEvent,
        deactivateAccountEvent,
        reactivateAccountEvent,
        updateBirthdayOverrideEvent,
        userBlockedForIncompleteProfileEvent,
        userBlockedForMissingBirthdayEvent,
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
        AnalyticsUtil.reportEvent(getApp(context), messageSentEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(messageSentEvent.name())
                .putCustomAttribute(SENDER_ID_ATTRIBUTE, senderId)
                .putCustomAttribute(RECIPIENT_ID_ATTRIBUTE, recipientId));
    }

    public static void reportProfileBuilderOpened(Application app) {
        AnalyticsUtil.reportEvent(app, profileBuilderOpenedEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(profileBuilderOpenedEvent.name()));
    }

    public static void reportPhotoUploaded(Context context, int photoIndex) {
        AnalyticsUtil.reportEvent(getApp(context), photoUploadedEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(photoUploadedEvent.name())
                .putCustomAttribute(PHOTO_INDEX_ATTRIBUTE, photoIndex));
    }

    public static void reportDeactivateAccount(Application app) {
        AnalyticsUtil.reportEvent(app, deactivateAccountEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(deactivateAccountEvent.name()));
    }

    public static void reportReactivateAccount(Application app) {
        AnalyticsUtil.reportEvent(app, reactivateAccountEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(reactivateAccountEvent.name()));
    }

    public static void reportUpdateBirthdayOverride(Context context) {
        AnalyticsUtil.reportEvent(getApp(context), updateBirthdayOverrideEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(updateBirthdayOverrideEvent.name()));
    }

    public static void reportUserBlockedForIncompleteProfile(Context context) {
        AnalyticsUtil.reportEvent(getApp(context), userBlockedForIncompleteProfileEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(userBlockedForIncompleteProfileEvent.name()));
    }

    public static void reportUserBlockedForMissingBirthday(Context context) {
        AnalyticsUtil.reportEvent(getApp(context), userBlockedForMissingBirthdayEvent, "");

        Answers.getInstance().logCustom(new CustomEvent(userBlockedForMissingBirthdayEvent.name()));
    }

    private static Application getApp(Context context) {
        return (Application) context.getApplicationContext();
    }
}
