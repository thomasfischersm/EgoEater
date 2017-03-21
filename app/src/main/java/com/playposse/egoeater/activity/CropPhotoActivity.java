package com.playposse.egoeater.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.playposse.egoeater.R;

/**
 * An {@link android.app.Activity} that crops profile photos to fit the dimensions of the app.
 */
public class CropPhotoActivity extends ParentActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_crop_photo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
