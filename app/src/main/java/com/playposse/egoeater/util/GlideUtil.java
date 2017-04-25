package com.playposse.egoeater.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Utility for loading images with Glide.
 */
public final class GlideUtil {

    private GlideUtil() {}

    public static void load(ImageView imageView, String imageUrl) {
        if (imageUrl != null) {
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .into(imageView);
        } else {
            imageView.setImageBitmap(null);
        }
    }
}
