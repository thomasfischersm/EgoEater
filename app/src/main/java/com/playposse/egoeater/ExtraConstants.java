package com.playposse.egoeater;

import android.content.Context;
import android.content.Intent;

import com.playposse.egoeater.activity.CropPhotoActivity;
import com.playposse.egoeater.activity.ViewProfileActivity;

/**
 * A central class for dealing with extra constants of intents.
 */
public class ExtraConstants {

    private static final String PHOTO_INDEX_ID = "com.playposse.egoeater.photoIndex";
    private static final String PROFILE_ID = "com.playposse.egoeater.profileId";

    public static int getPhotoIndex(Intent intent) {
        return intent.getIntExtra(PHOTO_INDEX_ID, 0);
    }

    public static Intent createCropPhotoIntent(Context context, int photoIndex) {
        return new Intent(context, CropPhotoActivity.class)
                .putExtra(PHOTO_INDEX_ID, photoIndex);
    }

    public static long getProfileId(Intent intent) {
        return intent.getLongExtra(PROFILE_ID, -1);
    }

    public static void startViewProfileActivity(Context context, long profileId) {
        Intent intent = new Intent(context, ViewProfileActivity.class);
        intent.putExtra(PROFILE_ID, profileId);
        context.startActivity(intent);
    }
}
