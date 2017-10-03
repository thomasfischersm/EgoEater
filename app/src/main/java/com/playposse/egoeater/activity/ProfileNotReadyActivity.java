package com.playposse.egoeater.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.playposse.egoeater.util.ProfileUtil;

/**
 * An {@link Activity} that warns the user that the profile hasn't been filled out yet, and the
 * compare activity cannot be started yet.
 */
public class ProfileNotReadyActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new ProfileNotReadyFragment());

        selectActivityTab(RATING_ACTIVITY_TAB_POSITION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ProfileUtil.isReady(this)) {
            startActivity(new Intent(this, RatingActivity.class));
        }
    }
}
