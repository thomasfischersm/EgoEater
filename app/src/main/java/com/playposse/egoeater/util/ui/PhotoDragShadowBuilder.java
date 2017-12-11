package com.playposse.egoeater.util.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View.DragShadowBuilder;
import android.widget.ImageView;

import com.playposse.egoeater.R;

/**
 * A {@link DragShadowBuilder} that shows up as a gray rectangle with the apps default aspect
 * ratio of 3:2.
 */
public class PhotoDragShadowBuilder extends DragShadowBuilder {

    private static final String LOG_TAG = PhotoDragShadowBuilder.class.getSimpleName();

    private final ImageView imageView;
    private final Drawable alternativeShadow;
    private final int width;
    private final int height;

    public PhotoDragShadowBuilder(ImageView imageView) {
        super(imageView);

        this.imageView = imageView;

        width = (int) imageView.getResources().getDimension(R.dimen.drag_shadow_width);
        height = (int) imageView.getResources().getDimension(R.dimen.drag_shadow_height);

        alternativeShadow = new ColorDrawable(Color.LTGRAY);
        alternativeShadow.setBounds(0, 0, width, height);
    }

    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {
        size.set(width, height);
        touch.set(width / 2, height / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        Drawable photoDrawable = imageView.getDrawable();

        if (photoDrawable == null) {
            // Fall back to gray square.
            alternativeShadow.draw(canvas);
            Log.i(LOG_TAG, "onDrawShadow: Drew alternative shadow");
        } else if (photoDrawable instanceof BitmapDrawable) {
            // Try to scale the photo.
            Drawable shadowDrawable = scaleBitmapDrawable((BitmapDrawable) photoDrawable);
            shadowDrawable.draw(canvas);
            Log.i(LOG_TAG, "onDrawShadow: Drew scaled BitmapDrawable");
//        } else if (photoDrawable instanceof GlideBitmapDrawable) {
//            // Try to scale the photo.
//            Drawable shadowDrawable = scaleBitmapDrawable((GlideBitmapDrawable) photoDrawable);
//            shadowDrawable.draw(canvas);
//            Log.i(LOG_TAG, "onDrawShadow: Drew scaled GlideBitmapDrawable");
        } else {
            // Fall back to drawing the photo without scaling.
            photoDrawable.draw(canvas);
            Log.e(LOG_TAG, "onDrawShadow: Drew unscaled photo of "
                    + photoDrawable.getClass().getName());
        }
    }

    @NonNull
    private Drawable scaleBitmapDrawable(BitmapDrawable photoDrawable) {
        return scaleBitmapDrawable(photoDrawable.getBitmap());
    }


//    @NonNull
//    private Drawable scaleBitmapDrawable(GlideBitmapDrawable photoDrawable) {
//        return scaleBitmapDrawable(photoDrawable.getBitmap());
//    }

    @NonNull
    private Drawable scaleBitmapDrawable(Bitmap photoBitmap) {
        Log.i(LOG_TAG, "scaleBitmapDrawable: original bitmap: "
                + photoBitmap.getWidth() + ", " + photoBitmap.getHeight());
        Bitmap shadowBitmap = Bitmap.createScaledBitmap(photoBitmap, width, height, true);
        Log.i(LOG_TAG, "scaleBitmapDrawable: scaled bitmap: "
                + shadowBitmap.getWidth() + ", " + shadowBitmap.getHeight());
        BitmapDrawable shadowDrawable = new BitmapDrawable(imageView.getResources(), shadowBitmap);
        shadowDrawable.setGravity(Gravity.FILL);
        shadowDrawable.setBounds(0, 0, width, height);
        return shadowDrawable;
    }
}
