package com.playposse.egoeater.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.GetMaxMessageIndexClientAction;
import com.playposse.egoeater.clientactions.ReportMessageReadClientAction;
import com.playposse.egoeater.clientactions.SendMessageClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MessageTable;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.firebase.actions.NotifyNewMessageClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.FuckOffUiHelper;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.RecyclerViewCursorAdapter;
import com.playposse.egoeater.util.SmartCursor;
import com.playposse.egoeater.util.StringUtil;

import java.text.DateFormat;
import java.util.GregorianCalendar;

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
    private ImageView sendImageView;

    private long profileId;
    private long partnerId;
    private UserBean userBean;
    private ProfileParcelable partner;

    private MessagesCursorAdapter messagesCursorAdapter;
    private ContentObserver contentObserver;
    private LinearLayoutManager linearLayoutManager;

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
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        noMessagesTextView = findViewById(R.id.noMessagesTextView);
        newMessageEditText = findViewById(R.id.newMessageEditText);
        sendImageView = findViewById(R.id.sendImageView);

        // Look up information in the intent and preferences.
        Long userId = EgoEaterPreferences.getUser(this).getUserId();
        if (userId == null) {
            // This strange case shows up in the logs. I don't know how the userId could be null
            // for this activity. Let's send the user back to the login screen.
            GlobalRouting.onStartup(this);
            return;
        }
        profileId = userId;
        partnerId = ExtraConstants.getProfileId(getIntent());

        // Build the RecyclerView for messages.
        linearLayoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        messagesCursorAdapter = new MessagesCursorAdapter();
        messagesRecyclerView.setAdapter(messagesCursorAdapter);

        // This will initialize the loader and perform all data operations.
        new LoadProfileAsyncTask().execute();

        // Add click listener to send messages.
        sendImageView.setOnClickListener(new View.OnClickListener() {
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
                FuckOffUiHelper.fuckOff(this, partnerId, getApplication());
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

    @Nullable
    private static boolean isDifferentDay(
            @Nullable Long previousMessageCreated,
            GregorianCalendar current) {

        if (previousMessageCreated == null) {
            return true;
        }

        GregorianCalendar previous = new GregorianCalendar();
        previous.setTimeInMillis(previousMessageCreated);

        return previous.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isToday(GregorianCalendar calendar) {
        GregorianCalendar today = new GregorianCalendar();

        return (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR))
                && (calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));
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
        protected void onBindViewHolder(final MessagesViewHolder holder, final int position, Cursor cursor) {
            // Read from cursor.
            SmartCursor smartCursor = new SmartCursor(cursor, MessageTable.COLUMN_NAMES);
            long senderId = smartCursor.getLong(MessageTable.SENDER_PROFILE_ID_COLUMN);
            long recipientId = smartCursor.getLong(MessageTable.RECIPIENT_PROFILE_ID_COLUMN);
            Long currentCreated = smartCursor.getLong(MessageTable.CREATED_COLUMN);
            Long previousCreated = smartCursor.getLong(MessageTable.PREVIOUS_MESSAGE_CREATED_COLUMN);
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

            // Show date if appropriate.
            GregorianCalendar currentCalendar = new GregorianCalendar();
            currentCalendar.setTimeInMillis(currentCreated);
            boolean isDifferentDay = isDifferentDay(previousCreated, currentCalendar);
            if (isDifferentDay) {
                holder.getDateTextView().setVisibility(VISIBLE);
                if (isToday(currentCalendar)) {
                    holder.getDateTextView().setText(R.string.today_date);
                } else {
                    DateFormat dateFormat =
                            android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    String currentDateStr = dateFormat.format(currentCalendar.getTime());
                    holder.getDateTextView().setText(currentDateStr);
                }
            } else {
                holder.getDateTextView().setVisibility(GONE);
            }

            // Prepare timestamp
            holder.getTimeTextView().setVisibility(GONE);
            DateFormat dateFormat =
                    android.text.format.DateFormat.getDateFormat(getApplicationContext());
            DateFormat timeFormat =
                    android.text.format.DateFormat.getTimeFormat(getApplicationContext());
            String currentDateStr = dateFormat.format(currentCalendar.getTime()) + " "
                    + timeFormat.format(currentCalendar.getTime());
            holder.getTimeTextView().setText(currentDateStr);



            // Add click listener to partner photo.
            if (senderId == partnerId) {
                holder.getProfileImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExtraConstants.startViewProfileActivity(MessagingActivity.this, partnerId);
                    }
                });
            }

            // Add click listener to show the time stamp.
            final TextView timeTextView = holder.getTimeTextView();
            holder.getMessageTextView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int visibility = timeTextView.getVisibility();
                    timeTextView.setVisibility((visibility == GONE) ? VISIBLE : GONE);
                    timeTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            linearLayoutManager.scrollToPosition(position);
                        }
                    });
                }
            });
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} for messages.
     */
    private static class MessagesViewHolder extends RecyclerView.ViewHolder {

        private final TextView dateTextView;
        private final ImageView profileImageView;
        private final TextView messageTextView;
        private final TextView timeTextView;

        private MessagesViewHolder(View itemView) {
            super(itemView);

            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            profileImageView = (ImageView) itemView.findViewById(R.id.profileImageView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
        }

        public TextView getDateTextView() {
            return dateTextView;
        }

        private ImageView getProfileImageView() {
            return profileImageView;
        }

        private TextView getMessageTextView() {
            return messageTextView;
        }

        public TextView getTimeTextView() {
            return timeTextView;
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
            QueryUtil.clearUnreadMessages(getContentResolver(), partnerId);
        }
    }
}
