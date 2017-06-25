package com.playposse.egoeater.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.ProfileBuilderFragment.QuestionStateHolder;

/**
 * A {@link Fragment} that shows a question to the user and offers options for the user to check
 * off.
 */
public class ProfileBuilderQuestionFragment extends Fragment {

    private static final String QUESTION_INDEX_PARAM = "questionIndex";

    private TextView questionTextView;
    private RecyclerView optionsRecyclerView;

    private int questionIndex;
    private QuestionStateHolder questionStateHolder;

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
//        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ProfileBuilderFragment) {
            ProfileBuilderFragment profileBuilderFragment = (ProfileBuilderFragment) parentFragment;
            questionStateHolder = profileBuilderFragment.getQuestionStateHolder(questionIndex);
        }

        // We can trust that the question configuration is already loaded because question fragments
        // can only be instantiated after the number of questions is known.
        questionTextView.setText(questionStateHolder.getQuestion().getPrompt());
        optionsRecyclerView.setAdapter(new OptionsAdapter());
        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    /**
     * An {@link RecyclerView.Adapter} that shows the options to answer the profile question to the
     * user.
     */
    private class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder> {

        public OptionsAdapter() {
        }

        @Override
        public OptionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View rootView = inflater.inflate(
                    R.layout.profile_question_option_list_item,
                    parent,
                    false);
            return new OptionsViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final OptionsViewHolder holder, int position) {
            final String optionText = questionStateHolder.getQuestion().getOptions().get(position);
            boolean isSelected = questionStateHolder.getSelectedOptions().contains(optionText);

            holder.getOptionCheckBox().setChecked(isSelected);
            holder.getOptionTextView().setText(optionText);

            holder.getOptionCheckBox().setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            boolean isSelected =
                                    questionStateHolder.getSelectedOptions().contains(optionText);

                            if (isChecked && !isSelected) {
                                questionStateHolder.getSelectedOptions().add(optionText);
                            } else if (!isChecked && isSelected) {
                                questionStateHolder.getSelectedOptions().remove(optionText);
                            }
                        }
                    });

            holder.getOptionTextView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.getOptionCheckBox().performClick();
                }
            });
        }

        @Override
        public int getItemCount() {
            return questionStateHolder.getQuestion().getOptions().size();
        }

        /**
         * A {@link RecyclerView.ViewHolder} for the layout to show an option.
         */
        class OptionsViewHolder extends RecyclerView.ViewHolder {

            private CheckBox optionCheckBox;
            private TextView optionTextView;

            OptionsViewHolder(View rootView) {
                super(rootView);

                optionCheckBox = (CheckBox) rootView.findViewById(R.id.optionCheckBox);
                optionTextView = (TextView) rootView.findViewById(R.id.optionTextView);
            }

            CheckBox getOptionCheckBox() {
                return optionCheckBox;
            }

            TextView getOptionTextView() {
                return optionTextView;
            }
        }
    }
}
