package com.playposse.egoeater.activity.admin;

import android.app.Activity;
import android.os.Bundle;

import com.playposse.egoeater.activity.ParentActivity;

/**
 * An {@link Activity} that shows an admin all the users.
 */
public class AdminViewProfilesActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new AdminViewProfilesFragment());
    }
}
