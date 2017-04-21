package com.playposse.egoeater.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.Rating;
import android.support.annotation.Nullable;
import android.util.Log;

import com.playposse.egoeater.clientactions.ReportRankingClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.RatingTable;
import com.playposse.egoeater.services.PopulatePipelineService;
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
            @Nullable PairingParcelable lastPairing,
            boolean potentialTriggerRebuild) {

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                PipelineTable.CONTENT_URI,
                PipelineTable.COLUMN_NAMES,
                null, // PipelineTable.ARE_PHOTOS_CACHED_COLUMN + " = 1",
                null,
                PipelineTable.ID_COLUMN + " asc");

        try {
            if ((cursor == null) || !cursor.moveToNext()) {
                return null;
            }

            SmartCursor smartCursor = new SmartCursor(cursor, PipelineTable.COLUMN_NAMES);
            PairingParcelable pairing = new PairingParcelable(smartCursor);

            // Check if perhaps we got the same pairing because the pipeline rebuilt.
            if ((lastPairing != null)
                    && (lastPairing.getProfileId0() == pairing.getProfileId0())
                    && (lastPairing.getProfileId1() == pairing.getProfileId1())) {

                if (!cursor.moveToNext()) {
                    return null;
                }

                // Get the second next pairing.
                pairing = new PairingParcelable(smartCursor);
            }

            return pairing;
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

        ContentResolver contentResolver = context.getContentResolver();

        // Remove the pairing from the pipeline.
        contentResolver.delete(
                PipelineTable.CONTENT_URI,
                PipelineTable.ID_COLUMN + " = ?",
                new String[]{Integer.toString(pairingId)});

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
        Log.i(LOG_TAG, "saveRating: Updated winner: " + winner.getName() + " before "
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
        Log.i(LOG_TAG, "saveRating: Updated loser: " + loser.getName() + " before "
                + loser.getWins() + " " + loser.getLosses() + " " + loser.getWinsLossesSum()
                + " after " + loserContentValues.get(ProfileTable.LOSSES_COLUMN)
                + " " + loserContentValues.get(ProfileTable.WINS_LOSSES_SUM_COLUMN));

        // Report the result to the cloud.
        new ReportRankingClientAction(context, winnerId, loserId).execute();
    }
}
