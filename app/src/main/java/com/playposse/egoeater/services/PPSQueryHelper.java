package com.playposse.egoeater.services;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileTable;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.firebase.EgoEaterFirebaseMessagingService;
import com.playposse.egoeater.storage.ProfileParcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A little helper class to make {@link ContentProvider} queries. The
 * {@link PopulatePipelineService} is quite complicated. As much as possible should be factored out.
 */
public class PPSQueryHelper {
    private static final String LOG_TAG = PPSQueryHelper.class.getSimpleName();

    @NonNull
    static ArrayList<ContentProviderOperation> prepareOperationsToDeleteOldPipeline(
            ContentResolver contentResolver) {

        // Get current pipeline ids.
        Cursor oldPipelineCursor = contentResolver.query(
                EgoEaterContract.PipelineTable.CONTENT_URI,
                new String[]{EgoEaterContract.PipelineTable.ID_COLUMN},
                null,
                null,
                null);
        ArrayList<ContentProviderOperation> deleteOldPipelineOperations = new ArrayList<>();
        try {
            while (oldPipelineCursor.moveToNext()) {
                int rowId = oldPipelineCursor.getInt(0);
                deleteOldPipelineOperations.add(
                        ContentProviderOperation
                                .newDelete(EgoEaterContract.PipelineTable.CONTENT_URI)
                                .withSelection(
                                        EgoEaterContract.PipelineTable.ID_COLUMN + " = ?",
                                        new String[]{Integer.toString(rowId)})
                                .build());

            }
        } finally {
            Log.i(LOG_TAG, "rebuildPipeline: Found old pipeline size: "
                    + deleteOldPipelineOperations.size());
            oldPipelineCursor.close();
        }
        return deleteOldPipelineOperations;
    }

    static void deleteOldPairings(
            ContentResolver contentResolver,
            ArrayList<ContentProviderOperation> deleteOldPipelineOperations)
            throws RemoteException, OperationApplicationException {

        contentResolver.applyBatch(
                EgoEaterContract.AUTHORITY,
                deleteOldPipelineOperations);
        Log.i(LOG_TAG, "rebuildPipeline: Deleted old pipeline "
                + deleteOldPipelineOperations.size());
    }

    @NonNull
    static Map<Integer, List<Long>> getProfileIdsByRankStatus(
            ContentResolver contentResolver) {

        // Build where clause to filter incomplete profiles
        String where = ProfileTable.PROFILE_TEXT_COLUMN + " is not null "
                + " and " + ProfileTable.PROFILE_TEXT_COLUMN + " !='' "
                + " and " + ProfileTable.PHOTO_URL_0_COLUMN + " is not null"
                + " and " + ProfileTable.IS_ACTIVE_COLUMN
                + " and " + ProfileTable.AGE_COLUMN + " is not null";

        // Query the profile ranking status.
        Cursor cursor = contentResolver.query(
                ProfileTable.CONTENT_URI,
                new String[]{
                        ProfileTable.PROFILE_ID_COLUMN,
                        ProfileTable.WINS_LOSSES_SUM_COLUMN},
                where,
                null,
                ProfileTable.WINS_LOSSES_SUM_COLUMN + " desc");

        // Sort profiles by ranking status.
        Map<Integer, List<Long>> profileIdsByRankStatus = new HashMap<>();
        try {
            while (cursor.moveToNext()) {
                long profileId = cursor.getLong(0);
                int winLossesSum = cursor.getInt(1);
                if (!profileIdsByRankStatus.containsKey(winLossesSum)) {
                    profileIdsByRankStatus.put(winLossesSum, new ArrayList<Long>());
                }
                profileIdsByRankStatus.get(winLossesSum).add(profileId);
            }
        } finally {
            Log.i(LOG_TAG, "rebuildPipeline: Found ranked profiles: "
                    + profileIdsByRankStatus.size());
            cursor.close();
        }
        return profileIdsByRankStatus;
    }

    @NonNull
    static List<ContentValues> createPairings(
            ContentResolver contentResolver,
            Map<Integer, List<Long>> profileIdsByRankStatus) {

        List<ContentValues> contentValuesList = new ArrayList<>();
        ArrayList<Integer> rankStatusList = new ArrayList<>(profileIdsByRankStatus.keySet());
        Collections.sort(rankStatusList);
        Collections.reverse(rankStatusList); // TODO: Do this in one step.
        for (int rankStatus : rankStatusList) {
            List<Long> profileIds = profileIdsByRankStatus.get(rankStatus);
            while (profileIds.size() > 1) {
                Long profileId = profileIds.remove(0);
                for (int i = 0; i < profileIds.size(); i++) {
                    Long otherProfileId = profileIds.get(i);
                    boolean isAlreadyCompared = QueryUtil.isAlreadyCompared(
                            contentResolver,
                            profileId,
                            otherProfileId);
                    if (!isAlreadyCompared) {
                        profileIds.remove(i);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(
                                EgoEaterContract.PipelineTable.PROFILE_0_ID_COLUMN,
                                profileId);
                        contentValues.put(
                                EgoEaterContract.PipelineTable.PROFILE_1_ID_COLUMN,
                                otherProfileId);
                        contentValuesList.add(contentValues);
                        break;
                    }
                }
            }
        }
        return contentValuesList;
    }

    static void storePairings(
            ContentResolver contentResolver,
            List<ContentValues> contentValuesList) {

        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        contentResolver.bulkInsert(
                EgoEaterContract.PipelineTable.CONTENT_URI,
                contentValuesArray);
        Log.i(LOG_TAG, "rebuildPipeline: Created new pairings: " + contentValuesArray.length);
    }

    @Nullable
    static Integer getUnrankedProfilesCount(ContentResolver contentResolver) {
        // Count profiles that are cached and not compared yet.
        Cursor profileCursor = contentResolver.query(
                ProfileTable.CONTENT_URI,
                new String[]{ProfileTable.ID_COLUMN},
                ProfileTable.WINS_LOSSES_SUM_COLUMN + " = ?",
                new String[]{"0"},
                null);
        if (profileCursor == null) {
            Log.e(LOG_TAG, "loadProfilesIfNecessary: Failed to query profiles");
            return null;
        }

        final int rowCount;
        try {
            rowCount = profileCursor.getCount();
            Log.i(LOG_TAG, "loadProfilesIfNecessary: Found profiles that need to be ranked: "
                    + rowCount);
            return rowCount;
        } finally {
            profileCursor.close();
        }
    }

    @Nullable
    static List<Long> getUnloadedProfileIds(
            ContentResolver contentResolver,
            int limit) {

        // Find unloaded profile ids.
        Cursor profileIdCursor = contentResolver.query(
                EgoEaterContract.ProfileIdTable.CONTENT_URI,
                new String[]{EgoEaterContract.ProfileIdTable.PROFILE_ID_COLUMN},
                "not " + EgoEaterContract.ProfileIdTable.IS_PROFILE_LOADED_COLUMN,
                null,
                EgoEaterContract.ProfileIdTable.ID_COLUMN + " asc");
        if (profileIdCursor == null) {
            Log.e(LOG_TAG, "loadProfilesIfNecessary: Failed to query for unloaded profile ids");
            return null;
        }
        final List<Long> profileIds = new ArrayList<>();
        try {
            while ((profileIdCursor.moveToNext())
                    && (profileIds.size() < limit)) {
                profileIds.add(profileIdCursor.getLong(0));
            }
        } finally {
            Log.i(LOG_TAG, "loadProfilesIfNecessary: Trying to load more profiles: "
                    + profileIds.size());
            profileIdCursor.close();
        }
        return profileIds;
    }

    static void saveProfiles(ContentResolver contentResolver, List<ProfileBean> profiles)
            throws RemoteException, OperationApplicationException {

        List<ContentValues> contentValuesList = new ArrayList<>(profiles.size());

        for (ProfileBean profile : profiles) {
            ContentValues contentValues = ProfileParcelable.toContentValues(profile);
            contentValuesList.add(contentValues);
            EgoEaterFirebaseMessagingService.subscribeToProfileUpdates(profile.getUserId());
        }

        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        contentResolver.bulkInsert(
                ProfileTable.CONTENT_URI,
                contentValuesArray);
        Log.i(LOG_TAG, "saveProfiles: Saved profiles to the device: " + contentValuesArray.length);

        markProfileIdsAsDownloaded(contentResolver, profiles);

        // Delete duplicate profiles just in case -> SHOULDN'T BE NEEDED
//        contentResolver.delete(EgoEaterContract.DeleteDuplicateProfiles.CONTENT_URI, null, null);
    }

    private static void markProfileIdsAsDownloaded(
            ContentResolver contentResolver,
            List<ProfileBean> profiles)
            throws RemoteException, OperationApplicationException {

        ArrayList<ContentProviderOperation> updateOperations = new ArrayList<>();
        for (ProfileBean profileBean : profiles) {
            String profileId = Long.toString(profileBean.getUserId());
            ContentProviderOperation updateOperation =
                    ContentProviderOperation.newUpdate(EgoEaterContract.ProfileIdTable.CONTENT_URI)
                            .withValue(EgoEaterContract.ProfileIdTable.IS_PROFILE_LOADED_COLUMN, true)
                            .withSelection(
                                    EgoEaterContract.ProfileIdTable.PROFILE_ID_COLUMN + " = ?",
                                    new String[]{profileId})
                            .build();
            updateOperations.add(updateOperation);
        }

        contentResolver.applyBatch(EgoEaterContract.AUTHORITY, updateOperations);
        Log.i(LOG_TAG, "markProfileIdsAsDownloaded: Marked profile ids as downloaded: "
                + updateOperations.size());
    }

    static List<Long> saveProfileIds(
            ContentResolver contentResolver,
            List<Long> newProfileIds,
            List<Long> existingProfileIds)
            throws InterruptedException {

        if (existingProfileIds == null) {
            return new ArrayList<>();
        }

        // Store new profile ids.
        List<Long> savedProfileIds = new ArrayList<>();
        ArrayList<ContentValues> contentValuesList = new ArrayList<>();
        for (Long profileId : newProfileIds) {
            if (!existingProfileIds.contains(profileId)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(EgoEaterContract.ProfileIdTable.PROFILE_ID_COLUMN, profileId);
                contentValuesList.add(contentValues);

                savedProfileIds.add(profileId);
            }
        }

        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        contentResolver.bulkInsert(
                EgoEaterContract.ProfileIdTable.CONTENT_URI,
                contentValuesArray);

        return savedProfileIds;
    }

    @Nullable
    static List<Long> getExistingProfileIds(ContentResolver contentResolver) {
        // Get existing profile ids.
        Cursor cursor = contentResolver.query(
                EgoEaterContract.ProfileIdTable.CONTENT_URI,
                new String[]{EgoEaterContract.ProfileIdTable.PROFILE_ID_COLUMN},
                null,
                null,
                null);
        if (cursor == null) {
            Log.e(LOG_TAG, "saveProfileIds: Failed to get profile ids.");
            return null;
        }

        List<Long> existingProfileIds = new ArrayList<>(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                existingProfileIds.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }
        return existingProfileIds;
    }
}
