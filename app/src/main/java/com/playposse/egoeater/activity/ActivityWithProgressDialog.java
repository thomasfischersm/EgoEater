package com.playposse.egoeater.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.playposse.egoeater.R;

/**
 * An {@link Activity} that has a progress dialog.
 */
public abstract class ActivityWithProgressDialog extends AppCompatActivity {

    private ProgressDialog progressDialog;

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
}
