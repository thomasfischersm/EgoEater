package com.playposse.egoeater.activity.admin;

import android.app.Activity;
import android.os.Bundle;

import com.playposse.egoeater.activity.base.ParentActivity;

/**
 * An {@link Activity} that shows an admin statistics of the user population.
 */
public class AdminStatisticsActivity extends ParentActivity {

    private static final String LOG_TAG = AdminStatisticsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new AdminStatisticsFragment());
    }
}
