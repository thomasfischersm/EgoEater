package com.playposse.egoeater.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SaveProfileClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;

/**
 * A {@link Fragment} that lets the user edit his/her profile.
 */
public class EditProfileFragment extends Fragment {

    private ImageView profilePhoto0ImageView;
    private ImageView profilePhoto1ImageView;
    private ImageView profilePhoto2ImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private FloatingActionButton editButton;
//    private EditText profileEditText;
//    private Button saveButton;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profilePhoto0ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto0ImageView);
        profilePhoto1ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto1ImageView);
        profilePhoto2ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto2ImageView);
        headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);
        editButton = (FloatingActionButton) rootView.findViewById(R.id.editButton);
//        profileEditText = (EditText) rootView.findViewById(R.id.profileEditText);
//        saveButton = (Button) rootView.findViewById(R.id.saveButton);

        initProfilePhoto(0, profilePhoto0ImageView, EgoEaterPreferences.getProfilePhotoUrl0(getContext()));
        initProfilePhoto(1, profilePhoto1ImageView, EgoEaterPreferences.getProfilePhotoUrl1(getContext()));
        initProfilePhoto(2, profilePhoto2ImageView, EgoEaterPreferences.getProfilePhotoUrl2(getContext()));

        UserBean userBean = EgoEaterPreferences.getUser(getContext());
        ProfileParcelable profile = new ProfileParcelable(userBean);
//        profileEditText.setText(userBean.getProfileText());
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(getContext(), profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(getContext(), profile));

//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onSaveClicked();
//            }
//        });

        return rootView;
    }

    private void initProfilePhoto(final int photoIndex, ImageView imageView, String photoUrl) {
        if (photoUrl != null) {
            GlideUtil.load(imageView, photoUrl);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        ExtraConstants.createCropPhotoIntent(getContext(), photoIndex);
                startActivity(intent);
            }
        });
    }
//
//    private void onSaveClicked() {
//        ((ParentActivity) getActivity()).showLoadingProgress();
//
//        new SaveProfileClientAction(
//                getContext(),
//                profileEditText.getText().toString(),
//                new ApiClientAction.Callback<Void>() {
//                    @Override
//                    public void onResult(Void data) {
//                        onSaveCompleted();
//                    }
//                }).execute();
//    }
//
//    private void onSaveCompleted() {
//        ((ParentActivity) getActivity()).dismissLoadingProgress();
//
//        startActivity(new Intent(getContext(), RatingActivity.class));
//    }
}
