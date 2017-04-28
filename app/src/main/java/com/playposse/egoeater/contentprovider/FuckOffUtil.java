package com.playposse.egoeater.contentprovider;

import android.content.ContentResolver;
import android.content.Context;

import com.playposse.egoeater.contentprovider.EgoEaterContract.MatchTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.RatingTable;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A collection of code around the fuck off functionality.
 */
public class FuckOffUtil {

    public static void eraseUserLocally(ContentResolver contentResolver, long partnerId) {
        // Erase from matches table.
        String partnerIdStr = Long.toString(partnerId);
        contentResolver.delete(
                MatchTable.CONTENT_URI,
                MatchTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{partnerIdStr});

        // Erase from profile table.
        contentResolver.delete(
                ProfileTable.CONTENT_URI,
                ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{partnerIdStr});

        // Erase from profile id table.
        contentResolver.delete(
                ProfileIdTable.CONTENT_URI,
                ProfileIdTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{partnerIdStr});

        // Erase from rating table.
        contentResolver.delete(
                RatingTable.CONTENT_URI,
                RatingTable.WINNER_ID_COLUMN + " = ? or " + RatingTable.LOSER_ID_COLUMN + " = ?",
                new String[]{partnerIdStr, partnerIdStr});

        // Erase from pipelin table.
        contentResolver.delete(
                PipelineTable.CONTENT_URI,
                PipelineTable.PROFILE_0_ID_COLUMN + " = ? or "
                        + PipelineTable.PROFILE_1_ID_COLUMN + " = ?",
                new String[]{partnerIdStr, partnerIdStr});
    }

    public static boolean isUserBanned(Context context, long partnerId) {
        return EgoEaterPreferences.getFuckedOffUsers(context).contains(partnerId);
    }
}
