package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.EmailUtil;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.LogoutUtil;
import com.playposse.egoeater.util.ProfileFormatter;

/**
 * A {@link Fragment} that contains the navigation drawer.
 */
public class NavigationDrawerFragment extends Fragment {

    public NavigationDrawerFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        // Prepare profile photo.
        String profilePhotoUrl0 = EgoEaterPreferences.getProfilePhotoUrl0(getContext());
        ImageView profilePhotoImageView =
                (ImageView) rootView.findViewById(R.id.profilePhotoImageView);
        if (profilePhotoUrl0 != null) {
            GlideUtil.load(profilePhotoImageView, profilePhotoUrl0);
        } else {
            profilePhotoImageView.setImageResource(R.drawable.ic_person_black_24dp);
        }

        // Prepare profile info.
        UserBean userBean = EgoEaterPreferences.getUser(getContext());
        ProfileParcelable profile = new ProfileParcelable(userBean);
        TextView headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        TextView subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(getContext(), profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(getContext(), profile));

        // Prepare menu options.
        addOnClickListener(
                rootView, R.id.profileIconImageView,
                R.id.profileIconTextView,
                EditProfileActivity.class);
        addOnClickListener(
                rootView,
                R.id.ratingIconImageView,
                R.id.ratingIconTextView,
                RatingActivity.class);
        addOnClickListener(
                rootView,
                R.id.matchesIconImageView,
                R.id.matchesIconTextView,
                MatchesActivity.class);
        addOnClickListener(
                rootView,
                R.id.sendFeedbackIconImageView,
                R.id.sendFeedbackIconTextView,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EmailUtil.sendFeedbackAction(getContext());
                    }
                });
        addOnClickListener(
                rootView,
                R.id.aboutIconImageView,
                R.id.aboutIconTextView,
                AboutActivity.class);
        addOnClickListener(
                rootView,
                R.id.logoutIconImageView,
                R.id.logoutIconTextView,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogoutUtil.logout(getActivity());
                    }
                });

        return rootView;
    }

    private void addOnClickListener(
            View rootView,
            int viewId0,
            int viewId1,
            final Class<?> startActivity) {

        addOnClickListener(
                rootView,
                viewId0,
                viewId1,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), startActivity));
                    }
                });
    }

    private void addOnClickListener(
            View rootView,
            int viewId0,
            int viewId1,
            View.OnClickListener onClickListener) {

        rootView.findViewById(viewId0).setOnClickListener(onClickListener);
        rootView.findViewById(viewId1).setOnClickListener(onClickListener);
    }
}
