package com.playposse.egoeater.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.playposse.egoeater.R;
import com.playposse.egoeater.util.AnalyticsUtil;

import io.fabric.sdk.android.Fabric;

/**
 * An {@link Activity} that has a progress dialog.
 */
public abstract class ActivityWithProgressDialog extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
    }

    protected void showLoadingProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(ActivityWithProgressDialog.this);
                progressDialog.setTitle(R.string.progress_dialog_title);
                progressDialog.setMessage(getString(R.string.progress_dialog_message));
                progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progressDialog.show();
            }
        });
    }

    protected void dismissLoadingProgress() {
        if (progressDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsUtil.reportScreenName(getApplication(), getClass().getSimpleName());

        CurrentActivity.setCurrentActivity(getClass());
    }
}
