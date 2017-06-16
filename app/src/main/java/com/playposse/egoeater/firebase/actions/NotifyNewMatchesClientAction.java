package com.playposse.egoeater.firebase.actions;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.activity.CurrentActivity;
import com.playposse.egoeater.activity.MatchesActivity;
import com.playposse.egoeater.backend.egoEaterApi.model.MatchBean;
import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.clientactions.GetMatchesClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchAndProfileQuery;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.MatchParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.DatabaseDumper;
import com.playposse.egoeater.util.NotificationUtil;
import com.playposse.egoeater.util.NotificationUtil.NotificationType;
import com.playposse.egoeater.util.SmartCursor;

import java.util.ArrayList;
import java.util.List;

import static com.playposse.egoeater.contentprovider.EgoEaterContract.AUTHORITY;

/**
 * Handles a Firebase notification that the cloud has new matches.
 */
public class NotifyNewMatchesClientAction extends FirebaseClientAction {

    private static final String LOG_TAG = NotifyNewMatchesClientAction.class.getSimpleName();

    private enum UpdateState {
        updatedMatches, // There is at least one new match.
        onlyLostMatches, // The user only lost matches.
        noChange, // The list of matches hasn't changed.
        noMatchYet, // Special case: The user doesn't have any matches yet.
        notLoggedIn, // Special case: The user is not logged in. We couldn't determine if there are new matches.
        error,
    }

    public NotifyNewMatchesClientAction(RemoteMessage remoteMessage) {
        super(remoteMessage);
    }

    @Override
    protected void execute(RemoteMessage remoteMessage) {
        long start = System.currentTimeMillis();
        Log.i(LOG_TAG, "execute: Received notifcation of new matches.");
        UpdateState updateState = processNewMatches();

        sendNotification(updateState);
        long end = System.currentTimeMillis();
        DatabaseDumper.dumpTables(new MainDatabaseHelper(getApplicationContext()));
        Log.i(LOG_TAG, "execute: Processed new match notification in " + (end - start) + "ms.");
    }

    private UpdateState processNewMatches() {
        if (EgoEaterPreferences.getSessionId(getApplicationContext()) == null) {
            return UpdateState.notLoggedIn;
        }

        try {
            List<MatchParcelable> existingMatches = getExistingMatches();
            List<MatchBean> newMatches = getNewMatches();

            // Sort matches.
            List<MatchParcelable> deleteMatches = new ArrayList<>(existingMatches);
            List<MatchBean> insertMatches = new ArrayList<>();
            List<MatchBean> updateMatches = new ArrayList<>();

            for (MatchBean matchBean : newMatches) {
                Long partnerId = matchBean.getOtherProfileBean().getUserId();
                int existingMatchIndex = getIndexByPartnerId(existingMatches, partnerId);
                if (existingMatchIndex >= 0) {
                    updateMatches.add(matchBean);
                } else {
                    insertMatches.add(matchBean);
                }

                int deleteMatchIndex = getIndexByPartnerId(deleteMatches, partnerId);
                if (deleteMatchIndex >= 0) {
                    deleteMatches.remove(deleteMatchIndex);
                }
            }

            // Update local database.
            updateProfile(updateMatches);
            insert(insertMatches);
            delete(deleteMatches);

            // Determine update state.
            boolean hasExisting = existingMatches.size() > 0;
            boolean hasInserts = insertMatches.size() > 0;
            boolean hasUpdates = updateMatches.size() > 0;
            boolean hasDeletes = deleteMatches.size() > 0;

            Log.i(LOG_TAG, "processNewMatches: hasExisting: " + hasExisting);
            Log.i(LOG_TAG, "processNewMatches: hasInserts: " + hasInserts);
            Log.i(LOG_TAG, "processNewMatches: hasUpdates: " + hasUpdates);
            Log.i(LOG_TAG, "processNewMatches: hasDeletes: " + hasDeletes);

            if (hasDeletes && !hasInserts && !hasUpdates) {
                return UpdateState.onlyLostMatches;
            } else if (hasDeletes || hasInserts || hasUpdates) {
                return UpdateState.updatedMatches;
            } else if (hasExisting) {
                return UpdateState.noChange;
            } else {
                return UpdateState.noMatchYet;
            }
        } catch (InterruptedException | RemoteException | OperationApplicationException ex) {
            Log.e(LOG_TAG, "execute: Failed to handle Firebase message about new matches.", ex);
            return UpdateState.error;
        }
    }

    private int getIndexByMatchId(List<MatchParcelable> existingMatches, long matchId) {
        for (int i = 0; i < existingMatches.size(); i++) {
            MatchParcelable matchParcelable = existingMatches.get(i);
            if (matchParcelable.getCloudMatchId() == matchId) {
                return i;
            }
        }
        return -1;
    }

    private int getIndexByPartnerId(List<MatchParcelable> existingMatches, long partnerId) {
        for (int i = 0; i < existingMatches.size(); i++) {
            MatchParcelable matchParcelable = existingMatches.get(i);
            if (matchParcelable.getOtherProfile().getProfileId() == partnerId) {
                return i;
            }
        }
        return -1;
    }

    private List<MatchBean> getNewMatches() throws InterruptedException {
        return GetMatchesClientAction.getBlocking(getApplicationContext());
    }

    private List<MatchParcelable> getExistingMatches() {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(
                MatchAndProfileQuery.CONTENT_URI,
                MatchAndProfileQuery.COLUMN_NAMES,
                null,
                null,
                null);

        try {
            List<MatchParcelable> matches = new ArrayList<>();
            SmartCursor smartCursor = new SmartCursor(cursor, MatchAndProfileQuery.COLUMN_NAMES);
            while (cursor.moveToNext()) {
                matches.add(new MatchParcelable(smartCursor));
            }
            return matches;
        } finally {
            cursor.close();
        }
    }

    private void updateProfile(List<MatchBean> matches)
            throws RemoteException, OperationApplicationException {

        if (matches.size() == 0) {
            return;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>(matches.size());
        for (MatchBean match : matches) {
            ProfileBean otherProfile = match.getOtherProfileBean();
            ContentValues contentValues = ProfileParcelable.toContentValues(otherProfile);

            ContentProviderOperation operation =
                    ContentProviderOperation.newUpdate(ProfileTable.CONTENT_URI)
                            .withValues(contentValues)
                            .withSelection(
                                    ProfileTable.PROFILE_ID_COLUMN + " = ?",
                                    new String[]{otherProfile.getUserId().toString()})
                            .build();
            operations.add(operation);
        }

        getApplicationContext().getContentResolver().applyBatch(AUTHORITY, operations);
    }

    private void insert(List<MatchBean> matches) {
        if (matches.size() == 0) {
            return;
        }

        // Create inserts.
        List<ContentValues> contentValuesList = new ArrayList<>(matches.size());
        for (MatchBean match : matches) {
            ProfileBean otherProfile = match.getOtherProfileBean();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MatchTable.MATCH_ID_COLUMN, match.getMatchId());
            contentValues.put(MatchTable.PROFILE_ID_COLUMN, otherProfile.getUserId());
            contentValues.put(MatchTable.IS_LOCKED_COLUMN, match.getLocked());
            contentValuesList.add(contentValues);
        }

        // Execute inserts in bulk.
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        contentResolver.bulkInsert(MatchTable.CONTENT_URI, contentValuesArray);
    }

    private void delete(List<MatchParcelable> matches)
            throws RemoteException, OperationApplicationException {

        if (matches.size() == 0) {
            return;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>(matches.size());
        for (MatchParcelable match : matches) {
            ContentProviderOperation operation =
                    ContentProviderOperation.newDelete(MatchTable.CONTENT_URI)
                            .withSelection(
                                    MatchTable.ID_COLUMN + " = ?",
                                    new String[]{Integer.toString(match.getLocalMatchId())})
                            .build();
            operations.add(operation);
        }

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.applyBatch(AUTHORITY, operations);
    }

    /**
     * Sends the user a notification if it makes sense.
     */
    private void sendNotification(UpdateState updateState) {
        if (MatchesActivity.class.equals(CurrentActivity.getCurrentActivity())) {
            // Already on matches Activity. Skip showing the notification.
            return;
        }

        Context context = getApplicationContext();
        switch (updateState) {
            case updatedMatches:
                NotificationUtil.sendNotification(context, NotificationType.UpdatedMatches);
                break;
            case onlyLostMatches:
                NotificationUtil.sendNotification(context, NotificationType.OnlyLostMatches);
                break;
            case notLoggedIn:
                NotificationUtil.sendNotification(context, NotificationType.NotLoggedIn);
                break;
            case noChange:
                // Ignore.
                break;
            case noMatchYet:
                // Ignore.
                break;
            case error:
                // Ignore.
                break;
        }
    }
}
