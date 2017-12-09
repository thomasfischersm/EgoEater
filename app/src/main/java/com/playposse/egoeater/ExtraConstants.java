package com.playposse.egoeater;

import android.content.Context;
import android.content.Intent;

import com.playposse.egoeater.activity.CropPhotoActivity;
import com.playposse.egoeater.activity.LoginActivity;
import com.playposse.egoeater.activity.MessagingActivity;
import com.playposse.egoeater.activity.ViewProfileActivity;

/**
 * A central class for dealing with extra constants of intents.
 */
public class ExtraConstants {

    private static final String PHOTO_INDEX_ID = "com.playposse.egoeater.photoIndex";
    private static final String PROFILE_ID = "com.playposse.egoeater.profileId";
    private static final String SESSION_EXPIRED_FLAG = "com.playposse.egoeater.sessionExpired";

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

    public static void startMessagesActivity(Context context, long profileId) {
        Intent intent = new Intent(context, MessagingActivity.class);
        intent.putExtra(PROFILE_ID, profileId);
        context.startActivity(intent);
    }

    public static void startLoginActivityWithSessionExpirationDialog(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SESSION_EXPIRED_FLAG, true);
        context.startActivity(intent);
    }

    public static boolean hasSessionExpired(Intent intent) {
        return intent.getBooleanExtra(SESSION_EXPIRED_FLAG, false);
    }
}
