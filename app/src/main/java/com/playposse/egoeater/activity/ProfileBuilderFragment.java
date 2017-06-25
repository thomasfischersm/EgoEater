package com.playposse.egoeater.activity;

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
import android.widget.Button;

import com.playposse.egoeater.R;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SaveProfileClientAction;
import com.playposse.egoeater.data.profilewizard.ProfileBuilderConfiguration;
import com.playposse.egoeater.data.profilewizard.ProfileQuestion;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.SimpleAlertDialog;
import com.playposse.egoeater.util.StringUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link Fragment} that shows multiple fragments, that the user can swipe through, to build a
 * profile.
 */
public class ProfileBuilderFragment extends Fragment {

    private static final String LOG_TAG = ProfileBuilderFragment.class.getSimpleName();

    /**
     * A {@link Map} between the question index and the selected options.
     */
    private final Map<Integer, List<String>> selectedOptionsMap = new HashMap<>();

    /**
     * A {@link List} that holds the question indexes in the order that the user wants to present
     * them in the final profile text.
     */
    private final List<Integer> orderedQuestionIndexList = new ArrayList<>();

    private ViewPager profileBuilderViewPager;
    private Button discardButton;
    private Button continueButton;
    private Button saveButton;

    private ProfileBuilderConfiguration profileBuilderConfiguration;
    private ProfilePagerAdapter profilePagerAdapter;

    public ProfileBuilderFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            selectedOptionsMap.clear();
            for (String key : savedInstanceState.keySet()) {
                try {
                    int questionIndex = Integer.parseInt(key);
                    selectedOptionsMap.put(questionIndex, savedInstanceState.getStringArrayList(key));
                } catch (NumberFormatException ex) {
                    // Ignore keys that aren't numbers.
                }
            }
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile_builder, container, false);

        profileBuilderViewPager = (ViewPager) rootView.findViewById(R.id.profileBuilderViewPager);
        discardButton = (Button) rootView.findViewById(R.id.discardButton);
        continueButton = (Button) rootView.findViewById(R.id.continueButton);
        saveButton = (Button) rootView.findViewById(R.id.saveButton);

        new ProfileBuilderConfigurationLoadingTask().execute();

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDiscardClicked();
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

    private void onContinueClicked() {
        int currentIndex = profileBuilderViewPager.getCurrentItem();
        if (currentIndex < profilePagerAdapter.getCount() - 1) {
            profileBuilderViewPager.setCurrentItem(currentIndex + 1);
        }
    }

    private void onDiscardClicked() {
        SimpleAlertDialog.confirmDiscard(
                getContext(),
                new Runnable() {
                    @Override
                    public void run() {
                        startEditProfileActivity();
                    }
                });
    }

    private void onSaveClicked() {
        ((ActivityWithProgressDialog) getActivity()).showLoadingProgress();
        String profileStr = getSummaryStateHolder().getProfileString();
        new SaveProfileClientAction(
                getContext(),
                profileStr,
                new ApiClientAction.Callback<Void>() {
                    @Override
                    public void onResult(Void data) {
                        onSaveComplete();
                    }
                }).execute();
    }

    private void onSaveComplete() {
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

        for (Entry<Integer, List<String>> entry : selectedOptionsMap.entrySet()) {
            String key = entry.getKey().toString();
            ArrayList<String> value = new ArrayList<>(entry.getValue());
            outState.putStringArrayList(key, value);
        }
    }

    public QuestionStateHolder getQuestionStateHolder(int questionIndex) {
        return new QuestionStateHolder(questionIndex);
    }

    public SummaryStateHolder getSummaryStateHolder() {
        return new SummaryStateHolder();
    }

    private void refreshButtonVisibility(int position) {
        if (position < profilePagerAdapter.getCount() - 1) {
            continueButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
        } else {
            continueButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

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

    /**
     * A callback for child {@link Fragment}s to retrieve question configurations and store
     * question responses.
     */
    class QuestionStateHolder {

        private final int questionIndex;

        private QuestionStateHolder(int questionIndex) {
            this.questionIndex = questionIndex;
        }

        ProfileQuestion getQuestion() {
            return profileBuilderConfiguration.getQuestions().get(questionIndex);
        }

        List<String> getSelectedOptions() {
            if (!selectedOptionsMap.containsKey(questionIndex)) {
                selectedOptionsMap.put(questionIndex, new ArrayList<String>());
            }
            return selectedOptionsMap.get(questionIndex);
        }

        public void saveAnswer(List<String> selectedOptions) {
            selectedOptionsMap.put(questionIndex, selectedOptions);
        }
    }

    /**
     * A callback for child {@link Fragment}s to access all selected options and to re-order them.
     */
    class SummaryStateHolder {

        void init() {
            orderedQuestionIndexList.clear();
            for (int i = 0; i < profileBuilderConfiguration.getQuestions().size(); i++) {
                if ((selectedOptionsMap.containsKey(i)) && (selectedOptionsMap.get(i).size() > 0)) {
                    orderedQuestionIndexList.add(i);
                }
            }
        }

        List<String> getSelectedOptions(int positionIndex) {
            int questionIndex = orderedQuestionIndexList.get(positionIndex);
            return selectedOptionsMap.get(questionIndex);
        }

        String getSelectedOptionsString(int positionIndex) {
            String optionsSeparator = getString(R.string.profile_options_separator);
            List<String> selectedOptions = getSelectedOptions(positionIndex);
            if ((selectedOptions != null) && (selectedOptions.size()> 0)) {
                return StringUtil.concat(selectedOptions, optionsSeparator);
            } else {
                return null;
            }
        }

        String getProfileString() {
            String questionsSeparator = getString(R.string.profile_questions_separator);
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < getCount(); i++) {
                if (sb.length() > 0) {
                    sb.append(questionsSeparator);
                }
                sb.append(getSelectedOptionsString(i));
            }
            return sb.toString();
        }

        int getCount() {
            return orderedQuestionIndexList.size();
        }

        void move(int fromPositionIndex, int toPositionIndex) {
            int questionIndex = orderedQuestionIndexList.remove(fromPositionIndex);
            orderedQuestionIndexList.add(toPositionIndex, questionIndex);
        }
    }
}
