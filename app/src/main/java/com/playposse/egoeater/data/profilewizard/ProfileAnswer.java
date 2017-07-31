package com.playposse.egoeater.data.profilewizard;

import android.content.Context;

import com.playposse.egoeater.R;
import com.playposse.egoeater.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A user's answer to a {@link ProfileQuestion}.
 */
public class ProfileAnswer {

    private static final String QUESTION_INDEX_NAME = "questionIndex";
    private static final String SELECTED_OPTIONS_NAME = "selectedOptions";
    private static final String OTHER_ANSWER_NAME = "otherAnswer";

    private final int questionIndex;
    private final List<String> selectedOptions = new ArrayList<>();

    private String otherAnswer = null;

    public ProfileAnswer(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public ProfileAnswer(JSONObject jsonObject) throws JSONException {
        questionIndex = jsonObject.getInt(QUESTION_INDEX_NAME);
        if (jsonObject.isNull(OTHER_ANSWER_NAME)) {
            otherAnswer = null;
        } else {
            otherAnswer = jsonObject.getString(OTHER_ANSWER_NAME);
        }

        JSONArray selectedOptionsArray = jsonObject.getJSONArray(SELECTED_OPTIONS_NAME);
        for (int i = 0; i < selectedOptionsArray.length(); i++) {
            String selectedOption = selectedOptionsArray.getString(i);
            selectedOptions.add(selectedOption);
        }
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public List<String> getSelectedOptions() {
        return selectedOptions;
    }

    public void setOtherAnswer(String otherAnswer) {
        this.otherAnswer = otherAnswer;
    }

    public String getOtherAnswer() {
        return otherAnswer;
    }

    public boolean isEmpty() {
        return (selectedOptions.size() == 0) && (StringUtil.isEmpty(otherAnswer));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject answerObject = new JSONObject();
        answerObject.put(QUESTION_INDEX_NAME, questionIndex);
        answerObject.put(OTHER_ANSWER_NAME, otherAnswer);
        answerObject.put(SELECTED_OPTIONS_NAME, new JSONArray(selectedOptions));
        return answerObject;
    }

    public String toString(Context context) {
        String optionsSeparator = context.getString(R.string.profile_options_separator);
        boolean hasSelectedOptions = (selectedOptions != null) && (selectedOptions.size() > 0);
        boolean hasOtherOption = !StringUtil.isEmpty(otherAnswer);

        if (hasSelectedOptions || hasOtherOption) {
            List<String> tmp = this.selectedOptions;
            if (hasOtherOption) {
                tmp = new ArrayList<>(tmp);
                tmp.add(otherAnswer);
            }
            return StringUtil.concat(tmp, optionsSeparator);
        } else {
            return null;
        }
    }
}
