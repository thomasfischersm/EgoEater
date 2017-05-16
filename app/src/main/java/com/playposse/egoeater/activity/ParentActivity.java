package com.playposse.egoeater.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.EmailUtil;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.LogoutUtil;
import com.playposse.egoeater.util.ProfileFormatter;

/**
 * An abstract {@link android.app.Activity} that contains the boilerplate to instantiate the support
 * toolbar.
 */
public abstract class ParentActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout mainFragmentContainer;
    private NavigationView navigationView;

    private ProgressDialog progressDialog;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int activityResId = getLayoutResId();
        if (activityResId != 0) {
            setContentView(activityResId);
        } else {
            setContentView(R.layout.activity_parent);
            drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
            mainFragmentContainer = (LinearLayout) findViewById(R.id.mainFragmentContainer);
            navigationView = (NavigationView) findViewById(R.id.navigationView);

            drawerToggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    R.string.drawer_open,
                    R.string.drawer_close);
            drawerLayout.addDrawerListener(drawerToggle);

            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            return onOptionsItemSelected(item);
                        }
                    });

            initNavigationHeader();
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        applyCustomFontToActionBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    // TODO: Remove this.
    protected int getLayoutResId() {
        return 0;
    }

    ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ((drawerToggle != null) && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
                break;
            case R.id.profile_menu_item:
                drawerLayout.closeDrawers();
                finish();
                startActivity(new Intent(this, ViewOwnProfileActivity.class));
                return true;
            case R.id.rating_menu_item:
                drawerLayout.closeDrawers();
                finish();
                startActivity(new Intent(this, RatingActivity.class));
                return true;
            case R.id.matches_menu_item:
                drawerLayout.closeDrawers();
                finish();
                startActivity(new Intent(this, MatchesActivity.class));
                return true;
            case R.id.send_feedback_menu_item:
                drawerLayout.closeDrawers();
                EmailUtil.sendFeedbackAction(this);
                return true;
            case R.id.about_menu_item:
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.logout_menu_item:
                drawerLayout.closeDrawers();
                LogoutUtil.logout(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
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

    private void applyCustomFontToActionBar() {
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);

        if (titleTextView != null) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/nexa_bold.otf");
            titleTextView.setTypeface(typeface);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    protected void addMainFragment(Fragment mainFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.mainFragmentContainer, mainFragment);
        fragmentTransaction.commit();
    }

    public void initNavigationHeader() {
        View rootView = navigationView.getHeaderView(0);

        // Prepare profile photo.
        String profilePhotoUrl0 = EgoEaterPreferences.getProfilePhotoUrl0(this);
        ImageView profilePhotoImageView =
                (ImageView) rootView.findViewById(R.id.profilePhotoImageView);
        if (profilePhotoUrl0 != null) {
            GlideUtil.load(profilePhotoImageView, profilePhotoUrl0);
        } else {
            profilePhotoImageView.setImageResource(R.drawable.ic_person_black_24dp);
        }

        // Prepare profile info.
        UserBean userBean = EgoEaterPreferences.getUser(this);
        ProfileParcelable profile = new ProfileParcelable(userBean);
        TextView headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        TextView subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(this, profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(this, profile));
    }
}