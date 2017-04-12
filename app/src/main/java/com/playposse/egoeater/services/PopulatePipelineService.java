package com.playposse.egoeater.services;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.playposse.egoeater.backend.egoEaterApi.EgoEaterApi;
import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.GetProfileIdsByDistanceClientAction;
import com.playposse.egoeater.clientactions.GetProfilesByIdClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.DeleteDuplicateProfiles;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.DatabaseDumper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} that downloads profiles, determines good pairs for rankings, pre-caches
 * profile photos.
 */
public class PopulatePipelineService extends IntentService {

    private static final String LOG_TAG = PopulatePipelineService.class.getSimpleName();

    private static final String SERVICE_NAME = "PopulatePipelineService";

    /**
     * The minimum number of pipelines that have to be in the pipeline.
     */
    private static final int MIN_PIPELINE_SIZE = 10;

    /**
     * The minimum number of profiles that have to be cached and unused.
     */
    private static final int MIN_PROFILES_CACHED = 100;

    /**
     * The maximum radius in miles that profiles are searched for.
     */
    private static final int MAX_SEARCH_RADIUS = 15;

    /**
     * The maximum profiles that have their photos pre-loaded.
     */
    private static final int MAX_PROFILE_PHOTO_CACHE = 5;

    private boolean isActive = false;

    public PopulatePipelineService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (isActive) {
            return;
        }
        isActive = true;

        loadProfilesIfNecessary(new Runnable() {
            @Override
            public void run() {
                try {
                    rebuildPipeline();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException ex) {
                    Log.e(LOG_TAG, "run: Failed to rebuild pipeline.", ex);
                }
                cacheProfilePhotos();
                DatabaseDumper.dumpTables(new MainDatabaseHelper(getApplicationContext()));
            }
        });

        isActive = false;
    }

    private int rebuildPipeline() throws RemoteException, OperationApplicationException {
        // Get current pipeline ids.
        Cursor oldPipelineCursor = getContentResolver().query(
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


        // Query the profile ranking status.
        Cursor cursor = getContentResolver().query(
                EgoEaterContract.ProfileTable.CONTENT_URI,
                new String[]{
                        EgoEaterContract.ProfileTable.PROFILE_ID_COLUMN,
                        EgoEaterContract.ProfileTable.WINS_LOSSES_SUM_COLUMN},
                null,
                null,
                EgoEaterContract.ProfileTable.WINS_LOSSES_SUM_COLUMN + " desc");

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

        // Create pairings
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
                    if (!isAlreadyCompared(profileId, otherProfileId)) {
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

        // Store pairings
        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        getContentResolver().bulkInsert(
                EgoEaterContract.PipelineTable.CONTENT_URI,
                contentValuesArray);
        Log.i(LOG_TAG, "rebuildPipeline: Created new pairings: " + contentValuesArray.length);

        // Delete old pairings
        getContentResolver().applyBatch(
                EgoEaterContract.AUTHORITY,
                deleteOldPipelineOperations);
        Log.i(LOG_TAG, "rebuildPipeline: Deleted old pipeline "
                + deleteOldPipelineOperations.size());

        return contentValuesList.size();
    }

    private void loadProfilesIfNecessary(final Runnable runnable) {
        // Count profiles that are cached and not compared yet.
        Cursor profileCursor = getContentResolver().query(
                EgoEaterContract.ProfileTable.CONTENT_URI,
                new String[]{EgoEaterContract.ProfileTable.ID_COLUMN},
                EgoEaterContract.ProfileTable.WINS_LOSSES_SUM_COLUMN + " = ?",
                new String[]{"0"},
                null);
        if (profileCursor == null) {
            Log.e(LOG_TAG, "loadProfilesIfNecessary: Failed to query profiles");
            return;
        }

        final int rowCount;
        try {
            rowCount = profileCursor.getCount();
            Log.i(LOG_TAG, "loadProfilesIfNecessary: Found profiles that need to be ranked: "
                    + rowCount);
        } finally {
            profileCursor.close();
        }

        // Find out if we need more profiles.
        if (rowCount > MIN_PROFILES_CACHED) {
            Log.i(LOG_TAG, "loadProfilesIfNecessary: We found enough unranked profiles. Skip!");
            return;
        }

        // Find unloaded profile ids.
        Cursor profileIdCursor = getContentResolver().query(
                ProfileIdTable.CONTENT_URI,
                new String[]{ProfileIdTable.PROFILE_ID_COLUMN},
                "not " + ProfileIdTable.IS_PROFILE_LOADED_COLUMN,
                null,
                ProfileIdTable.ID_COLUMN + " asc");
        if (profileIdCursor == null) {
            Log.e(LOG_TAG, "loadProfilesIfNecessary: Failed to query for unloaded profile ids");
            return;
        }
        final List<Long> profileIds = new ArrayList<>();
        try {
            while ((profileIdCursor.moveToNext())
                    && (profileIds.size() < MIN_PROFILES_CACHED - rowCount)) {
                profileIds.add(profileIdCursor.getLong(0));
            }
        } finally {
            Log.i(LOG_TAG, "loadProfilesIfNecessary: Trying to load more profiles: "
                    + profileIds.size());
            profileIdCursor.close();
        }

        // Load more profiles
        new GetProfilesByIdClientAction(
                getApplicationContext(),
                new ApiClientAction.Callback<List<ProfileBean>>() {
                    @Override
                    public void onResult(List<ProfileBean> profiles) {
                        if (profiles != null) {
                            Log.i(LOG_TAG, "onResult: Received profiles from the cloud: "
                                    + profiles.size());
                        } else {
                            Log.i(LOG_TAG, "onResult: Got null from GetProfilesByIdClientAction");
                        }
                        try {
                            saveProfiles(profiles);
                        } catch (RemoteException | OperationApplicationException ex) {
                            Log.e(LOG_TAG, "onResult: Failed to save profiles", ex);
                        }

                        // Load additional profile ids if needed.
                        if (rowCount + profileIds.size() > MIN_PROFILES_CACHED) {
                            return;
                        }
                        int neededProfileIdCount =
                                MIN_PROFILES_CACHED - rowCount + profileIds.size();
                        loadProfileIds(neededProfileIdCount, runnable);
                    }
                },
                profileIds).execute();

    }

    private void saveProfiles(List<ProfileBean> profiles)
            throws RemoteException, OperationApplicationException {

        List<ContentValues> contentValuesList = new ArrayList<>(profiles.size());

        for (ProfileBean profile : profiles) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(EgoEaterContract.ProfileTable.PROFILE_ID_COLUMN, profile.getUserId());
            contentValues.put(EgoEaterContract.ProfileTable.FIRST_NAME_COLUMN, profile.getFirstName());
            contentValues.put(EgoEaterContract.ProfileTable.LAST_NAME_COLUMN, profile.getLastName());
            contentValues.put(EgoEaterContract.ProfileTable.NAME_COLUMN, profile.getName());
            contentValues.put(EgoEaterContract.ProfileTable.PROFILE_TEXT_COLUMN, profile.getProfileText());
            contentValues.put(EgoEaterContract.ProfileTable.DISTANCE_COLUMN, profile.getDistance());
            contentValues.put(EgoEaterContract.ProfileTable.CITY_COLUMN, profile.getCity());
            contentValues.put(EgoEaterContract.ProfileTable.STATE_COLUMN, profile.getState());
            contentValues.put(EgoEaterContract.ProfileTable.COUNTRY_COLUMN, profile.getCountry());
            contentValues.put(EgoEaterContract.ProfileTable.AGE_COLUMN, profile.getAge());
            contentValues.put(EgoEaterContract.ProfileTable.GENDER_COLUMN, profile.getGender());

            List<String> profilePhotoUrls = profile.getProfilePhotoUrls();
            if (profilePhotoUrls != null) {
                if (profilePhotoUrls.size() > 0) {
                    contentValues.put(
                            EgoEaterContract.ProfileTable.PHOTO_URL_0_COLUMN,
                            profilePhotoUrls.get(0));
                }
                if (profilePhotoUrls.size() > 1) {
                    contentValues.put(
                            EgoEaterContract.ProfileTable.PHOTO_URL_1_COLUMN,
                            profilePhotoUrls.get(1));
                }
                if (profilePhotoUrls.size() > 2) {
                    contentValues.put(
                            EgoEaterContract.ProfileTable.PHOTO_URL_2_COLUMN,
                            profilePhotoUrls.get(2));
                }
            }
            contentValuesList.add(contentValues);
        }

        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        getContentResolver().bulkInsert(
                EgoEaterContract.ProfileTable.CONTENT_URI,
                contentValuesArray);
        Log.i(LOG_TAG, "saveProfiles: Saved profiles to the device: " + contentValuesArray.length);

        markProfileIdsAsDownloaded(profiles);

        // Delete duplicate profiles just in case
        getContentResolver().delete(DeleteDuplicateProfiles.CONTENT_URI, null, null);
    }

    private void markProfileIdsAsDownloaded(List<ProfileBean> profiles)
            throws RemoteException, OperationApplicationException {

        ArrayList<ContentProviderOperation> updateOperations = new ArrayList<>();
        for (ProfileBean profileBean : profiles) {
            String profileId = Long.toString(profileBean.getUserId());
            ContentProviderOperation updateOperation =
                    ContentProviderOperation.newUpdate(ProfileIdTable.CONTENT_URI)
                            .withValue(ProfileIdTable.IS_PROFILE_LOADED_COLUMN, true)
                            .withSelection(
                                    ProfileIdTable.PROFILE_ID_COLUMN + " = ?",
                                    new String[]{profileId})
                            .build();
            updateOperations.add(updateOperation);
        }

        getContentResolver().applyBatch(EgoEaterContract.AUTHORITY, updateOperations);
        Log.i(LOG_TAG, "markProfileIdsAsDownloaded: Marked profile ids as downloaded: "
                + updateOperations.size());
    }

    private void loadProfileIds(final int minProfileIdCount, final Runnable runnable) {
        int queryRadius = EgoEaterPreferences.getQueryRadius(getApplicationContext());
        if (queryRadius < MAX_SEARCH_RADIUS) {
            queryRadius++;
            EgoEaterPreferences.setQueryRadius(getApplicationContext(), queryRadius);
        }

        new GetProfileIdsByDistanceClientAction(
                getApplicationContext(),
                new ApiClientAction.Callback<List<Long>>() {
                    @Override
                    public void onResult(List<Long> profileIds) {
                        saveProfileIds(profileIds, minProfileIdCount, runnable);
                    }
                },
                queryRadius).execute();
    }

    private void saveProfileIds(List<Long> profileIds, int minProfileIdCount, Runnable runnable) {
        // Get existing profile ids.
        Cursor cursor = getContentResolver().query(
                ProfileIdTable.CONTENT_URI,
                new String[]{ProfileIdTable.PROFILE_ID_COLUMN},
                null,
                null,
                null);
        if (cursor == null) {
            Log.e(LOG_TAG, "saveProfileIds: Failed to get profile ids.");
            return;
        }

        List<Long> existingProfileIds = new ArrayList<>(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                existingProfileIds.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }

        // Store new profile ids.
        ArrayList<ContentValues> contentValuesList = new ArrayList<>();
        for (Long profileId : profileIds) {
            if (!existingProfileIds.contains(profileId)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProfileIdTable.PROFILE_ID_COLUMN, profileId);
                contentValuesList.add(contentValues);
            }
        }

        ContentValues[] contentValuesArray =
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
        getContentResolver().bulkInsert(
                ProfileIdTable.CONTENT_URI,
                contentValuesArray);

        // Check if more profile ids need to be loaded.
        int queryRadius = EgoEaterPreferences.getQueryRadius(getApplicationContext());
        if ((contentValuesArray.length < minProfileIdCount) && (queryRadius < MAX_SEARCH_RADIUS)) {
            loadProfileIds(minProfileIdCount - contentValuesArray.length, runnable);
        } else {
            runnable.run();
        }
    }


    private void cacheProfilePhotos() {
        // Load the profiles for the first n profiles in the pipeline.
        Cursor pipelineCursor = getContentResolver().query(
                EgoEaterContract.PipelineTable.CONTENT_URI,
                new String[]{
                        EgoEaterContract.PipelineTable.PROFILE_0_ID_COLUMN,
                        EgoEaterContract.PipelineTable.PROFILE_1_ID_COLUMN},
                null,
                null,
                EgoEaterContract.PipelineTable.ID_COLUMN + " asc");
        if (pipelineCursor == null) {
            Log.e(LOG_TAG, "cacheProfilePhotos: Failed to query pipeline");
            return;
        }

        try {
            while (pipelineCursor.moveToNext()) {
                long profileId0 = pipelineCursor.getLong(0);
                long profileId1 = pipelineCursor.getLong(1);

                cacheProfilePhotos(profileId0);
                cacheProfilePhotos(profileId1);
            }
        } finally {
            pipelineCursor.close();
        }
    }

    private void cacheProfilePhotos(long profileId) {
        Cursor cursor = getContentResolver().query(
                EgoEaterContract.ProfileTable.CONTENT_URI,
                new String[]{
                        EgoEaterContract.ProfileTable.PHOTO_URL_0_COLUMN,
                        EgoEaterContract.ProfileTable.PHOTO_URL_1_COLUMN,
                        EgoEaterContract.ProfileTable.PHOTO_URL_2_COLUMN},
                EgoEaterContract.ProfileTable.PROFILE_ID_COLUMN + " = ?",
                new String[]{Long.toString(profileId)},
                null);

        try {
            while (cursor.moveToNext()) {
                cacheProfilePhoto(cursor.getString(0));
                cacheProfilePhoto(cursor.getString(0));
                cacheProfilePhoto(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

    }

    private void cacheProfilePhoto(String photoUrl) {
        Glide.with(getApplicationContext())
                .load(photoUrl)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        // TODO: Find out if we need to call get() on the Future.
    }

    /**
     * Checks if the two profiles are already compared.
     */
    private boolean isAlreadyCompared(Long profileId0, Long profileId1) {
        String p0Col = EgoEaterContract.RatingTable.WINNER_ID_COLUMN;
        String p1Col = EgoEaterContract.RatingTable.LOSER_ID_COLUMN;
        String where = String.format(
                "((%1$s = ?) and (%2$s = ?)) or ((%3$s = ?) and (%4$s = ?))",
                p0Col,
                p1Col,
                p1Col,
                p0Col);

        Cursor cursor = getContentResolver().query(
                EgoEaterContract.RatingTable.CONTENT_URI,
                new String[]{EgoEaterContract.RatingTable.ID_COLUMN},
                where,
                new String[]{
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
}
