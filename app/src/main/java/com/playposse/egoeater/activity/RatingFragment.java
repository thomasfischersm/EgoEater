package com.playposse.egoeater.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A {@link Fragment} to compare two profiles.
 */
public class RatingFragment extends Fragment {

    private TextView orCircleTextView;

    public RatingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_rating, container, false);

        orCircleTextView = rootView.findViewById(R.id.orCircleTextView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!EgoEaterPreferences.hasSeenComparisonInfo(getContext())) {
            showFeatureDiscover();
            EgoEaterPreferences.setHasSeenComparisonInfo(getContext(), true);
        }
    }

    public RatingProfileFragment getLeftRatingProfileFragment() {
        return (RatingProfileFragment) getChildFragmentManager()
                .findFragmentById(R.id.leftProfileFragment);
    }

    public RatingProfileFragment getRightRatingProfileFragment() {
        return (RatingProfileFragment) getChildFragmentManager()
                .findFragmentById(R.id.rightProfileFragment);
    }

    private void showFeatureDiscover() {
        TapTargetView.showFor(getActivity(),
                TapTarget.forView(
                        orCircleTextView,
                        getString(R.string.comparison_info_title),
                        getString(R.string.comparison_info_body))
                        // All options below are optional
                        .outerCircleColor(R.color.colorSecondary)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.primaryTextColorDark)   // Specify a color for the target circle
                        .titleTextSize(20)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.primaryTextColorLight)      // Specify the color of the title text
                        .descriptionTextSize(14)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.primaryTextColorLight)  // Specify the color of the description text
//                        .textColor(R.color.secondaryTextColorDark)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        //.dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                        //.icon(Drawable)                     // Specify a custom drawable to draw as the target
                        .targetRadius(60));                  // Specify the target radius (in dp)
    }
}
