package com.playposse.egoeater.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.playposse.egoeater.R;

/**
 * An {@link android.app.Activity} that informs the user that no more pairings are available.
 */
public class NoMorePairingsActivity extends ParentWithLocationCheckActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_no_more_pairings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
