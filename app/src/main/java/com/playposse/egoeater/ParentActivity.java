package com.playposse.egoeater;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * An abstract {@link android.app.Activity} that contains the boilerplate to instantiate the support
 * toolbar.
 */
public abstract class ParentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    protected abstract int getLayoutResId();

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.options_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.help_menu_item:
//                startActivity(new Intent(this, HelpActivity.class));
//                return true;
//            case R.id.settings_menu_item:
//                startActivity(new Intent(this, SettingsActivity.class));
//                return true;
//            case R.id.send_feedback_menu_item:
//                EmailUtil.sendFeedbackAction(this);
//                return true;
//            case R.id.about_menu_item:
//                startActivity(new Intent(this, AboutActivity.class));
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();

        EgoEaterApplication application = (EgoEaterApplication) getApplication();
        Tracker tracker = application.getDefaultTracker();
        tracker.setScreenName(getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}