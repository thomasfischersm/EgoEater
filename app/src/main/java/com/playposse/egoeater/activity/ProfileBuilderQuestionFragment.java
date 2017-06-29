package com.playposse.egoeater.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.data.profilewizard.ProfileAnswer;
import com.playposse.egoeater.data.profilewizard.ProfileBuilderConfiguration;
import com.playposse.egoeater.data.profilewizard.ProfileQuestion;
import com.playposse.egoeater.data.profilewizard.ProfileUserData;
import com.playposse.egoeater.util.StringUtil;

import java.util.List;

/**
 * A {@link Fragment} that shows a question to the user and offers options for the user to check
 * off.
 */
public class ProfileBuilderQuestionFragment extends Fragment {

    private static final String LOG_TAG = ProfileBuilderQuestionFragment.class.getSimpleName();

    private static final String QUESTION_INDEX_PARAM = "questionIndex";

    private TextView questionTextView;
    private RecyclerView optionsRecyclerView;

    private int questionIndex;
    private ProfileBuilderConfiguration profileBuilderConfiguration;
    private ProfileUserData profileUserData;

    public ProfileBuilderQuestionFragment() {
        // Required empty public constructor
    }

    public static ProfileBuilderQuestionFragment newInstance(int questionIndex) {
        ProfileBuilderQuestionFragment fragment = new ProfileBuilderQuestionFragment();
        Bundle args = new Bundle();
        args.putInt(QUESTION_INDEX_PARAM, questionIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionIndex = getArguments().getInt(QUESTION_INDEX_PARAM);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView =
                inflater.inflate(R.layout.fragment_profile_builder_question, container, false);

        questionTextView = (TextView) rootView.findViewById(R.id.questionTextView);
        optionsRecyclerView = (RecyclerView) rootView.findViewById(R.id.optionsRecyclerView);

        Fragment parentFragment = getFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (parentFragment instanceof ProfileBuilderFragment) {
            ProfileBuilderFragment profileBuilderFragment = (ProfileBuilderFragment) parentFragment;
            profileBuilderConfiguration = profileBuilderFragment.getProfileBuilderConfiguration();
            profileUserData = profileBuilderFragment.getProfileUserData();
        }

        // We can trust that the question configuration is already loaded because question fragments
        // can only be instantiated after the number of questions is known.
        String questionPrompt =
                profileBuilderConfiguration.getQuestions().get(questionIndex).getPrompt();
        questionTextView.setText(questionPrompt);
        optionsRecyclerView.setAdapter(new OptionsAdapter());
        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    /**
     * An {@link RecyclerView.Adapter} that shows the options to answer the profile question to the
     * user.
     */
    private class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder> {

        private static final int REGULAR_VIEW_TYPE = 1;
        private static final int OTHER_VIEW_TYPE = 2;

        OptionsAdapter() {
        }

        @Override
        public int getItemViewType(int position) {
            if (position < getItemCount() - 1) {
                return REGULAR_VIEW_TYPE;
            } else {
                return OTHER_VIEW_TYPE;
            }
        }

        @Override
        public OptionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            if (viewType == REGULAR_VIEW_TYPE) {
                View rootView = inflater.inflate(
                        R.layout.profile_question_option_list_item,
                        parent,
                        false);
                return new RegularOptionsViewHolder(rootView);
            } else if (viewType == OTHER_VIEW_TYPE) {
                View rootView = inflater.inflate(
                        R.layout.profile_question_option_list_other_item,
                        parent,
                        false);
                return new OtherOptionViewHolder(rootView);
            } else {
                throw new IllegalStateException("Encountered unexpected view type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(OptionsViewHolder holder, int position) {
            holder.onBindViewHolder(position);
        }

        @Override
        public int getItemCount() {
            ProfileQuestion question =
                    profileBuilderConfiguration.getQuestions().get(questionIndex);
            return question.getOptions().size() + 1;
        }

        /**
         * A {@link RecyclerView.ViewHolder} for the layout to show an option.
         */
        abstract class OptionsViewHolder extends RecyclerView.ViewHolder {

            private final CheckBox optionCheckBox;

            OptionsViewHolder(View rootView) {
                super(rootView);

                optionCheckBox = (CheckBox) rootView.findViewById(R.id.optionCheckBox);
            }

            abstract void onBindViewHolder(int position);

            CheckBox getOptionCheckBox() {
                return optionCheckBox;
            }
        }

        /**
         * A {@link RecyclerView.ViewHolder} for regular options.
         */
        class RegularOptionsViewHolder extends OptionsViewHolder {

            private final TextView optionTextView;

            RegularOptionsViewHolder(View rootView) {
                super(rootView);

                optionTextView = (TextView) rootView.findViewById(R.id.optionTextView);
            }

            @Override
            void onBindViewHolder(int position) {
                ProfileQuestion question =
                        profileBuilderConfiguration.getQuestions().get(questionIndex);
                final String optionText = question.getOptions().get(position);
                ProfileAnswer answer =
                        profileUserData.getAnswer(questionIndex);
                final List<String> selectedOptions = answer.getSelectedOptions();
                boolean isSelected = selectedOptions.contains(optionText);

                getOptionCheckBox().setChecked(isSelected);
                getOptionTextView().setText(optionText);

                getOptionCheckBox().setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                boolean isSelected =
                                        selectedOptions.contains(optionText);

                                if (isChecked && !isSelected) {
                                    selectedOptions.add(optionText);
                                } else if (!isChecked && isSelected) {
                                    selectedOptions.remove(optionText);
                                }
                            }
                        });

                getOptionTextView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getOptionCheckBox().performClick();
                    }
                });
            }

            TextView getOptionTextView() {
                return optionTextView;
            }
        }

        /**
         * A {@link RecyclerView.ViewHolder} that is used for the other item at the bottom of the
         * list. It has an additional text input for the user to enter a custom option.
         */
        class OtherOptionViewHolder extends OptionsViewHolder {

            private final EditText otherOptionEditText;

            OtherOptionViewHolder(View rootView) {
                super(rootView);

                otherOptionEditText = (EditText) rootView.findViewById(R.id.otherOptionEditText);

                getOtherOptionEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Ignore.
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Log.d(LOG_TAG, "onTextChanged: called");
                        // Ignore.
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d(LOG_TAG, "afterTextChanged: called");
                        getOptionCheckBox().post(new Runnable() {
                            @Override
                            public void run() {
                                ProfileAnswer answer =
                                        profileUserData.getAnswer(questionIndex);
                                String otherAnswer = StringUtil.getCleanString(otherOptionEditText);
                                answer.setOtherAnswer(otherAnswer);

                                boolean isNotEmpty = !StringUtil.isEmpty(otherAnswer);
                                boolean isOtherChecked = getOptionCheckBox().isChecked();
                                if (isNotEmpty != isOtherChecked) {
                                    getOptionCheckBox().setChecked(isNotEmpty);
                                    Log.d(LOG_TAG, "afterTextChanged: Changed checkbox: "
                                            + isNotEmpty);
                                }
                            }
                        });
                    }
                });
            }

            @Override
            void onBindViewHolder(int position) {
                final ProfileAnswer answer =
                        profileUserData.getAnswer(questionIndex);
                String otherAnswer = answer.getOtherAnswer();
                boolean isOtherSelected = !StringUtil.isEmpty(otherAnswer);

                getOptionCheckBox().setChecked(isOtherSelected);
                getOtherOptionEditText().setText(otherAnswer);

                getOptionCheckBox().setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView,
                                    boolean isChecked) {

                                if (!isChecked) {
                                    getOtherOptionEditText().setText("");
                                    answer.setOtherAnswer(null);
                                }
                            }
                        });
            }

            public EditText getOtherOptionEditText() {
                return otherOptionEditText;
            }
        }
    }
}
