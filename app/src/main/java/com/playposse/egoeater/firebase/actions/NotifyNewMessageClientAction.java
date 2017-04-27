package com.playposse.egoeater.firebase.actions;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.activity.CurrentActivity;
import com.playposse.egoeater.backend.egoEaterApi.model.MessageBean;
import com.playposse.egoeater.clientactions.GetConversationClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MessageTable;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.firebase.FirebaseMessage;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.NotificationUtil;
import com.playposse.egoeater.util.NotificationUtil.NotificationType;

import java.util.List;

/**
 * A client action that receives a Firebase notification that another user has sent this user a
 * message.
 * <p>
 * <p>If the message is short enough, it is included in the Firebase message.
 */
public class NotifyNewMessageClientAction extends FirebaseClientAction {

    private static final String LOG_TAG = NotifyNewMessageClientAction.class.getSimpleName();

    private static final String SENDER_PROFILE_ID_KEY = "senderProfileId";
    private static final String IS_MESSAGE_INCLUDED_KEY = "isMessageIncluded";
    private static final String MESSAGE_INDEX_KEY = "messageIndex";
    private static final String MESSAGE_CONTENT_KEY = "messageContent";

    public NotifyNewMessageClientAction(RemoteMessage remoteMessage) {
        super(remoteMessage);
    }

    @Override
    protected void execute(RemoteMessage remoteMessage) {
        NotifyNewMessageMessage message = new NotifyNewMessageMessage(remoteMessage);
        long senderProfileId = message.getSenderProfileId();
        long profileId = EgoEaterPreferences.getUser(getApplicationContext()).getUserId();

        // Check if the message content is included.
        try {
            if (!message.isMessageIncluded()) {
                reImportConversation(getApplicationContext(), senderProfileId, profileId);
            } else if (!isMessageIndexCorrect(message, senderProfileId, profileId)) {
                reImportConversation(getApplicationContext(), senderProfileId, profileId);
            } else {
                updateConversationMessage(message, senderProfileId, profileId);
            }

            if ((CurrentActivity.getMessagingPartnerId() == null)
                    || (CurrentActivity.getMessagingPartnerId() != senderProfileId)) {
                NotificationUtil.sendNotification(
                        getApplicationContext(),
                        NotificationType.NewMessage);
            }

            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            QueryUtil.markMatchHasNewMessage(contentResolver, senderProfileId, true);
        } catch (InterruptedException ex) {
            Log.e(LOG_TAG, "execute: Failed to process new message.", ex);
        }
    }

    private boolean isMessageIndexCorrect(
            NotifyNewMessageMessage message,
            long senderProfileId,
            long profileId) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Integer currentMessageIndex =
                QueryUtil.getMaxMessageIndex(contentResolver, profileId, senderProfileId);
        return ((currentMessageIndex != null)
                && (currentMessageIndex + 1 == message.getMessageIndex()));
    }

    public static void reImportConversation(
            Context context,
            long senderProfileId,
            long profileId) throws InterruptedException {

        deleteConversation(context, senderProfileId, profileId);
        QueryUtil.lockMatch(context.getContentResolver(), senderProfileId);
        List<MessageBean> messages =
                GetConversationClientAction.getBlocking(context, senderProfileId);
        save(context, messages, senderProfileId, profileId);
    }

    private static void deleteConversation(Context context, long senderProfileId, long profileId) {
        ContentResolver contentResolver = context.getContentResolver();
        String senderStr = Long.toString(senderProfileId);
        String recipientStr = Long.toString(profileId);
        int rowCount = contentResolver.delete(
                MessageTable.CONTENT_URI,
                "((sender_profile_id = ?) and (recipient_profile_id = ?)) " +
                        "or ((sender_profile_id = ?) and (recipient_profile_id = ?))",
                new String[]{
                        senderStr,
                        recipientStr,
                        recipientStr,
                        senderStr});
        Log.i(LOG_TAG, "deleteConversation: Deleted old conversation: " + rowCount);
    }

    private static void save(
            Context context,
            List<MessageBean> messages,
            long senderProfileId,
            long profileId) {
        ContentValues[] contentValuesArray = new ContentValues[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            MessageBean message = messages.get(i);
            long recipientProfileId =
                    inverse(message.getSenderProfileId(), senderProfileId, profileId);

            contentValuesArray[i] = new ContentValues();
            contentValuesArray[i].put(MessageTable.SENDER_PROFILE_ID_COLUMN, message.getSenderProfileId());
            contentValuesArray[i].put(MessageTable.RECIPIENT_PROFILE_ID_COLUMN, recipientProfileId);
            contentValuesArray[i].put(MessageTable.MESSAGE_INDEX_COLUMN, message.getMessageIndex());
            contentValuesArray[i].put(MessageTable.IS_RECEIVED_COLUMN, message.getReceived());
            contentValuesArray[i].put(MessageTable.CREATED_COLUMN, message.getCreated());
            contentValuesArray[i].put(MessageTable.MESSAGE_CONTENT_COLUMN, message.getMessageContent());
        }

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.bulkInsert(MessageTable.CONTENT_URI, contentValuesArray);
    }

    /**
     * Takes a choice of two longs. Specifies one of the two and returns the other.
     */
    private static long inverse(long thisLong, long choiceA, long choiceB) {
        return (thisLong == choiceA) ? choiceB : choiceA;
    }

    private void updateConversationMessage(
            NotifyNewMessageMessage message,
            long senderProfileId,
            long profileId) {

        long recipientProfileId =
                inverse(message.getSenderProfileId(), senderProfileId, profileId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageTable.SENDER_PROFILE_ID_COLUMN, message.getSenderProfileId());
        contentValues.put(MessageTable.RECIPIENT_PROFILE_ID_COLUMN, recipientProfileId);
        contentValues.put(MessageTable.MESSAGE_INDEX_COLUMN, message.getMessageIndex());
        contentValues.put(MessageTable.IS_RECEIVED_COLUMN, false);
        contentValues.put(MessageTable.MESSAGE_CONTENT_COLUMN, message.getMessageContent());

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.insert(MessageTable.CONTENT_URI, contentValues);
    }

    /**
     * A Firebase message that tells the buddy that the senior buddy has successfully authorized
     * the buddy to teach the mission.
     */
    private static final class NotifyNewMessageMessage extends FirebaseMessage {

        private NotifyNewMessageMessage(RemoteMessage message) {
            super(message);
        }

        public long getSenderProfileId() {
            return getLong(SENDER_PROFILE_ID_KEY);
        }

        public boolean isMessageIncluded() {
            return getBoolean(IS_MESSAGE_INCLUDED_KEY);
        }

        public int getMessageIndex() {
            return getInteger(MESSAGE_INDEX_KEY);
        }

        public String getMessageContent() {
            return data.get(MESSAGE_CONTENT_KEY);
        }
    }
}
