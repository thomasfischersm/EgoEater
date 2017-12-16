package com.playposse.egoeater.activity;

import android.os.Bundle;

import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.base.ParentActivity;

public class MatchesActivity extends ParentActivity {

    // TODO: Think about listening to contentprovider changes and refreshing the view.

    private static final String LOG_TAG = MatchesActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new MatchesFragment());

        setTitle(R.string.matches_activity_title);

        selectActivityTab(MATCHES_ACTIVITY_TAB_POSITION);
    }
}
