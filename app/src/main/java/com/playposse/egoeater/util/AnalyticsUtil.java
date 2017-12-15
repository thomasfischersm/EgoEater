package com.playposse.egoeater.util;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.RatingEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
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
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.profileBuilderSavedEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.ratingEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reactivateAccountEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.reportAbuseEvent;
import static com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory.savedProfileEvent;
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
        profileBuilderSavedEvent,
        photoUploadedEvent,
        deactivateAccountEvent,
        reactivateAccountEvent,
        updateBirthdayOverrideEvent,
        userBlockedForIncompleteProfileEvent,
        userBlockedForMissingBirthdayEvent,
        savedProfileEvent,
    }

    public enum UserProperty {
        isLoggedIn,
        hasPhotoUploaded,
        hasProfileFilledOut,
        hasRated,
        hasMessaged,
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
        AnalyticsUtil.reportEvent(app, ratingEvent, ratingEvent.name());

        Answers.getInstance().logRating(new RatingEvent()
                .putContentId(Long.toString(winnerId)));

        setUserProperties(app.getApplicationContext(), UserProperty.hasRated, true);
    }

    public static void reportLogin(Application app) {
        AnalyticsUtil.reportEvent(app, loginEvent, loginEvent.name());

        Answers.getInstance().logLogin(new LoginEvent()
                .putSuccess(true));

        Context context = app.getApplicationContext();
        AnalyticsUtil.setUserProperties(context, UserProperty.isLoggedIn, true);
    }

    public static void reportFirebaseMessageReceived(Application app, String messageName) {
        AnalyticsUtil.reportEvent(app, firebaseEvent, messageName);

        Answers.getInstance().logCustom(new CustomEvent(firebaseEvent.name())
                .putCustomAttribute(MESSAGE_NAME_ATTRIBUTE, messageName));
    }

    public static void reportFuckOffEvent(Application app, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(app, fuckOffEvent, fuckOffEvent.name());

        Answers.getInstance().logCustom(new CustomEvent(fuckOffEvent.name())
                .putCustomAttribute(SENDER_ID_ATTRIBUTE, senderId)
                .putCustomAttribute(RECIPIENT_ID_ATTRIBUTE, recipientId));
    }

    public static void reportAbuseEvent(Application app, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(app, reportAbuseEvent, reportAbuseEvent.name());

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
        AnalyticsUtil.reportEvent(app, connectivityLost, connectivityLost.name());

        Answers.getInstance().logCustom(new CustomEvent(connectivityLost.name()));
    }

    public static void reportConnectivityRestored(Application app) {
        AnalyticsUtil.reportEvent(app, connectivityRestored, connectivityRestored.name());

        Answers.getInstance().logCustom(new CustomEvent(connectivityRestored.name()));
    }

    public static void reportMessageSent(Context context, Long senderId, long recipientId) {
        AnalyticsUtil.reportEvent(getApp(context), messageSentEvent, messageSentEvent.name());

        Answers.getInstance().logCustom(new CustomEvent(messageSentEvent.name())
                .putCustomAttribute(SENDER_ID_ATTRIBUTE, senderId)
                .putCustomAttribute(RECIPIENT_ID_ATTRIBUTE, recipientId));

        setUserProperties(context, UserProperty.hasMessaged, true);
    }

    public static void reportProfileBuilderOpened(Application app) {
        report(app, profileBuilderOpenedEvent);
    }

    public static void reportProfileBuilderSaved(Application app) {
        report(app, profileBuilderSavedEvent);

        setUserProperties(app.getApplicationContext(), UserProperty.hasProfileFilledOut, true);
    }
    public static void reportPhotoUploaded(Context context, int photoIndex) {
        AnalyticsUtil.reportEvent(getApp(context), photoUploadedEvent, photoUploadedEvent.name());

        Answers.getInstance().logCustom(new CustomEvent(photoUploadedEvent.name())
                .putCustomAttribute(PHOTO_INDEX_ATTRIBUTE, photoIndex));

        setUserProperties(context, UserProperty.hasPhotoUploaded, true);
    }

    public static void reportDeactivateAccount(Application app) {
        report(app, deactivateAccountEvent);
    }

    public static void reportReactivateAccount(Application app) {
        report(app, reactivateAccountEvent);
    }

    public static void reportUpdateBirthdayOverride(Context context) {
        report(context, updateBirthdayOverrideEvent);
    }

    public static void reportUserBlockedForIncompleteProfile(Context context) {
        report(context, userBlockedForIncompleteProfileEvent);
    }

    public static void reportUserBlockedForMissingBirthday(Context context) {
        report(context, userBlockedForMissingBirthdayEvent);
    }

    public static void reportSavedProfile(Application app) {
        report(app, savedProfileEvent);

        setUserProperties(app.getApplicationContext(), UserProperty.hasProfileFilledOut, true);
    }

    public static void setUserProperties(Context context, UserProperty property, boolean value) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setUserProperty(property.name(), Boolean.toString(value));
    }

    private static void report(Application app, AnalyticsCategory category) {
        AnalyticsUtil.reportEvent(app, category, category.name());

        Answers.getInstance().logCustom(new CustomEvent(category.name()));
    }

    private static void report(Context context, AnalyticsCategory category) {
        report(getApp(context), category);
    }

    private static Application getApp(Context context) {
        return (Application) context.getApplicationContext();
    }
}
