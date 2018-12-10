package com.playposse.egoeater.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.Nullable;
import android.util.Log;

import com.playposse.egoeater.clientactions.ReportRankingClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MaxMessageIndexQuery;
import com.playposse.egoeater.contentprovider.EgoEaterContract.MessageTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.RatingTable;
import com.playposse.egoeater.services.PopulatePipelineService;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.PairingParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.SmartCursor;

/**
 * A collection of methods to query {@link EgoEaterContentProvider} for information.
 */
public final class QueryUtil {

    private static final String LOG_TAG = QueryUtil.class.getSimpleName();

    private QueryUtil() {
    }

    @Nullable
    public static PairingParcelable getNextPairing(
            Context context,
            boolean potentialTriggerRebuild) {

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                PipelineTable.CONTENT_URI,
                PipelineTable.COLUMN_NAMES,
                null, // PipelineTable.ARE_PHOTOS_CACHED_COLUMN + " = 1",
                null,
                PipelineTable.ID_COLUMN + " asc");

        try {
            if ((cursor == null)) {
                return null;
            }
            SmartCursor smartCursor = new SmartCursor(cursor, PipelineTable.COLUMN_NAMES);

            while (cursor.moveToNext()) {
                PairingParcelable pairing = new PairingParcelable(smartCursor);

                long profileId0 = pairing.getProfileId0();
                long profileId1 = pairing.getProfileId1();
                if (!isAlreadyCompared(contentResolver, profileId0, profileId1)) {
                    return pairing;
                } else {
                    // If it is already compared, let's delete it.
                    deletePipelineEntry(contentResolver, pairing.getPairingId());
                }
            }
            return null;
        } finally {
            if ((cursor != null) && potentialTriggerRebuild && !cursor.moveToNext()) {
                // Reached the end of the pipeline. Trigger rebuilding it.
                PopulatePipelineService.startService(
                        context,
                        PipelineLogTable.RATING_ACTIVITY_TRIGGER);
            }
        }
    }

    @Nullable
    public static ProfileParcelable getProfileByProfileId(
            ContentResolver contentResolver,
            long profileId) {

        Cursor cursor = contentResolver.query(
                ProfileTable.CONTENT_URI,
                ProfileTable.COLUMN_NAMES,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(profileId)},
                null);

        if ((cursor != null) && (cursor.moveToNext())) {
            SmartCursor smartCursor = new SmartCursor(cursor, ProfileTable.COLUMN_NAMES);
            return new ProfileParcelable(smartCursor);
        } else {
            return null;
        }
    }

    public static void saveRating(
            Context context,
            int pairingId,
            long winnerId,
            long loserId) {

        long start = System.currentTimeMillis();
        ContentResolver contentResolver = context.getContentResolver();

        // Remove the pairing from the pipeline.
        deletePipelineEntry(contentResolver, pairingId);

        // Store the rating.
        ContentValues ratingContentValues = new ContentValues();
        ratingContentValues.put(RatingTable.WINNER_ID_COLUMN, winnerId);
        ratingContentValues.put(RatingTable.LOSER_ID_COLUMN, loserId);
        contentResolver.insert(RatingTable.CONTENT_URI, ratingContentValues);

        // Update the winner profile.
        ProfileParcelable winner = getProfileByProfileId(contentResolver, winnerId);
        ContentValues winnerContentValues = new ContentValues();
        winnerContentValues.put(ProfileTable.WINS_COLUMN, winner.getWins() + 1);
        winnerContentValues.put(ProfileTable.WINS_LOSSES_SUM_COLUMN, winner.getWinsLossesSum() + 1);
        contentResolver.update(
                ProfileTable.CONTENT_URI,
                winnerContentValues,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(winnerId)});
        Log.i(LOG_TAG, "saveRating: Updated winner: " + winner.getFirstName() + " before "
                + winner.getWins() + " " + winner.getLosses() + " " + winner.getWinsLossesSum()
                + " after " + winnerContentValues.get(ProfileTable.WINS_COLUMN)
                + " " + winnerContentValues.get(ProfileTable.WINS_LOSSES_SUM_COLUMN));

        // Update the loser profile.
        ProfileParcelable loser = getProfileByProfileId(contentResolver, loserId);
        ContentValues loserContentValues = new ContentValues();
        loserContentValues.put(ProfileTable.LOSSES_COLUMN, loser.getLosses() + 1);
        loserContentValues.put(ProfileTable.WINS_LOSSES_SUM_COLUMN, loser.getWinsLossesSum() - 1);
        contentResolver.update(
                ProfileTable.CONTENT_URI,
                loserContentValues,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(loserId)});
        Log.i(LOG_TAG, "saveRating: Updated loser: " + loser.getFirstName() + " before "
                + loser.getWins() + " " + loser.getLosses() + " " + loser.getWinsLossesSum()
                + " after " + loserContentValues.get(ProfileTable.LOSSES_COLUMN)
                + " " + loserContentValues.get(ProfileTable.WINS_LOSSES_SUM_COLUMN));

        // Report the result to the cloud.
        new ReportRankingClientAction(context, winnerId, loserId).execute();

        // Log the time duration.
        long end = System.currentTimeMillis();
        Log.i(LOG_TAG, "saveRating: Took " + (end - start) + "ms");
    }

    private static void deletePipelineEntry(ContentResolver contentResolver, int pairingId) {
        contentResolver.delete(
                PipelineTable.CONTENT_URI,
                PipelineTable.ID_COLUMN + " = ?",
                new String[]{Integer.toString(pairingId)});
    }

    /**
     * Checks if the two profiles are already compared.
     */
    public static boolean isAlreadyCompared(
            ContentResolver contentResolver,
            Long profileId0,
            Long profileId1) {

        String p0Col = RatingTable.WINNER_ID_COLUMN;
        String p1Col = RatingTable.LOSER_ID_COLUMN;
        String where = String.format(
                "((%1$s = ?) and (%2$s = ?)) or ((%3$s = ?) and (%4$s = ?))",
                p0Col,
                p1Col,
                p1Col,
                p0Col);

        Cursor cursor = contentResolver.query(
                RatingTable.CONTENT_URI,
                new String[]{RatingTable.ID_COLUMN},
                where,
                new String[]{
                        Long.toString(profileId0),
                        Long.toString(profileId1),
                        Long.toString(profileId0),
                        Long.toString(profileId1)
                },
                null);
        try {
            Log.i(LOG_TAG, "isAlreadyCompared: Check pairing: " + profileId0 + " " + profileId1
                    + " " + (cursor.getCount() > 0));
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }

    public static boolean hasMatches(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(
                MatchTable.CONTENT_URI,
                MatchTable.COLUMN_NAMES,
                null,
                null,
                null);
        try {
            return (cursor != null) && (cursor.getCount() > 0);
        } finally {
            cursor.close();
        }
    }

    public static ProfileParcelable getProfileById(
            ContentResolver contentResolver,
            long profileId) {

        Cursor cursor = contentResolver.query(
                ProfileTable.CONTENT_URI,
                ProfileTable.COLUMN_NAMES,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(profileId)},
                null);

        try {
            if ((cursor != null) && cursor.moveToNext()) {
                SmartCursor smartCursor = new SmartCursor(cursor, ProfileTable.COLUMN_NAMES);
                return new ProfileParcelable(smartCursor);
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Integer getMaxMessageIndex(
            ContentResolver contentResolver,
            long profileAId,
            long profileBId) {

        Cursor cursor = contentResolver.query(
                MaxMessageIndexQuery.CONTENT_URI,
                null,
                null,
                new String[]{Long.toString(profileAId), Long.toString(profileBId)},
                null);

        try {
            if ((cursor == null) || (!cursor.moveToNext())) {
                return null;
            } else {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Long getLastMessageCreated(
            ContentResolver contentResolver,
            long profileId,
            long partnerId) {

        String profileStr = Long.toString(profileId);
        String partnerStr = Long.toString(partnerId);

        Cursor cursor = contentResolver.query(
                MessageTable.CONTENT_URI,
                new String[]{MessageTable.CREATED_COLUMN},
                "((sender_profile_id = ?) and (recipient_profile_id = ?)) " +
                        "or ((sender_profile_id = ?) and (recipient_profile_id = ?))",
                new String[]{
                        profileStr,
                        partnerStr,
                        partnerStr,
                        profileStr},
                MessageTable.MESSAGE_INDEX_COLUMN + " desc");

        try {
            if ((cursor != null) && (cursor.moveToNext())) {
                return cursor.getLong(0);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Marks a match as locked. This happens when one of the users sends the first message.
     */
    public static void lockMatch(ContentResolver contentResolver, long profileId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MatchTable.IS_LOCKED_COLUMN, true);

        int rowCount = contentResolver.update(
                MatchTable.CONTENT_URI,
                contentValues,
                MatchTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(profileId)});

        if (rowCount != 1) {
            Log.w(LOG_TAG, "lockMatch: Tried to lock a match and failed: " + rowCount);
        }
    }

    public static void markMessageRead(
            Context context,
            long recipientProfileId,
            int messageIndex) {

        ContentResolver contentResolver = context.getContentResolver();
        Long senderProfileId = EgoEaterPreferences.getUser(context).getUserId();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageTable.IS_RECEIVED_COLUMN, true);

        int rowCount = contentResolver.update(
                MessageTable.CONTENT_URI,
                contentValues,
                MessageTable.SENDER_PROFILE_ID_COLUMN + " = ? and "
                        + MessageTable.RECIPIENT_PROFILE_ID_COLUMN + " = ? and "
                        + MessageTable.MESSAGE_INDEX_COLUMN + " = ?",
                new String[]{
                        Long.toString(senderProfileId),
                        Long.toString(recipientProfileId),
                        Integer.toString(messageIndex)});

        Log.w(LOG_TAG, "markMessageRead: Failed to mark a message as read. Received an unexpected" +
                " row count: " + rowCount);
    }

    public static void incrementUnreadMessages(
            ContentResolver contentResolver,
            long partnerId) {

        int unreadMessagesCount = getUnreadMessagesCount(contentResolver, partnerId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MatchTable.HAS_NEW_MESSAGE, true);
        contentValues.put(MatchTable.UNREAD_MESSAGES_COUNT, unreadMessagesCount + 1);

        int rowCount = contentResolver.update(
                MatchTable.CONTENT_URI,
                contentValues,
                MatchTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(partnerId)});

        if (rowCount != 1) {
            Log.w(LOG_TAG, "lockMatch: Tried to mark match as having a new message but failed: "
                    + rowCount);
        }
    }

    private static int getUnreadMessagesCount(ContentResolver contentResolver, long partnerId) {
        Cursor cursor = contentResolver.query(
                MatchTable.CONTENT_URI,
                new String[]{MatchTable.UNREAD_MESSAGES_COUNT},
                MatchTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(partnerId)},
                null);
        try {
            if ((cursor != null) && (cursor.moveToNext())) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    public static void clearUnreadMessages(ContentResolver contentResolver, long partnerId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MatchTable.HAS_NEW_MESSAGE, false);
        contentValues.put(MatchTable.UNREAD_MESSAGES_COUNT, 0);

        int rowCount = contentResolver.update(
                MatchTable.CONTENT_URI,
                contentValues,
                MatchTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(partnerId)});

        if (rowCount != 1) {
            Log.w(LOG_TAG, "lockMatch: Tried to mark match as having a new message but failed: "
                    + rowCount);
        }
    }
}
