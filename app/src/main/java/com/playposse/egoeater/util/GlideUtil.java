package com.playposse.egoeater.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

/**
 * Utility for loading images with Glide.
 */
public final class GlideUtil {

    private GlideUtil() {
    }

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

    public static void load(ImageView imageView, String imageUrl, int placeHolderResId) {
        if (imageUrl != null) {
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(placeHolderResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .into(imageView);
        } else {
            imageView.setImageBitmap(null);
        }
    }

    public static void loadCircular(final ImageView imageView, String imageUrl) {
        final Context context = imageView.getContext();

        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap bitmap) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }
}
