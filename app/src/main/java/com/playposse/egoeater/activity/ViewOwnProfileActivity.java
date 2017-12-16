package com.playposse.egoeater.activity;

import android.os.Bundle;

import com.playposse.egoeater.activity.base.ParentWithLocationCheckActivity;

/**
 * An {@link android.app.Activity} to edit the profile.
 */
public class ViewOwnProfileActivity extends ParentWithLocationCheckActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new ViewOwnProfileFragment());

        selectActivityTab(PROFILE_ACTIVITY_TAB_POSITION);
    }
}
