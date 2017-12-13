package com.playposse.egoeater.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.playposse.egoeater.glide.GlideApp;

/**
 * Utility for loading images with Glide.
 */
public final class GlideUtil {

    private GlideUtil() {
    }

    public static void load(ImageView imageView, String imageUrl) {
        if (imageUrl != null) {
            GlideApp.with(imageView.getContext())
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
            GlideApp.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(placeHolderResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .into(imageView);
        } else {
            imageView.setImageBitmap(null);
        }
    }

    public static void loadWithoutHardwareAcceleration(ImageView imageView, String imageUrl) {
        if (imageUrl != null) {
            GlideApp.with(imageView.getContext())
                    .applyDefaultRequestOptions(new RequestOptions().disallowHardwareConfig())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .into(imageView);
        } else {
            imageView.setImageBitmap(null);
        }
    }

    public static void loadCircular(final ImageView imageView, String imageUrl) {
        final Context context = imageView.getContext();

        GlideApp.with(context)
                .asBitmap()
                .load(imageUrl)
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
