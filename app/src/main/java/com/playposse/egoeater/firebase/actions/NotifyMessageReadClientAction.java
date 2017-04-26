package com.playposse.egoeater.firebase.actions;

import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.firebase.FirebaseMessage;

/**
 * A Firebase client action that receives notification that a message has been read.
 */
public class NotifyMessageReadClientAction extends FirebaseClientAction {

    private static final String RECIPIENT_PROFILE_ID_KEY = "recipientProfileId";
    private static final String MESSAGE_INDEX_KEY = "messageIndex";

    public NotifyMessageReadClientAction(RemoteMessage remoteMessage) {
        super(remoteMessage);
    }

    @Override
    protected void execute(RemoteMessage remoteMessage) {
        NotifyMessageReadMessage message = new NotifyMessageReadMessage(remoteMessage);

        QueryUtil.markMessageRead(
                getApplicationContext(),
                message.getRecipientProfileId(),
                message.getMessageIndex());
    }

    private static final class NotifyMessageReadMessage extends FirebaseMessage {

        private NotifyMessageReadMessage(RemoteMessage message) {
            super(message);
        }

        public long getRecipientProfileId() {
            return getLong(RECIPIENT_PROFILE_ID_KEY);
        }

        public int getMessageIndex() {
            return getInteger(MESSAGE_INDEX_KEY);
        }
    }
}
