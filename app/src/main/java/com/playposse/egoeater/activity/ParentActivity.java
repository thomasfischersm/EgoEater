package com.playposse.egoeater.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.admin.AdminStatisticsActivity;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.EmailUtil;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.LogoutUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.admin.AdminImportUtil;

/**
 * An abstract {@link android.app.Activity} that contains the boilerplate to instantiate the support
 * toolbar.
 */
public abstract class ParentActivity extends ActivityWithProgressDialog {

    private static final String LOG_TAG = ParentActivity.class.getSimpleName();

    protected static final int PROFILE_ACTIVITY_TAB_POSITION = 0;
    protected static final int RATING_ACTIVITY_TAB_POSITION = 1;
    protected static final int MATCHES_ACTIVITY_TAB_POSITION = 2;

    private DrawerLayout drawerLayout;
    private LinearLayout mainFragmentContainer;
    private NavigationView navigationView;
    private ImageView infoImageView;
    private TabLayout activityTabLayout;

    private ActionBarDrawerToggle drawerToggle;
    private ActivityTabSelectedListener tabSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        redirectToAccountActivationIfNecessary();
        redirectToLoginIfSessionExpired();

        int activityResId = getLayoutResId();
        if (activityResId != 0) {
            setContentView(activityResId);
        } else {
            setContentView(R.layout.activity_parent);
            drawerLayout = findViewById(R.id.drawerLayout);
            mainFragmentContainer = findViewById(R.id.mainFragmentContainer);
            navigationView = findViewById(R.id.navigationView);
            infoImageView = findViewById(R.id.infoImageView);
            activityTabLayout = findViewById(R.id.activityTabLayout);

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

            infoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAppInfo();
                }
            });

            initNavigationHeader();

            tabSelectedListener = new ActivityTabSelectedListener();
            activityTabLayout.addOnTabSelectedListener(tabSelectedListener);
        }

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
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

    /**
     * Show an admin menu only for admin users.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!EgoEaterPreferences.isAdmin(this)) {
            return super.onCreateOptionsMenu(menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.admin_menu, menu);
            return true;
        }
    }

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
            case R.id.admin_statistics_action:
                Intent intent = new Intent(this, AdminStatisticsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.admin_refresh_action:
                AdminImportUtil.refresh(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void initNavigationHeader() {
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
        if (userBean == null) {
            Log.e(LOG_TAG, "initNavigationHeader: The user is missing from the preferences!");
            GlobalRouting.onSessionExpired(this);
            return;
        }
        if (userBean.getUserId() == null) {
            Log.e(LOG_TAG, "initNavigationHeader: The UserBean.userId is null when it " +
                    "shouldn't be!");
            // Send back to login to fix this.
            GlobalRouting.onSessionExpired(this);
            return;
        }
        ProfileParcelable profile = new ProfileParcelable(userBean);
        TextView headlineTextView = rootView.findViewById(R.id.headlineTextView);
        TextView subHeadTextView = rootView.findViewById(R.id.subHeadTextView);
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(this, profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(this, profile));
    }

    private void showAppInfo() {
        TapTargetView.showFor(this,
                TapTarget.forView(
                        infoImageView,
                        getString(R.string.app_info_title),
                        getString(R.string.app_info_body))
                        // All options below are optional
                        .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.primaryTextColorDark)   // Specify a color for the target circle
                        .titleTextSize(20)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.primaryTextColorDark)      // Specify the color of the title text
                        .descriptionTextSize(14)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.secondaryTextColorDark)  // Specify the color of the description text
//                        .textColor(R.color.secondaryTextColorDark)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        //.dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                        //.icon(Drawable)                     // Specify a custom drawable to draw as the target
                        .targetRadius(60));                  // Specify the target radius (in dp)
    }

    /**
     * Selects the specified tab.
     */
    protected void selectActivityTab(int position) {
        activityTabLayout.removeOnTabSelectedListener(tabSelectedListener);
        TabLayout.Tab tab = activityTabLayout.getTabAt(position);
        if (tab != null) {
            tab.select();
        }
        activityTabLayout.addOnTabSelectedListener(tabSelectedListener);
    }

    /**
     * A {@link TabLayout.OnTabSelectedListener} that launches the selected activity.
     */
    private class ActivityTabSelectedListener implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:
                    finish();
                    startActivity(new Intent(ParentActivity.this, ViewOwnProfileActivity.class));
                    break;
                case 1:
                    finish();
                    startActivity(new Intent(ParentActivity.this, RatingActivity.class));
                    break;
                case 2:
                    finish();
                    startActivity(new Intent(ParentActivity.this, MatchesActivity.class));
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            // Ignore.
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // Ignore.
        }
    }

    private void redirectToLoginIfSessionExpired() {
        if (this instanceof LoginActivity) {
            // Skip this check for the login activity.
            return;
        }

        if (EgoEaterPreferences.getSessionId(this) == null) {
            finish();
            GlobalRouting.onSessionExpired(this);
        }
    }

    private void redirectToAccountActivationIfNecessary() {
        if (this instanceof ReactivateAccountActivity) {
            // Skip this check if we are already on the activity to reactivate the account.
            return;
        }

        if (!EgoEaterPreferences.isActive(this)) {
            finish();
            GlobalRouting.onRequiresAccountReactivation(this);
        }
    }
}