package com.playposse.egoeater.activity;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.playposse.egoeater.R;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineTable;
import com.playposse.egoeater.contentprovider.MainDatabaseHelper;
import com.playposse.egoeater.contentprovider.QueryUtil;
import com.playposse.egoeater.services.PopulatePipelineService;
import com.playposse.egoeater.util.DatabaseDumper;

import java.util.List;

/**
 * An {@link android.app.Activity} that informs the user that no more pairings are available.
 */
public class NoMorePairingsActivity extends ParentWithLocationCheckActivity {

    private static final String LOG_TAG = NoMorePairingsActivity.class.getSimpleName();

    private ContentObserver contentObserver;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_no_more_pairings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (QueryUtil.getNextPairing(this, null, false) != null) {
            // The pipeline has a new pairing. Send the user back to the RatingActivity.
            startActivity(new Intent(this, RatingActivity.class));
        }
    }
}
