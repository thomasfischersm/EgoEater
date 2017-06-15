package com.playposse.egoeater.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.MatchesActivity;

/**
 * A utility to create notifications.
 */
public final class NotificationUtil {

    public enum NotificationType {
        UpdatedMatches(
                1,
                R.drawable.ic_favorite_border_black_24dp,
                R.string.updated_matches_notification_title,
                R.string.updated_matches_notification_text,
                MatchesActivity.class),
        OnlyLostMatches(
                2,
                R.drawable.ic_favorite_border_black_24dp,
                R.string.only_lost_matches_notification_title,
                R.string.only_lost_matches_notification_text,
                MatchesActivity.class),
        NewMessage(
                3,
                R.drawable.ic_message_black_24dp,
                R.string.new_message_notification_title,
                R.string.new_message_notification_text,
                MatchesActivity.class),
        Unmatched(
                4,
                R.drawable.ic_noun_882380_cc,
                R.string.unmatch_notification_title,
                R.string.unmatch_notification_text,
                MatchesActivity.class),
        NotLoggedIn(
                5,
                R.drawable.ic_favorite_border_black_24dp,
                R.string.not_logged_in_notification_title,
                R.string.not_logged_in_notification_text,
                MatchesActivity.class),
        ;
        private int notificationId;
        private int iconId;
        private int titleId;
        private int textId;
        private Class<?> activityClass;

        NotificationType(
                int notificationId,
                int iconId,
                int titleId,
                int textId,
                Class<?> activityClass) {

            this.notificationId = notificationId;
            this.iconId = iconId;
            this.titleId = titleId;
            this.textId = textId;
            this.activityClass = activityClass;
        }

        public int getId() {
            return notificationId;
        }

        public int getIconId() {
            return iconId;
        }

        public int getTitleId() {
            return titleId;
        }

        public int getTextId() {
            return textId;
        }

        public Class<?> getActivityClass() {
            return activityClass;
        }
    }

    private NotificationUtil() {}

    public static void sendNotification(Context context, NotificationType type) {

        Intent intent = new Intent(context, type.getActivityClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        String title = context.getString(type.getTitleId());
        String text = context.getString(type.getTextId());

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(type.getIconId())
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(type.getId(), notification);
    }

    public static void clear(Context context, NotificationType notificationType) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationType.getId());

    }
}
