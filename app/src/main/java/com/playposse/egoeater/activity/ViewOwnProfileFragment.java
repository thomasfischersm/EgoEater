package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.DeleteProfilePhotoClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.SimpleAlertDialog;

/**
 * A {@link Fragment} that lets the user edit his/her profile.
 */
public class ViewOwnProfileFragment extends Fragment {

    private ImageView profilePhoto0ImageView;
    private CardView photo1CardView;
    private ImageView profilePhoto1ImageView;
    private CardView photo2CardView;
    private ImageView profilePhoto2ImageView;
    private ImageView emptyPhoto1ImageView;
    private ImageView emptyPhoto2ImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private TextView profileTextView;
    private FloatingActionButton editButton;

    public ViewOwnProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_own_profile, container, false);

        profilePhoto0ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto0ImageView);
        photo1CardView = (CardView) rootView.findViewById(R.id.photo1CardView);
        profilePhoto1ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto1ImageView);
        photo2CardView = (CardView) rootView.findViewById(R.id.photo2CardView);
        profilePhoto2ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto2ImageView);
        emptyPhoto1ImageView = (ImageView) rootView.findViewById(R.id.emptyPhoto1ImageView);
        emptyPhoto2ImageView = (ImageView) rootView.findViewById(R.id.emptyPhoto2ImageView);
        headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);
        profileTextView = (TextView) rootView.findViewById(R.id.profileTextView);
        editButton = (FloatingActionButton) rootView.findViewById(R.id.editButton);

        initProfilePhoto(
                0,
                profilePhoto0ImageView,
                null,
                null,
                EgoEaterPreferences.getProfilePhotoUrl0(getContext()));
        initProfilePhoto(
                1,
                profilePhoto1ImageView,
                emptyPhoto1ImageView,
                photo1CardView,
                EgoEaterPreferences.getProfilePhotoUrl1(getContext()));
        initProfilePhoto(
                2,
                profilePhoto2ImageView,
                emptyPhoto2ImageView,
                photo2CardView,
                EgoEaterPreferences.getProfilePhotoUrl2(getContext()));

        UserBean userBean = EgoEaterPreferences.getUser(getContext());
        ProfileParcelable profile = new ProfileParcelable(userBean);
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(getContext(), profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(getContext(), profile));
        profileTextView.setText(profile.getProfileText());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });

        return rootView;
    }

    private void initProfilePhoto(
            final int photoIndex,
            ImageView imageView,
            @Nullable ImageView emptyView,
            @Nullable CardView cardView,
            String photoUrl) {

        if (photoUrl != null) {
            GlideUtil.load(imageView, photoUrl);
            imageView.setVisibility(View.VISIBLE);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
            if (cardView != null) {
                cardView.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setVisibility(View.GONE);
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
            }
            if (cardView != null) {
                cardView.setVisibility(View.GONE);
            }
        }

        View.OnClickListener clickListenerToDialog = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleAlertDialog.confirmPhoto(
                        getContext(),
                        new Runnable() {
                            @Override
                            public void run() {
                                pickPhoto(photoIndex);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                deletePhoto(photoIndex);
                            }
                        });
            }
        };
        View.OnClickListener clickListenerToPickActivity = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto(photoIndex);
            }
        };
        if (photoIndex == 0) {
            imageView.setOnClickListener(clickListenerToPickActivity);
        } else {
            imageView.setOnClickListener(clickListenerToDialog);
        }
        if (emptyView != null) {
            emptyView.setOnClickListener(clickListenerToPickActivity);
        }
    }

    private void pickPhoto(int photoIndex) {
        Intent intent = ExtraConstants.createCropPhotoIntent(getContext(), photoIndex);
        startActivity(intent);
    }

    private void deletePhoto(final int photoIndex) {
        ((ActivityWithProgressDialog) getActivity()).showLoadingProgress();
        new DeleteProfilePhotoClientAction(
                getContext(),
                photoIndex,
                new ApiClientAction.Callback<Void>() {
                    @Override
                    public void onResult(Void data) {
                        ((ActivityWithProgressDialog) getActivity()).dismissLoadingProgress();
                        clearPhotoSlot(photoIndex);
                    }
                }).execute();
    }

    private void clearPhotoSlot(int photoIndex) {
        if ((photoIndex == 1) && (EgoEaterPreferences.getProfilePhotoUrl1(getContext()) != null)) {
            // We actually need to clear the next photo slot and move the photo from slot 2 to slot
            // 1.
            initProfilePhoto(
                    1,
                    profilePhoto1ImageView,
                    emptyPhoto1ImageView,
                    photo1CardView,
                    EgoEaterPreferences.getProfilePhotoUrl1(getContext()));

            photoIndex = 2;
        }


        switch (photoIndex) {
            case 1:
                profilePhoto1ImageView.setImageBitmap(null);
                photo1CardView.setVisibility(View.GONE);
                profilePhoto1ImageView.setVisibility(View.GONE);
                emptyPhoto1ImageView.setVisibility(View.VISIBLE);
                break;
            case 2:
                profilePhoto2ImageView.setImageBitmap(null);
                photo2CardView.setVisibility(View.GONE);
                profilePhoto2ImageView.setVisibility(View.GONE);
                emptyPhoto2ImageView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
