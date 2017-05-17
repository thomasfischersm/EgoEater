package com.playposse.egoeater.util;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * A {@link GridLayoutManager} that adjusts the column count responding to the available size. It
 * takes a minItemWidth and maxColumnCount to decided on the best column count.
 */
public class ResponsiveGridLayoutManager extends GridLayoutManager {

    private final int minItemPxWidth;
    private final int maxColumnCount;

    private int lastWidth = -1;

    public ResponsiveGridLayoutManager(Context context, int minItemDpWidth, int maxColumnCount) {
        super(context, 1 /* meaningless placeholder */);

        this.maxColumnCount = maxColumnCount;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        minItemPxWidth = (int) Math.ceil(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                minItemDpWidth,
                displayMetrics));
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (lastWidth != getWidth()) {
            int availableWidth = getWidth() - getPaddingRight() - getPaddingLeft();
            int spanCount = Math.min(maxColumnCount, Math.max(1, availableWidth / minItemPxWidth));
            setSpanCount(spanCount);

            // Remember to avoid recalculating unnecessarily.
            lastWidth = getWidth();
        }

        super.onLayoutChildren(recycler, state);
    }
}
