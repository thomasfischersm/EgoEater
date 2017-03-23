package com.playposse.egoeater;

import android.content.Context;
import android.content.Intent;

import com.playposse.egoeater.activity.CropPhotoActivity;

/**
 * A central class for dealing with extra constants of intents.
 */
public class ExtraConstants {

    private static final String PHOTO_INDEX_ID = "com.playposse.egoeater.photoIndex";

    public static int getPhotoIndex(Intent intent) {
        return intent.getIntExtra(PHOTO_INDEX_ID, 0);
    }

    public static Intent createCropPhotoIntent(Context context, int photoIndex) {
        return new Intent(context, CropPhotoActivity.class)
                .putExtra(PHOTO_INDEX_ID, photoIndex);
    }
}
