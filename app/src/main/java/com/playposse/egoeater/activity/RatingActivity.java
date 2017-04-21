package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.LinearLayout;

import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.storage.PairingParcelable;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.DatabaseDumper;

/**
 * An {@link android.app.Activity} that shows two profiles side by side. The user should choose the
 * preferred profile.
 */
public class RatingActivity
        extends ParentActivity
        implements ProfileFragment.ProfileSelectionListener {

    private static final String LOG_TAG = RatingActivity.class.getSimpleName();

    private static final String PAIRING_KEY = "pairing";
    private static final String LEFT_PROFILE_KEY = "leftProfile";
    private static final String RIGHT_PROFILE_KEY = "rightProfile";

    private ProfileFragment leftProfileFragment;
    private ProfileFragment rightProfileFragment;

    private PairingParcelable pairing;
    private ProfileParcelable leftProfile;
    private ProfileParcelable rightProfile;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rating;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leftProfileFragment =
                (ProfileFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.leftProfileFragment);
        rightProfileFragment =
                (ProfileFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.rightProfileFragment);

        new LoadPairingAsyncTask().execute();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pairing = savedInstanceState.getParcelable(PAIRING_KEY);
        leftProfile = savedInstanceState.getParcelable(LEFT_PROFILE_KEY);
        rightProfile = savedInstanceState.getParcelable(RIGHT_PROFILE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PAIRING_KEY, pairing);
        outState.putParcelable(LEFT_PROFILE_KEY, leftProfile);
        outState.putParcelable(RIGHT_PROFILE_KEY, rightProfile);
    }

    @Override
    public void onProfileSelected(ProfileParcelable profile) {
        if (profile == null) {
            Log.e(LOG_TAG, "onProfileSelected: Got a null profile!");
            return;
        }

        new StoreRatingAsyncTask().execute(profile);
    }

    /**
     * An {@link AsyncTask} that loads the next pairing for the user to choose.
     */
    private class LoadPairingAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            leftProfileFragment.setProfile(leftProfile);
            rightProfileFragment.setProfile(rightProfile);
        }
    }

    private class StoreRatingAsyncTask extends AsyncTask<ProfileParcelable, Void, Void> {

        @Override
        protected Void doInBackground(ProfileParcelable... params) {
            // Store rating.
            ProfileParcelable profile = params[0];
            boolean isWinnerLeft = (profile.getProfileId() == leftProfile.getProfileId());
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
                    new LoadPairingAsyncTask().execute();
                }
            });

            DatabaseDumper.dumpTables(new MainDatabaseHelper(getApplicationContext()));

            return null;
        }
    }
}
