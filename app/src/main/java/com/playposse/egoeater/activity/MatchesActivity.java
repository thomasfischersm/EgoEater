package com.playposse.egoeater.activity;

import android.os.Bundle;

import com.playposse.egoeater.R;

public class MatchesActivity extends ParentActivity {

    // TODO: Think about listening to contentprovider changes and refreshing the view.

    private static final String LOG_TAG = MatchesActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new MatchesFragment());

        setTitle(R.string.matches_activity_title);
    }
}
