package com.playposse.egoeater.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;

/**
 * {@link Activity} that is started on app startup. It's sole purpose is to have
 * {@link GlobalRouting} pick the true first {@link Activity}.
 */
public class LauncherActivity extends ParentActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_launcher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalRouting.onStartup(this);
    }
}
