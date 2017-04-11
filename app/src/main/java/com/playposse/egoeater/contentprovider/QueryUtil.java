package com.playposse.egoeater.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.Rating;
import android.support.annotation.Nullable;

import com.playposse.egoeater.clientactions.ReportRankingClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.RatingTable;
import com.playposse.egoeater.storage.PairingParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.SmartCursor;

/**
 * A collection of methods to query {@link EgoEaterContentProvider} for information.
 */
public final class QueryUtil {

    private QueryUtil() {
    }

    @Nullable
    public static PairingParcelable getNextPairing(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(
                PipelineTable.CONTENT_URI,
                PipelineTable.COLUMN_NAMES,
                null, // PipelineTable.ARE_PHOTOS_CACHED_COLUMN + " = 1",
                null,
                PipelineTable.ID_COLUMN + " asc");

        if ((cursor != null) && (cursor.moveToNext())) {
            SmartCursor smartCursor = new SmartCursor(cursor, PipelineTable.COLUMN_NAMES);
            return new PairingParcelable(smartCursor);
        } else {
            return null;
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
        ContentValues ratingContentvalues = new ContentValues();
        ratingContentvalues.put(RatingTable.WINNER_ID_COLUMN, winnerId);
        ratingContentvalues.put(RatingTable.LOSER_ID_COLUMN, loserId);
        contentResolver.insert(RatingTable.CONTENT_URI, ratingContentvalues);

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

        // Update the loser profile.
        ProfileParcelable loser = getProfileByProfileId(contentResolver, winnerId);
        ContentValues loserContentValues = new ContentValues();
        loserContentValues.put(ProfileTable.LOSSES_COLUMN, loser.getLosses() + 1);
        loserContentValues.put(ProfileTable.WINS_LOSSES_SUM_COLUMN, loser.getWinsLossesSum() - 1);
        contentResolver.update(
                ProfileTable.CONTENT_URI,
                loserContentValues,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(loserId)});

        // Report the result to the cloud.
        new ReportRankingClientAction(context, winnerId, loserId);
    }
}
