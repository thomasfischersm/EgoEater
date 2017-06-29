package com.playposse.egoeater.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SaveProfileClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.SimpleAlertDialog;

/**
 * An {@link Activity} where the user edits the profile text. Contrast this with the
 * {@link ViewOwnProfileActivity}, which shows the user his/her own profile in a read-only version.
 */
public class EditProfileActivity extends ActivityWithProgressDialog {

    public static final int MAX_PROFILE_CHARACTER_COUNT = 500;

    private Toolbar toolbar;
    private ImageView discardImageView;
    private TextView titleTextView;
    private TextView saveTextView;
    private ScrollView scrollView;
    private CardView photo1CardView;
    private ImageView profilePhoto0ImageView;
    private CardView photo2CardView;
    private ImageView profilePhoto1ImageView;
    private ImageView profilePhoto2ImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private EditText profileEditText;
    private TextView characterCountTextView;
    private TextView profileBuilderLink;

    private String originalProfileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        discardImageView = (ImageView) findViewById(R.id.discardImageView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        saveTextView = (TextView) findViewById(R.id.saveTextView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        profilePhoto0ImageView = (ImageView) findViewById(R.id.profilePhoto0ImageView);
        photo1CardView = (CardView) findViewById(R.id.photo1CardView);
        profilePhoto1ImageView = (ImageView) findViewById(R.id.profilePhoto1ImageView);
        photo2CardView = (CardView) findViewById(R.id.photo2CardView);
        profilePhoto2ImageView = (ImageView) findViewById(R.id.profilePhoto2ImageView);
        headlineTextView = (TextView) findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) findViewById(R.id.subHeadTextView);
        profileEditText = (EditText) findViewById(R.id.profileEditText);
        characterCountTextView = (TextView) findViewById(R.id.characterCountTextView);
        profileBuilderLink = (TextView) findViewById(R.id.profileBuilderLink);

        setSupportActionBar(toolbar);

        UserBean userBean = EgoEaterPreferences.getUser(this);
        ProfileParcelable profile = new ProfileParcelable(userBean);
        originalProfileText = profile.getProfileText();

        if (originalProfileText == null) {
            originalProfileText = "";
        }

        if (profile.getPhotoUrl0() != null) {
            GlideUtil.load(profilePhoto0ImageView, profile.getPhotoUrl0());
        }
        if (profile.getPhotoUrl1() != null) {
            GlideUtil.load(profilePhoto1ImageView, profile.getPhotoUrl1());
        } else {
            photo1CardView.setVisibility(View.GONE);
        }
        if (profile.getPhotoUrl2() != null) {
            GlideUtil.load(profilePhoto2ImageView, profile.getPhotoUrl2());
        } else {
            photo2CardView.setVisibility(View.GONE);
        }
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(this, profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateDistanceAndProfile(this, profile));
        profileEditText.setText(profile.getProfileText());

        // Add listeners
        ProfileTextWatcher profileTextWatcher = new ProfileTextWatcher();
        profileTextWatcher.afterTextChanged(null);
        profileEditText.addTextChangedListener(profileTextWatcher);

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
            }
        });

        discardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardAndExit();
            }
        });

        profileEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollView.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    scrollView.fullScroll(View.FOCUS_DOWN);

                                }
                            },
                            150);
                }
            }
        });

        profileBuilderLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), ProfileBuilderActivity.class));
            }
        });
    }

    private void saveAndExit() {
        String newProfileText = profileEditText.getText().toString();

        showLoadingProgress();
        new SaveProfileClientAction(
                getApplicationContext(),
                newProfileText,
                new ApiClientAction.Callback<Void>() {
                    @Override
                    public void onResult(Void data) {
                        dismissLoadingProgress();
                        exit();
                    }
                }).execute();
    }

    private void discardAndExit() {
        if (originalProfileText.equals(profileEditText.getText().toString())) {
            // No changes -> simply exit.
            exit();
            return;
        }

        SimpleAlertDialog.confirmDiscard(
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        exit();
                    }
                });
    }

    private void exit() {
        finish();
        startActivity(new Intent(getApplicationContext(), ViewOwnProfileActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsUtil.reportScreenName(getApplication(), getClass().getSimpleName());

        CurrentActivity.setCurrentActivity(getClass());
    }

    @Override
    protected void onPause() {
        super.onPause();

        CurrentActivity.clearActivity();
    }

    @Override
    public void onBackPressed() {
        discardAndExit();
    }

    /**
     * A {@link TextWatcher} that updates the preview and character count.
     */
    private class ProfileTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignore.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Ignore.
        }

        @Override
        public void afterTextChanged(Editable s) {
            String newProfileText = profileEditText.getText().toString();
            UserBean userBean = EgoEaterPreferences.getUser(getApplicationContext());
            userBean.setProfileText(newProfileText);
            ProfileParcelable profile = new ProfileParcelable(userBean);

            subHeadTextView.setText(ProfileFormatter.formatCityStateDistanceAndProfile(
                    EditProfileActivity.this,
                    profile));

            characterCountTextView.setText(getString(
                    R.string.characterCounter,
                    newProfileText.length(),
                    MAX_PROFILE_CHARACTER_COUNT));
        }
    }
}
