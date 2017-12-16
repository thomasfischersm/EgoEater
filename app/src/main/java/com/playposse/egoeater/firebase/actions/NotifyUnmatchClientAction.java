package com.playposse.egoeater.firebase.actions;

import android.content.ContentResolver;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.activity.base.CurrentActivity;
import com.playposse.egoeater.activity.MatchesActivity;
import com.playposse.egoeater.contentprovider.FuckOffUtil;
import com.playposse.egoeater.firebase.FirebaseMessage;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.NotificationUtil;
import com.playposse.egoeater.util.NotificationUtil.NotificationType;

/**
 * A client action that informs the user that another user has told him/her to fuck off.
 */
public class NotifyUnmatchClientAction extends FirebaseClientAction {

    private static final String PARTNER_ID_KEY = "partnerId";

    public NotifyUnmatchClientAction(RemoteMessage remoteMessage) {
        super(remoteMessage);
    }

    @Override
    protected void execute(RemoteMessage remoteMessage) {
        // Prepare references.
        NotifyNewMessageMessage message = new NotifyNewMessageMessage(remoteMessage);
        long partnerId = message.getPartnerId();
        ContentResolver contentResolver = getApplicationContext().getContentResolver();

        // Handle the clearing.
        EgoEaterPreferences.addPissedOffUser(getApplicationContext(), partnerId);
        FuckOffUtil.eraseUserLocally(contentResolver, partnerId);

        // Send the user a notification.
        NotificationUtil.sendNotification(getApplicationContext(), NotificationType.Unmatched);

        // If the user is in a chat with that user currently, exit the chat.
        if ((CurrentActivity.getMessagingPartnerId() != null)
                && (CurrentActivity.getMessagingPartnerId() == partnerId)) {
            startActivity(new Intent(getApplicationContext(), MatchesActivity.class));
        }
    }

    /**
     * A Firebase message that carries the profile id of the user that told this user to fuck off.
     */
    private static final class NotifyNewMessageMessage extends FirebaseMessage {

        private NotifyNewMessageMessage(RemoteMessage message) {
            super(message);
        }

        private long getPartnerId() {
            return getLong(PARTNER_ID_KEY);
        }
    }
}
