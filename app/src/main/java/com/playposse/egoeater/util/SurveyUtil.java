package com.playposse.egoeater.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.View;

import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A utility that nudges the user with intermittent snack bars to fill out a survey about the app.
 */
public final class SurveyUtil {

    private static final String LOG_TAG = SurveyUtil.class.getSimpleName();

    private static final String SURVEY_URL = "https://docs.google.com/forms/d/e/1FAIpQLScG6aSPd92pn-Y4C9mTpTcXEnxIZ31_6JtrPpoI15dCcYCFgg/viewform";

    private SurveyUtil() {
    }

    public static void showSurveyNudge(@Nullable View view) {
        if (view == null) {
            // The current activity doesn't have a CoordinatorLayout.
            return;
        }

        Context context = view.getContext();

        if (EgoEaterPreferences.hasSurveyBeenClicked(context)) {
            // The survey has already been shown. Don't show it again!
            return;
        }

        int nudgeCount = EgoEaterPreferences.incrementSurveyNudgeCounter(context);
        Log.d(LOG_TAG, "showSurveyNudge: Survey nudge count is " + nudgeCount);

        if (!isPowerOfTwo(nudgeCount)) {
            // Only bug the user in these intervals: 2nd, 4th, 8th, 16th, 32nd... time of starting
            // an activity.
            return;
        }

        showSnackBar(view);
    }

    private static void showSnackBar(final View view) {
        Snackbar snackbar = Snackbar.make(
                view,
                R.string.survey_snack_bar_prompt,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(
                R.string.survey_snack_bar_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSurveyClicked(view.getContext());
                    }
                });
        snackbar.show();
    }

    private static void onSurveyClicked(Context context) {
        EgoEaterPreferences.setHasSurveyBeenClicked(context, true);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SURVEY_URL));
        context.startActivity(intent);
    }

    private static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }
}
