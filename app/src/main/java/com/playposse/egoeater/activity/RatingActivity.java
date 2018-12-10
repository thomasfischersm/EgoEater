package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.Log;

import com.playposse.egoeater.BuildConfig;
import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.base.ParentActivity;
import com.playposse.egoeater.activity.specialcase.NoMorePairingsActivity;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.PairingParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.DatabaseDumper;
import com.playposse.egoeater.util.SurveyUtil;
import com.playposse.egoeater.util.dialogs.SimpleAlertDialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An {@link android.app.Activity} that shows two profiles side by side. The user should choose the
 * preferred profile.
 */
public class RatingActivity
        extends ParentActivity<RatingFragment>
        implements RatingProfileFragment.ProfileSelectionListener {

    private static final String LOG_TAG = RatingActivity.class.getSimpleName();

    private static final String PAIRING_KEY = "pairing";
    private static final String LEFT_PROFILE_KEY = "leftProfile";
    private static final String RIGHT_PROFILE_KEY = "rightProfile";

    private RatingFragment ratingFragment;
    private RatingProfileFragment leftRatingProfileFragment;
    private RatingProfileFragment rightRatingProfileFragment;

    private PairingParcelable pairing;
    private ProfileParcelable leftProfile;
    private ProfileParcelable rightProfile;

    private ExecutorService threadPoolExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ratingFragment = new RatingFragment();
        addMainFragment(ratingFragment);

        selectActivityTab(RATING_ACTIVITY_TAB_POSITION);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Try to show a snackbar with a survey prompt.
        RatingFragment contentFragment = getContentFragment();
        if ((contentFragment != null) && (contentFragment.getView() != null)) {
            CoordinatorLayout coordinatorLayout =
                    contentFragment.getView().findViewById(R.id.root_view);
            SurveyUtil.showSurveyNudge(coordinatorLayout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        leftRatingProfileFragment = ratingFragment.getLeftRatingProfileFragment();
        rightRatingProfileFragment = ratingFragment.getRightRatingProfileFragment();

        threadPoolExecutor = Executors.newCachedThreadPool();

        new LoadPairingAsyncTask().executeOnExecutor(Executors.newCachedThreadPool());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pairing = savedInstanceState.getParcelable(PAIRING_KEY);
        leftProfile = savedInstanceState.getParcelable(LEFT_PROFILE_KEY);
        rightProfile = savedInstanceState.getParcelable(RIGHT_PROFILE_KEY);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
            threadPoolExecutor = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PAIRING_KEY, pairing);
        outState.putParcelable(LEFT_PROFILE_KEY, leftProfile);
        outState.putParcelable(RIGHT_PROFILE_KEY, rightProfile);
    }

    @Override
    public void onProfileSelected(final ProfileParcelable profile) {
        if (profile == null) {
            Log.e(LOG_TAG, "onProfileSelected: Got a null profile!");
            return;
        }

        if (EgoEaterPreferences.hasFirstProfileBeenSelected(this)) {
            onProfileConfirmed(profile);
        } else {
            SimpleAlertDialog.confirm(
                    this,
                    R.string.first_selection_dialog_header,
                    R.string.first_selection_dialog_message,
                    new Runnable() {
                        @Override
                        public void run() {
                            EgoEaterPreferences.setFirstProfileBeenSelected(
                                    RatingActivity.this,
                                    true);
                            onProfileConfirmed(profile);
                        }
                    });
        }
    }

    private void onProfileConfirmed(ProfileParcelable profile) {
        if (threadPoolExecutor != null) {
            new StoreRatingAsyncTask(pairing, leftProfile, rightProfile, profile)
                    .executeOnExecutor(threadPoolExecutor);
        } else {
            new StoreRatingAsyncTask(pairing, leftProfile, rightProfile, profile)
                    .execute();
        }
    }

    /**
     * An {@link AsyncTask} that loads the next pairing for the user to choose.
     */
    private class LoadPairingAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOG_TAG, "Start LoadPairingAsyncTask");
            pairing = QueryUtil.getNextPairing(getApplicationContext(), true);

            if (pairing == null) {
                // No more pairings. Re-direct to a page to inform the user.
                startActivity(new Intent(getApplicationContext(), NoMorePairingsActivity.class));
                return null;
            }

            leftProfile = QueryUtil.getProfileByProfileId(
                    getContentResolver(),
                    pairing.getProfileId0());
            rightProfile = QueryUtil.getProfileByProfileId(
                    getContentResolver(),
                    pairing.getProfileId1());

            Log.i(LOG_TAG, "Done LoadPairingAsyncTask");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if ((leftProfile != null) && (rightProfile != null)) {
                leftRatingProfileFragment.setProfile(leftProfile);
                rightRatingProfileFragment.setProfile(rightProfile);
            }
        }
    }

    private class StoreRatingAsyncTask extends AsyncTask<Void, Void, Void> {

        private final PairingParcelable pairing;
        private final ProfileParcelable leftProfile;
        private final ProfileParcelable rightProfile;
        private final ProfileParcelable winningProfile;

        private StoreRatingAsyncTask(
                PairingParcelable pairing,
                ProfileParcelable leftProfile,
                ProfileParcelable rightProfile,
                ProfileParcelable winningProfile) {

            this.pairing = pairing;
            this.leftProfile = leftProfile;
            this.rightProfile = rightProfile;
            this.winningProfile = winningProfile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOG_TAG, "Start StoreRatingAsyncTask");

            // Ensure we are in a valid state.
            if ((pairing == null)
                    || (leftProfile == null)
                    || (rightProfile == null)
                    || (winningProfile == null)) {
                return null;
            }

            // Store rating.
            boolean isWinnerLeft = (winningProfile.getProfileId() == leftProfile.getProfileId());
            long winnerId = isWinnerLeft ? leftProfile.getProfileId() : rightProfile.getProfileId();
            long loserId = isWinnerLeft ? rightProfile.getProfileId() : leftProfile.getProfileId();
            QueryUtil.saveRating(
                    getApplicationContext(),
                    pairing.getPairingId(),
                    winnerId,
                    loserId);

            // Load next pairing.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (threadPoolExecutor != null) {
                        new LoadPairingAsyncTask().executeOnExecutor(threadPoolExecutor);
                    } else {
                        new LoadPairingAsyncTask().execute();
                    }
                }
            });

            AnalyticsUtil.reportRating(getApplication(), winnerId);

            if (BuildConfig.DEBUG) {
                DatabaseDumper.dumpTables(new MainDatabaseHelper(getApplicationContext()));
            }

            return null;
        }
    }
}
