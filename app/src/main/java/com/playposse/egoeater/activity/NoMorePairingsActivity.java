package com.playposse.egoeater.activity;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Bundle;

import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.services.PopulatePipelineService;
import com.playposse.egoeater.util.DatabaseDumper;

/**
 * An {@link android.app.Activity} that informs the user that no more pairings are available.
 */
public class NoMorePairingsActivity extends ParentWithLocationCheckActivity {

    private static final String LOG_TAG = NoMorePairingsActivity.class.getSimpleName();

    private ContentObserver contentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new NoMorePairingsFragment());

        selectActivityTab(RATING_ACTIVITY_TAB_POSITION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Let's kick the service to build the pipeline. Maybe, another pairing can be found.
        PopulatePipelineService.startService(
                this,
                PipelineLogTable.NO_MORE_PAIRING_ACTIVITY_TRIGGER);

        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                checkIfPipelineIsRefreshed();
            }
        };
        getContentResolver().registerContentObserver(
                PipelineTable.CONTENT_URI,
                true,
                contentObserver);

        DatabaseDumper.dumpTables(new MainDatabaseHelper(this));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (contentObserver != null) {
            getContentResolver().unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }

    private void checkIfPipelineIsRefreshed() {
        if (QueryUtil.getNextPairing(this, false) != null) {
            // The pipeline has a new pairing. Send the user back to the RatingActivity.
            startActivity(new Intent(this, RatingActivity.class));
        }
    }
}
