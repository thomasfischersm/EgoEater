package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SaveProfileClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.DataMunchUtil;
import com.playposse.egoeater.util.EgoEaterConstants;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.StringUtil;

import static com.playposse.egoeater.util.EgoEaterConstants.LOCATION_SEPARATOR;
import static com.playposse.egoeater.util.EgoEaterConstants.USA_COUNTRY;

/**
 * An {@link android.app.Activity} to edit the profile.
 */
public class EditProfileActivity extends ParentWithLocationCheckActivity {

    private ImageButton profilePhoto0Button;
    private ImageButton profilePhoto1Button;
    private ImageButton profilePhoto2Button;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private EditText profileEditText;
    private Button saveButton;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.edit_profile_activity_title);

        profilePhoto0Button = (ImageButton) findViewById(R.id.profilePhoto0Button);
        profilePhoto1Button = (ImageButton) findViewById(R.id.profilePhoto1Button);
        profilePhoto2Button = (ImageButton) findViewById(R.id.profilePhoto2Button);
        headlineTextView = (TextView) findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) findViewById(R.id.subHeadTextView);
        profileEditText = (EditText) findViewById(R.id.profileEditText);
        saveButton = (Button) findViewById(R.id.saveButton);

        initProfilePhoto(0, profilePhoto0Button, EgoEaterPreferences.getProfilePhotoUrl0(this));
        initProfilePhoto(1, profilePhoto1Button, EgoEaterPreferences.getProfilePhotoUrl1(this));
        initProfilePhoto(2, profilePhoto2Button, EgoEaterPreferences.getProfilePhotoUrl2(this));

        UserBean userBean = EgoEaterPreferences.getUser(this);
        profileEditText.setText(userBean.getProfileText());
        headlineTextView.setText(formatHeadline(userBean));
        subHeadTextView.setText(formatSubHead(userBean));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });
    }

    private void initProfilePhoto(final int photoIndex, ImageButton imageButton, String photoUrl) {
        if (photoUrl != null) {
            GlideUtil.load(imageButton, photoUrl);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        ExtraConstants.createCropPhotoIntent(getApplicationContext(), photoIndex);
                startActivity(intent);
            }
        });
    }

    private void onSaveClicked() {
        showLoadingProgress();

        new SaveProfileClientAction(
                getApplicationContext(),
                profileEditText.getText().toString(),
                new ApiClientAction.Callback<Void>() {
                    @Override
                    public void onResult(Void data) {
                        onSaveCompleted();
                    }
                }).execute();
    }

    private void onSaveCompleted() {
        dismissLoadingProgress();

        startActivity(new Intent(this, RatingActivity.class));
    }

    private String formatHeadline(UserBean userBean) {
        StringBuilder sb = new StringBuilder();
        sb.append(userBean.getFirstName());

        Integer age = DataMunchUtil.getAge(userBean.getBirthday());
        if (age != null) {
            sb.append(getString(R.string.age_speparator));
            sb.append(age);
        }
        return sb.toString();
    }

    private String formatSubHead(UserBean userBean) {
        if (StringUtil.isEmpty(userBean.getCity())
                || StringUtil.isEmpty(userBean.getState())
                || StringUtil.isEmpty(userBean.getCountry())) {
            // Skip. The data is incomplete.
            return "";
        }

        if (userBean.getCountry().equals(USA_COUNTRY)) {
            return userBean.getCity() + LOCATION_SEPARATOR + userBean.getState();
        } else {
            return userBean.getCity() + LOCATION_SEPARATOR + userBean.getCountry();
        }
    }
}
