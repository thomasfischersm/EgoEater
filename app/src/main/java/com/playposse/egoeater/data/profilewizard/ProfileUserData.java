package com.playposse.egoeater.data.profilewizard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.playposse.egoeater.R;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.AnalyticsUtil.AnalyticsCategory;
import com.playposse.egoeater.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.playposse.egoeater.activity.EditProfileActivity.MAX_PROFILE_CHARACTER_COUNT;

/**
 * Stores the profile data that a user enters to generate the profile text.
 */
public class ProfileUserData {

    private static final String LOG_TAG = ProfileUserData.class.getSimpleName();

    private static final String BUNDLE_KEY = "profileData";

    private static final String ANSWERS_NAME = "answers";
    private static final String ORDER_NAME = "order";

    private Map<Integer, ProfileAnswer> questionIndexToAnswerMap = new HashMap<>();

    /**
     * A list that contains the order of the answers as the user decides to put them into the
     * profile text.
     */
    private List<Integer> answersOrder = new ArrayList<>();

    public ProfileUserData() {
    }

    private ProfileUserData(JSONObject jsonObject) throws JSONException {
        JSONArray answersArray = jsonObject.getJSONArray(ANSWERS_NAME);

        for (int i = 0; i < answersArray.length(); i++) {
            JSONObject answerObject = answersArray.getJSONObject(i);
            ProfileAnswer answer = new ProfileAnswer(answerObject);
            questionIndexToAnswerMap.put(answer.getQuestionIndex(), answer);
        }

        JSONArray orderArray = jsonObject.getJSONArray(ORDER_NAME);
        for (int i = 0; i < orderArray.length(); i++) {
            answersOrder.add(orderArray.getInt(i));
        }
    }

    public ProfileAnswer getAnswer(int questionIndex) {
        ProfileAnswer answer = questionIndexToAnswerMap.get(questionIndex);
        if (answer == null) {
            answer = new ProfileAnswer(questionIndex);
            questionIndexToAnswerMap.put(questionIndex, answer);
        }
        return answer;
    }

    public List<Integer> getAnswersOrder() {
        return answersOrder;
    }

    public void move(int fromPositionIndex, int toPositionIndex) {
        int questionIndex = answersOrder.remove(fromPositionIndex);
        answersOrder.add(toPositionIndex, questionIndex);
    }

    /**
     * Rebuilds, the order of the answers. The user can use drag'n'drop to specify the answer.
     * This method will build the initial order of answers. It will also update the order based
     * on options that users add and remove.
     */
    public void refreshOrder() {
        // Remove user removed answers.
        Iterator<Integer> orderIterator = answersOrder.iterator();
        while (orderIterator.hasNext()) {
            Integer questionIndex = orderIterator.next();
            if (!questionIndexToAnswerMap.keySet().contains(questionIndex)) {
                orderIterator.remove();
            } else {
                ProfileAnswer answer = questionIndexToAnswerMap.get(questionIndex);
                if (answer.isEmpty()) {
                    orderIterator.remove();
                }
            }
        }

        // Add new answers.
        for (int questionIndex : questionIndexToAnswerMap.keySet()) {
            if (!answersOrder.contains(questionIndex)) {
                ProfileAnswer answer = questionIndexToAnswerMap.get(questionIndex);
                if (!answer.isEmpty()) {
                    answersOrder.add(questionIndex);
                }
            }
        }
    }

    /**
     * Reads the user's answer to the profile builder from the saved bundle of the {@link Fragment}.
     */
    public static ProfileUserData read(Bundle savedInstanceState) throws JSONException {
        String json = savedInstanceState.getString(BUNDLE_KEY);
        JSONObject jsonObject = new JSONObject(json);
        return new ProfileUserData(jsonObject);
    }

    /**
     * Saves the user's answer to the profile builder to a {@link Bundle}.
     */
    public void save(Bundle outState) throws JSONException {
        outState.putString(BUNDLE_KEY, toJson());
    }

    private String toJson() throws JSONException {
        JSONArray answersArray = new JSONArray();
        for (ProfileAnswer answer : questionIndexToAnswerMap.values()) {
            answersArray.put(answer.toJson());
        }

        JSONArray orderArray = new JSONArray();
        for (int questionIndex : answersOrder) {
            orderArray.put(questionIndex);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ANSWERS_NAME, answersArray);
        jsonObject.put(ORDER_NAME, orderArray);
        String json = jsonObject.toString();

        Log.d(LOG_TAG, "toJson: Converted profile builder user data to JSON: \n" + json);

        return json;
    }

    public String toString(Context context) {
        String questionsSeparator = context.getString(R.string.profile_questions_separator);
        StringBuilder sb = new StringBuilder();

        for (int questionIndex : answersOrder) {
            ProfileAnswer answer = questionIndexToAnswerMap.get(questionIndex);
            String answerStr = answer.toString(context);
            if (answerStr == null) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(questionsSeparator);
            }

            sb.append(answerStr);
        }

        // Enforce max character count
        if (sb.length() <= MAX_PROFILE_CHARACTER_COUNT) {
            return sb.toString();
        } else {
            return sb.substring(0, MAX_PROFILE_CHARACTER_COUNT);
        }
    }

    public void recordAnalytics(Activity activity) {
        for (ProfileAnswer answer : questionIndexToAnswerMap.values()) {
            String otherAnswer = answer.getOtherAnswer();
            if (!StringUtil.isEmpty(otherAnswer)) {
                AnalyticsUtil.reportEvent(
                        activity.getApplication(),
                        AnalyticsCategory.enteredOtherProfileOption,
                        otherAnswer);
            }
        }
    }
}
