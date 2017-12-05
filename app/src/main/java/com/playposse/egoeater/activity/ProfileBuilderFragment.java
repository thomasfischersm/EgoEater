package com.playposse.egoeater.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SaveProfileClientAction;
import com.playposse.egoeater.data.profilewizard.ProfileBuilderConfiguration;
import com.playposse.egoeater.data.profilewizard.ProfileUserData;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.SimpleAlertDialog;
import com.playposse.egoeater.util.StringUtil;

import org.json.JSONException;

import java.io.IOException;

/**
 * A {@link Fragment} that shows multiple fragments, that the user can swipe through, to build a
 * profile.
 */
public class ProfileBuilderFragment extends Fragment {

    private static final String LOG_TAG = ProfileBuilderFragment.class.getSimpleName();

    private ImageView discardImageView;
    private TextView titleTextView;
    private TextView resetTextView;
    private ViewPager profileBuilderViewPager;
    private TextView pageIndexTextView;
    private Button backButton;
    private Button continueButton;
    private Button saveButton;

    private ProfileUserData profileUserData;
    private ProfileBuilderConfiguration profileBuilderConfiguration;
    private ProfilePagerAdapter profilePagerAdapter;

    public ProfileBuilderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            try {
                String currentProfileText = EgoEaterPreferences.getProfileText(getActivity());
                String lastProfileText =
                        EgoEaterPreferences.getProfileBuilderLastProfileText(getActivity());
                if ((currentProfileText != null) && currentProfileText.equals(lastProfileText)) {
                    // If the user hasn't hand edited the profile since the last profile builder
                    // run, remember the preferences. Otherwise, let the user start over with a
                    // clean slate.
                    profileUserData =
                            EgoEaterPreferences.getProfileBuilderLastUserData(getActivity());
                } else {
                    profileUserData = new ProfileUserData();
                }
            } catch (JSONException ex) {
                Log.e(
                        LOG_TAG,
                        "onCreate: Failed to retrieve ProfileUserData from preferences.",
                        ex);
                profileUserData = new ProfileUserData();
            }
        } else  {
            try {
                profileUserData = ProfileUserData.read(savedInstanceState);
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "onCreate: Failed to read profile builder user data.", ex);
            }
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile_builder, container, false);

        discardImageView = (ImageView) rootView.findViewById(R.id.discardImageView);
        titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        resetTextView = (TextView) rootView.findViewById(R.id.resetTextView);
        profileBuilderViewPager = (ViewPager) rootView.findViewById(R.id.profileBuilderViewPager);
        pageIndexTextView = (TextView) rootView.findViewById(R.id.pageIndexTextView);
        backButton = (Button) rootView.findViewById(R.id.backButton);
        continueButton = (Button) rootView.findViewById(R.id.continueButton);
        saveButton = (Button) rootView.findViewById(R.id.saveButton);

        new ProfileBuilderConfigurationLoadingTask().execute();

        titleTextView.setText(R.string.profile_builder_title);

        discardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((profileUserData != null) && profileUserData.isEmpty()) {
                    // Nothing to lose -> dismiss without confirm dialog!
                    startEditProfileActivity();
                } else {
                    SimpleAlertDialog.confirmDiscard(
                            getContext(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    startEditProfileActivity();
                                }
                            });
                }
            }
        });

        resetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleAlertDialog.confirm(
                        getActivity(),
                        R.string.confirm_reset_title,
                        R.string.confirm_reset_body,
                        new Runnable() {
                            @Override
                            public void run() {
                                onResetConfirmed();
                            }
                        }
                );
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackClicked();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onContinueClicked();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        return rootView;
    }

    private void onResetConfirmed() {
        profileUserData = new ProfileUserData();
        profileBuilderViewPager.setCurrentItem(0);
        profilePagerAdapter.notifyDataSetChanged();
    }

    private void onContinueClicked() {
        int currentIndex = profileBuilderViewPager.getCurrentItem();
        if (currentIndex < profilePagerAdapter.getCount() - 1) {
            profileBuilderViewPager.setCurrentItem(currentIndex + 1);
        }
    }

    private void onBackClicked() {
        int currentIndex = profileBuilderViewPager.getCurrentItem();
        if (currentIndex > 0) {
            profileBuilderViewPager.setCurrentItem(currentIndex - 1);
        }
    }

    private void onSaveClicked() {
        if (StringUtil.isEmpty(EgoEaterPreferences.getProfileText(getActivity()))) {
            // The user hasn't filled out the profile text yet. Skip the confirm dialog.
            onSaveConfirmed();
        } else {
            SimpleAlertDialog.confirm(
                    getActivity(),
                    R.string.confirm_save_profile_title,
                    R.string.confirm_save_profile_body,
                    new Runnable() {
                        @Override
                        public void run() {
                            onSaveConfirmed();
                        }
                    });
        }
    }

    private void onSaveConfirmed() {
        ((ActivityWithProgressDialog) getActivity()).showLoadingProgress();
        String profileStr = profileUserData.toString(getContext());
        new SaveProfileClientAction(
                getContext(),
                profileStr,
                new ApiClientAction.Callback<Void>() {
                    @Override
                    public void onResult(Void data) {
                        onSaveComplete();
                    }
                }).execute();

        // Save the last profile builder state to the preferences, so that the user can pick off
        // where he or she left off.
        try {
            EgoEaterPreferences.setProfileBuilderLastUserData(getActivity(), profileUserData);
            EgoEaterPreferences.setProfileBuilderLastProfileText(getActivity(), profileStr);
        } catch (JSONException ex) {
            Log.e(
                    LOG_TAG,
                    "onSaveConfirmed: Failed to save profile builder state to preferences.",
                    ex);
        }
    }

    private void onSaveComplete() {
        // Record any other fields with Google Analytics.
        profileUserData.recordAnalytics(getActivity());

        ((ActivityWithProgressDialog) getActivity()).dismissLoadingProgress();
        startEditProfileActivity();
    }

    private void startEditProfileActivity() {
        getActivity().finish();
        startActivity(new Intent(getContext(), EditProfileActivity.class));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            profileUserData.save(outState);
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "onSaveInstanceState: Failed to save profile builder user data.", ex);
        }
    }

    public ProfileUserData getProfileUserData() {
        return profileUserData;
    }

    public ProfileBuilderConfiguration getProfileBuilderConfiguration() {
        return profileBuilderConfiguration;
    }

    private void refreshButtonVisibility(int position) {
        boolean onLastPage = (position == (profilePagerAdapter.getCount() - 1));
        boolean onFirstPage = profileBuilderViewPager.getCurrentItem() == 0;

        backButton.setVisibility(onFirstPage ? View.INVISIBLE : View.VISIBLE);
        continueButton.setVisibility(onLastPage ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(onLastPage ? View.VISIBLE : View.GONE);
    }

    private void refreshPageIndex() {
        if ((profileBuilderViewPager != null) && (profileBuilderConfiguration != null)) {
            int currentPage = profileBuilderViewPager.getCurrentItem() + 1;
            int pageCount = profileBuilderConfiguration.getQuestions().size() + 2;
            String pageIndexStr =
                    getString(R.string.profile_builder_page_index, currentPage, pageCount);
            pageIndexTextView.setText(pageIndexStr);
        }
    }

    /**
     * An {@link AsyncTask} that loads the question data from the resources and parses the JSON.
     */
    private class ProfileBuilderConfigurationLoadingTask
            extends AsyncTask<Void, Void, ProfileBuilderConfiguration> {

        @Override
        protected ProfileBuilderConfiguration doInBackground(Void... params) {
            try {
                return ProfileBuilderConfiguration.load(getContext());
            } catch (IOException | JSONException ex) {
                Log.e(LOG_TAG, "doInBackground: Failed to load profile builder configuration", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ProfileBuilderConfiguration profileBuilderConfiguration) {
            ProfileBuilderFragment.this.profileBuilderConfiguration = profileBuilderConfiguration;

            // Initiate PagerAdapter.
            profilePagerAdapter =
                    new ProfilePagerAdapter(getFragmentManager(), profileBuilderConfiguration);
            profileBuilderViewPager.setAdapter(profilePagerAdapter);
            profileBuilderViewPager.addOnPageChangeListener(
                    new ButtonVisibilityPageChangeListener());
            profileBuilderViewPager.addOnPageChangeListener(new AnalyticsPageChangeListener());

            refreshButtonVisibility(profileBuilderViewPager.getCurrentItem());
            refreshPageIndex();
        }
    }

    /**
     * A {@link PagerAdapter} that offers one fragment per wizard question and adds a introduction
     * and summary fragment at the end.
     */
    private class ProfilePagerAdapter extends FragmentStatePagerAdapter {

        private final ProfileBuilderConfiguration profileBuilderConfiguration;

        private ProfilePagerAdapter(
                FragmentManager fragmentManager,
                ProfileBuilderConfiguration profileBuilderConfiguration) {

            super(fragmentManager);

            this.profileBuilderConfiguration = profileBuilderConfiguration;
        }

        @Override
        public Fragment getItem(int position) {
            int questionCount = profileBuilderConfiguration.getQuestions().size();
            if (position == 0) {
                return new ProfileBuilderIntroFragment();
            } else if (position < questionCount + 1) {
                int questionIndex = position - 1;
                return ProfileBuilderQuestionFragment.newInstance(questionIndex);
            } else if (position < questionCount + 2) {
                return new ProfileBuilderSummaryFragment();
            } else {
                throw new IllegalStateException("Encountered unexpected position: " + position);
            }
        }

        @Override
        public int getCount() {
            return profileBuilderConfiguration.getQuestions().size() + 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * A {@link OnPageChangeListener} that updates the visibility of the continue and save button.
     */
    private class ButtonVisibilityPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Nothing to do.
        }

        @Override
        public void onPageSelected(int position) {
            refreshButtonVisibility(position);
            refreshPageIndex();

            // Tell the fragment that it has become visible to update itself.
            Fragment fragment = profilePagerAdapter.getItem(position);
            if (fragment instanceof ProfileBuilderQuestionFragment) {
                ((ProfileBuilderQuestionFragment) fragment).refreshPreview();
            }

            // Hide keyboard in case it was opened to edit an other option.
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(backButton.getWindowToken(), 0);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Nothing to do.
        }
    }

    /**
     * A {@link OnPageChangeListener} that reports to Analytics when a new fragment is selected.
     */
    private class AnalyticsPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Nothing to do.
        }

        @Override
        public void onPageSelected(int position) {
            String screenName = ProfileBuilderActivity.class.getSimpleName() + position;
            AnalyticsUtil.reportScreenName(getActivity().getApplication(), screenName);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Nothing to do.
        }
    }
}
