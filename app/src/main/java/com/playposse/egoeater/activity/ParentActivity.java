package com.playposse.egoeater.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.playposse.egoeater.EgoEaterApplication;
import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.EmailUtil;

/**
 * An abstract {@link android.app.Activity} that contains the boilerplate to instantiate the support
 * toolbar.
 */
public abstract class ParentActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    protected abstract int getLayoutResId();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profile_menu_item:
                finish();
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            case R.id.rating_menu_item:
                finish();
                startActivity(new Intent(this, RatingActivity.class));
                return true;
            case R.id.matches_menu_item:
                finish();
                startActivity(new Intent(this, MatchesActivity.class));
                return true;
            case R.id.send_feedback_menu_item:
                EmailUtil.sendFeedbackAction(this);
                return true;
            case R.id.about_menu_item:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.logout_menu_item:
                EgoEaterPreferences.clearSessionId(this);
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsUtil.reportScreenName(getApplication(), getClass().getSimpleName());

        CurrentActivity.setCurrentActivity(getClass());
    }

    @Override
    protected void onPause() {
        super.onPause();

        CurrentActivity.clearActivity();
    }

    protected void showLoadingProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(ParentActivity.this);
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