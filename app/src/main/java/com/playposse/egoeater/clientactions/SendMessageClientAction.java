package com.playposse.egoeater.clientactions;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that sends a message to another user.
 */
public class SendMessageClientAction extends ApiClientAction<Void> {

    private final long recipientId;
    private final String message;

    public SendMessageClientAction(Context context, long recipientId, String message) {
        super(context);

        this.recipientId = recipientId;
        this.message = message;
    }

    @Override
    protected Void executeAsync() throws IOException {
        int messageIndex = saveMessageLocally(recipientId, message);
        if (messageIndex == 0) {
            lockMatchLocally(recipientId);
        }

        getApi().sendMessage(getSessionId(), recipientId, message).execute();

        return null;
    }

    private int saveMessageLocally(long recipientId, String messageContent) {
        ContentResolver contentResolver = getContext().getContentResolver();
        Long senderId = EgoEaterPreferences.getUser(getContext()).getUserId();
        Integer messageIndex = QueryUtil.getMaxMessageIndex(
                contentResolver,
                senderId,
                recipientId);

        if (messageIndex == null) {
            messageIndex = 0;
        } else {
            messageIndex++;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(EgoEaterContract.MessageTable.SENDER_PROFILE_ID_COLUMN, senderId);
        contentValues.put(EgoEaterContract.MessageTable.RECIPIENT_PROFILE_ID_COLUMN, recipientId);
        contentValues.put(EgoEaterContract.MessageTable.MESSAGE_INDEX_COLUMN, messageIndex);
        contentValues.put(EgoEaterContract.MessageTable.IS_RECEIVED_COLUMN, false);
        contentValues.put(EgoEaterContract.MessageTable.MESSAGE_CONTENT_COLUMN, messageContent);

        contentResolver.insert(EgoEaterContract.MessageTable.CONTENT_URI, contentValues);
        return messageIndex;
    }

    private void lockMatchLocally(long recipientId) {
        QueryUtil.lockMatch(getContext().getContentResolver(), recipientId);
    }
}
