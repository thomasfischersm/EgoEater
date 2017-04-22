package com.playposse.egoeater.services;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.playposse.egoeater.backend.egoEaterApi.model.ProfileBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.GetProfileIdsByDistanceClientAction;
import com.playposse.egoeater.clientactions.GetProfilesByIdClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.DeleteDuplicateProfiles;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.ProfileIdTable;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.CollectionsUtil;
import com.playposse.egoeater.util.DatabaseDumper;

import java.util.ArrayList;
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
     * When the pipeline rebuilt is triggered, the reason is logged to analyze unnecessary rebuilds.
     */
    private static final String TRIGGER_REASON_INTENT_PARAMETER = "triggerReason";

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

    public static void startService(Context context, int triggerReason) {
        Intent intent = new Intent(context, PopulatePipelineService.class);
        intent.putExtra(TRIGGER_REASON_INTENT_PARAMETER, triggerReason);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        if (isActive) {
            return;
        }
        isActive = true;
        final long start = System.currentTimeMillis();
        Log.i(LOG_TAG, "*** Start rebuilding pipeline.");

        try {
            loadProfilesIfNecessary();
            rebuildPipeline();
            cacheProfilePhotos();

            long end = System.currentTimeMillis();
            long duration = end - start;
            logExecution(intent, duration);
            DatabaseDumper.dumpTables(new MainDatabaseHelper(getApplicationContext()));
            Log.i(LOG_TAG, "*** Done rebuilding pipeline in " + duration + "ms");
            isActive = false;
        } catch (InterruptedException | RemoteException | OperationApplicationException ex) {
            Log.e(LOG_TAG, "run: Failed to rebuild pipeline2.", ex);
            ex.printStackTrace();
        }
    }

    private int rebuildPipeline() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> deleteOldPipelineOperations =
                PPSQueryHelper.prepareOperationsToDeleteOldPipeline(getContentResolver());

        Map<Integer, List<Long>> profileIdsByRankStatus =
                PPSQueryHelper.getProfileIdsByRankStatus(getContentResolver());

        List<ContentValues> contentValuesList =
                PPSQueryHelper.createPairings(getContentResolver(), profileIdsByRankStatus);

        PPSQueryHelper.storePairings(getContentResolver(), contentValuesList);

        PPSQueryHelper.deleteOldPairings(getContentResolver(), deleteOldPipelineOperations);

        return contentValuesList.size();
    }

    private void loadProfilesIfNecessary()
            throws InterruptedException, RemoteException, OperationApplicationException {

        // Determine unranked profiles
        final Integer unrankedProfilesCount =
                PPSQueryHelper.getUnrankedProfilesCount(getContentResolver());
        if (unrankedProfilesCount > MIN_PROFILES_CACHED) {
            Log.i(LOG_TAG, "loadProfilesIfNecessary: We found enough unranked profiles. Skip!");
            return;
        }

        // Determine unloaded profiles
        int neededProfilesCount = MIN_PROFILES_CACHED - unrankedProfilesCount;
        List<Long> unloadedProfileIds = PPSQueryHelper.getUnloadedProfileIds(
                getContentResolver(),
                neededProfilesCount);
        if (unloadedProfileIds == null) {
            return;
        }

        // Load profile ids if necessary.
        int neededProfileIdsCount = neededProfilesCount - unloadedProfileIds.size();
        final List<Long> newProfileIds;
        if (neededProfileIdsCount > 0) {
            newProfileIds = loadProfileIds(neededProfileIdsCount);
        } else {
            newProfileIds = null;
        }

        // Load more profiles
        List<Long> neededProfileIds =
                CollectionsUtil.combine(neededProfilesCount, unloadedProfileIds, newProfileIds);
        List<ProfileBean> profiles = GetProfilesByIdClientAction.getBlocking(
                getApplicationContext(),
                neededProfileIds);

        // Save profiles
        PPSQueryHelper.saveProfiles(getContentResolver(), profiles);
    }

    private List<Long> loadProfileIds(int neededProfileIdsCount)
            throws InterruptedException {

        List<Long> existingProfileIds = PPSQueryHelper.getExistingProfileIds(getContentResolver());

        int queryRadius = EgoEaterPreferences.getQueryRadius(getApplicationContext());
        List<Long> allRetrievedProfileIds = new ArrayList<>();

        while (allRetrievedProfileIds.size() < neededProfileIdsCount) {
            List<Long> profileIds = GetProfileIdsByDistanceClientAction.getBlocking(
                    getApplicationContext(),
                    queryRadius);
            if (profileIds == null) {
                profileIds = new ArrayList<>();
            }

            List<Long> savedProfileIds = PPSQueryHelper.saveProfileIds(
                    getContentResolver(),
                    profileIds,
                    existingProfileIds);
            allRetrievedProfileIds.addAll(savedProfileIds);

            // If the max radius has already been reached, run the cloud query only ones to check
            // for new users in the radius.
            if (queryRadius >= MAX_SEARCH_RADIUS) {
                break;
            }

            // Expand the search radius.
            queryRadius++;
            EgoEaterPreferences.setQueryRadius(getApplicationContext(), queryRadius);
        }

        return allRetrievedProfileIds;
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

    private void logExecution(Intent intent, long duration) {
        int triggerReason = intent.getIntExtra(TRIGGER_REASON_INTENT_PARAMETER, -1);

        ContentValues contentValues = new ContentValues();
        contentValues.put(PipelineLogTable.DURATION_MS_COLUMN, duration);
        contentValues.put(PipelineLogTable.TRIGGER_REASON_COLUMN, triggerReason);

        getContentResolver().insert(PipelineLogTable.CONTENT_URI, contentValues);
    }
}
