package com.playposse.egoeater.activity;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.edmodo.cropper.CropImageView;
import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * An {@link android.app.Activity} that crops profile photos to fit the dimensions of the app.
 */
public class CropPhotoActivity extends ParentActivity {

    /**
     * URL pattern to the FB profile photo of a user. The width is set to an arbitrary large size to
     * get the full size photo.
     */
    private static final String FB_PROFILE_PHOTO_URL =
            "https://graph.facebook.com/%1$s/picture?width=9999";

    private CropImageView cropImageView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_crop_photo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        // TODO: Add to entry paths, one for FB photo and one for picking a photo from the gallery.

        String fbProfileId = EgoEaterPreferences.getUser(this).getFbProfileId();
        String fbPhotoUrl = String.format(FB_PROFILE_PHOTO_URL, fbProfileId);
        Glide
                .with(this)
                .load(fbPhotoUrl)
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
//                        cropImageView.setImageBitmap(bitmap);
//                    }
//                })
                .into(cropImageView);
    }
}
