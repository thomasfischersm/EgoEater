package com.playposse.egoeater.data.profilewizard;

import android.content.Context;

import com.playposse.egoeater.R;
import com.playposse.egoeater.util.IoUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A data class that holds the configuration for the profile builder
 */
public class ProfileBuilderConfiguration {

    private static final String QUESTIONS_NAME = "questions";

    private final List<ProfileQuestion> questions = new ArrayList<>();

    private ProfileBuilderConfiguration(JSONObject jsonObject) throws JSONException {
        JSONArray questionsArray = jsonObject.getJSONArray(QUESTIONS_NAME);

        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject questionObject = questionsArray.getJSONObject(i);
            questions.add(new ProfileQuestion(questionObject));
        }
    }

    public List<ProfileQuestion> getQuestions() {
        return questions;
    }

    public static ProfileBuilderConfiguration load(Context context)
            throws IOException, JSONException {

        byte[] content = IoUtil.readRawResource(context, R.raw.profile_wizard_configuration);
        JSONObject jsonObject = new JSONObject(new String(content));

        return new ProfileBuilderConfiguration(jsonObject);
    }
}
