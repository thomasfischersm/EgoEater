package com.playposse.egoeater.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.playposse.egoeater.R;

/**
 * An {@link Activity} that lets two users message each other.
 */
public class MessagingActivity extends ParentActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_messaging;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
