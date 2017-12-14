package com.playposse.egoeater.activity.specialcase;

import android.app.Activity;
import android.os.Bundle;

import com.playposse.egoeater.activity.ParentActivity;

/**
 * An {@link Activity} that is shown to a user to request the age. Most users have the age imported
 * from Facebook. Some users have the age hidden on Facebook. These users are blocked from the
 * comparison page until an age is entered.
 */
public class MissingAgeActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new MissingAgeFragment());
    }
}
