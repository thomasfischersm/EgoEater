package com.playposse.egoeater.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.playposse.egoeater.R;

/**
 * An informative {@link android.app.Activity} that tells the user about the app.
 */
public class AboutActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new AboutFragment());
    }
}

