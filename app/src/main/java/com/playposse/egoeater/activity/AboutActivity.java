package com.playposse.egoeater.activity;

import android.os.Bundle;

import com.playposse.egoeater.activity.base.ParentActivity;

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

