package com.playposse.egoeater.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.FuckOffClientAction;
import com.playposse.egoeater.clientactions.GetMaxMessageIndexClientAction;
import com.playposse.egoeater.clientactions.ReportMessageReadClientAction;
import com.playposse.egoeater.clientactions.SendMessageClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MessageTable;
import com.playposse.egoeater.contentprovider.FuckOffUtil;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.firebase.actions.NotifyNewMessageClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.RecyclerViewCursorAdapter;
import com.playposse.egoeater.util.SimpleAlertDialog;
import com.playposse.egoeater.util.SmartCursor;
import com.playposse.egoeater.util.StringUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * An {@link Activity} that lets two users message each other.
 * <p>
 * TODO: Check for new messages on startup, in case something went bad with the Firebase messaging.
 */
public class MessagingActivity
        extends ParentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MessagingActivity.class.getSimpleName();

    private static final int LOADER_ID = 3;

    private RecyclerView messagesRecyclerView;
    private TextView noMessagesTextView;
    private EditText newMessageEditText;
    private ImageButton sendButton;

    private long profileId;
    private long partnerId;
    private UserBean userBean;
    private ProfileParcelable partner;

    private MessagesCursorAdapter messagesCursorAdapter;
    private ContentObserver contentObserver;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_messaging;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Find the views.
        messagesRecyclerView = (RecyclerView) findViewById(R.id.messagesRecyclerView);
        noMessagesTextView = (TextView) findViewById(R.id.noMessagesTextView);
        newMessageEditText = (EditText) findViewById(R.id.newMessageEditText);
        sendButton = (ImageButton) findViewById(R.id.sendButton);

        // Look up information in the intent and preferences.
        profileId = EgoEaterPreferences.getUser(this).getUserId();
        partnerId = ExtraConstants.getProfileId(getIntent());

        // Build the RecyclerView for messages.
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesCursorAdapter = new MessagesCursorAdapter();
        messagesRecyclerView.setAdapter(messagesCursorAdapter);

        // This will initialize the loader and perform all data operations.
        new LoadProfileAsyncTask().execute();

        // Add click listener to send messages.
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messaging_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fuckOffMenuItem:
                fuckOff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        CurrentActivity.setMessagingPartnerId(partnerId);

        // Refresh the view when new messages arrive.
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Log.i(LOG_TAG, "onChange: Noticed that the message table has changed.");
                messagesCursorAdapter.notifyDataSetChanged();
            }
        };
        getContentResolver().registerContentObserver(
                EgoEaterContract.MessageTable.CONTENT_URI,
                false,
                contentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (contentObserver != null) {
            getContentResolver().unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }

    private void sendMessage() {
        String message = newMessageEditText.getText().toString();
        if (StringUtil.isEmpty(message)) {
            // Ignore empty messages.
            return;
        }

        new SendMessageClientAction(this, partnerId, message).execute();
        newMessageEditText.setText("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String profileStr = Long.toString(profileId);
        String partnerStr = Long.toString(partnerId);

        return new CursorLoader(
                this,
                MessageTable.CONTENT_URI,
                MessageTable.COLUMN_NAMES,
                "((sender_profile_id = ?) and (recipient_profile_id = ?)) " +
                        "or ((sender_profile_id = ?) and (recipient_profile_id = ?))",
                new String[]{
                        profileStr,
                        partnerStr,
                        partnerStr,
                        profileStr},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        messagesCursorAdapter.swapCursor(cursor);
        messagesRecyclerView.setVisibility((cursor.getCount() > 0) ? VISIBLE : GONE);
        noMessagesTextView.setVisibility((cursor.getCount() > 0) ? GONE : VISIBLE);

        messagesRecyclerView.scrollToPosition(cursor.getCount() - 1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        messagesCursorAdapter.swapCursor(null);
        messagesRecyclerView.setVisibility(GONE);
        noMessagesTextView.setVisibility(VISIBLE);
    }

    private void fuckOff() {
        SimpleAlertDialog.confirm(
                this,
                R.string.fuck_off_dialog_title,
                R.string.fuck_off_dialog_text,
                new Runnable() {
                    @Override
                    public void run() {
                        fuckOffConfirmed();
                    }
                }
        );
    }

    private void fuckOffConfirmed() {
        EgoEaterPreferences.addFuckOffUser(this, partnerId);
        new FuckOffClientAction(this, partnerId).execute();
        FuckOffUtil.eraseUserLocally(getContentResolver(), partnerId);

        startActivity(new Intent(this, MatchesActivity.class));
    }

    /**
     * A cursor adapter for messages.
     */
    private class MessagesCursorAdapter extends RecyclerViewCursorAdapter<MessagesViewHolder> {

        private static final int SENT_MESSAGE_TEMPLATE = 1;
        private static final int RECEIVED_MESSAGE_TEMPLATE = 2;

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = getCursor(position);
            long senderId = cursor.getLong(1);

            return (profileId == senderId) ? SENT_MESSAGE_TEMPLATE : RECEIVED_MESSAGE_TEMPLATE;
        }

        @Override
        public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            final int layoutId;
            switch (viewType) {
                case SENT_MESSAGE_TEMPLATE:
                    layoutId = R.layout.sent_message_list_item;
                    break;
                case RECEIVED_MESSAGE_TEMPLATE:
                    layoutId = R.layout.received_message_list_item;
                    break;
                default:
                    throw new IllegalStateException("Encountered unexpected viewType: " + viewType);
            }

            View view =
                    LayoutInflater.from(context).inflate(layoutId, parent, false);
            return new MessagesViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(MessagesViewHolder holder, int position, Cursor cursor) {
            // Read from cursor.
            SmartCursor smartCursor = new SmartCursor(cursor, MessageTable.COLUMN_NAMES);
            long senderId = smartCursor.getLong(MessageTable.SENDER_PROFILE_ID_COLUMN);
            long recipientId = smartCursor.getLong(MessageTable.RECIPIENT_PROFILE_ID_COLUMN);
            String message = smartCursor.getString(MessageTable.MESSAGE_CONTENT_COLUMN);

            // Look up the profile information.
            final String senderPhotoUrl;
            if ((senderId == profileId) && (recipientId == partnerId)) {
                senderPhotoUrl = userBean.getProfilePhotoUrls().get(0);
            } else if ((senderId == partnerId) && (recipientId == profileId)) {
                senderPhotoUrl = partner.getPhotoUrl0();
            } else {
                throw new IllegalStateException("Got a message for recipient " + recipientId +
                        " but was in a conversation with " + partnerId);
            }

            // Populate the view.
            holder.getMessageTextView().setText(message);
            GlideUtil.loadCircular(holder.getProfileImageView(), senderPhotoUrl);

            // Add click listener to partner photo.
            if (senderId == partnerId) {
                holder.getProfileImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExtraConstants.startViewProfileActivity(MessagingActivity.this, partnerId);
                    }
                });
            }
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} for messages.
     */
    private static class MessagesViewHolder extends RecyclerView.ViewHolder {

        private final ImageView profileImageView;
        private final TextView messageTextView;

        private MessagesViewHolder(View itemView) {
            super(itemView);

            profileImageView = (ImageView) itemView.findViewById(R.id.profileImageView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        }

        private ImageView getProfileImageView() {
            return profileImageView;
        }

        private TextView getMessageTextView() {
            return messageTextView;
        }
    }

    /**
     * An {@link AsyncTask} that loads profile information and then starts the message loader.
     */
    private class LoadProfileAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            userBean = EgoEaterPreferences.getUser(MessagingActivity.this);
            partner = QueryUtil.getProfileById(getContentResolver(), partnerId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setTitle(partner.getFirstName());
            getLoaderManager().initLoader(LOADER_ID, null, MessagingActivity.this);
            new ReportMessagesReadAsyncTask().execute();
            new Thread(new CheckForNewMessagesRunnable()).start();
            new Thread(new MarkMessageReadInMatchRunnable()).start();
        }
    }

    /**
     * An {@link AsyncTask} that goes through all the messages and marks them all read, if
     * necessary.
     * <p>
     * <p>TODO: Could actually wait until the user scrolls to the particular message. Consider this
     * perhaps in the future.
     */
    private class ReportMessagesReadAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(
                    MessageTable.CONTENT_URI,
                    new String[]{MessageTable.MESSAGE_INDEX_COLUMN},
                    "(sender_profile_id = ?) and (recipient_profile_id = ?) and is_received = 0",
                    new String[]{
                            Long.toString(partnerId),
                            Long.toString(profileId)},
                    null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int messageIndex = cursor.getInt(0);
                    new ReportMessageReadClientAction(
                            MessagingActivity.this,
                            partnerId,
                            messageIndex).execute();
                }
            }

            return null;
        }
    }

    /**
     * An {@link Runnable} that looks at the start of the activity if the cloud has additional
     * messages.
     */
    private class CheckForNewMessagesRunnable implements Runnable {

        @Override
        public void run() {
            try {
                Log.i(LOG_TAG, "doInBackground: Checking if messages are up-to-date.");
                int cloudId =
                        new GetMaxMessageIndexClientAction(
                                MessagingActivity.this,
                                partnerId)
                                .executeBlocking();

                if (cloudId == -1) {
                    // There are no messages in the cloud.
                    Log.i(LOG_TAG, "doInBackground: The cloud doesn't have messages.");
                    return;
                }

                Integer deviceId =
                        QueryUtil.getMaxMessageIndex(getContentResolver(), profileId, partnerId);
                if ((deviceId != null) && (cloudId == deviceId)) {
                    // The messages are up-to-date.
                    Log.i(LOG_TAG, "doInBackground: Messages are up-to-date.");
                    return;
                }

                // Need to load messages.
                Log.i(LOG_TAG, "doInBackground: Trying to reImport the conversation.");
                NotifyNewMessageClientAction.reImportConversation(
                        MessagingActivity.this,
                        partnerId,
                        profileId);
                return;
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "doInBackground: Failed", ex);
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * A {@link Runnable} that sets the flag on the match as read.
     */
    private class MarkMessageReadInMatchRunnable implements Runnable {

        @Override
        public void run() {
            QueryUtil.markMatchHasNewMessage(getContentResolver(), partnerId, false);
        }
    }
}
