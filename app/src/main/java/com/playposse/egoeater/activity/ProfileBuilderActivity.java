package com.playposse.egoeater.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.playposse.egoeater.R;

/**
 * An {@link Activity} that consists of a wizard. The wizard gives the user a series of choices to
 * select for the profile. The final slide allows the user to re-order the selection to put the
 * most important item first.
 */
public class ProfileBuilderActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new ProfileBuilderFragment());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.profile_builder_title);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this, EditProfileActivity.class));
    }
}
