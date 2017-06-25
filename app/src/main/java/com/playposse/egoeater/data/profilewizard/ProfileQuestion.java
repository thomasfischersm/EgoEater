package com.playposse.egoeater.data.profilewizard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A data object that represents a profile question with its possible answers.
 */
public class ProfileQuestion {

    private static final String PROMPT_NAME = "prompt";
    private static final String OPTIONS_NAME = "options";

    private final String prompt;
    private final List<String> options = new ArrayList<>();

    ProfileQuestion(JSONObject questionObject) throws JSONException {
        prompt = questionObject.getString(PROMPT_NAME);
        JSONArray optionsArray = questionObject.getJSONArray(OPTIONS_NAME);

        for (int i = 0; i < optionsArray.length(); i++) {
            options.add(optionsArray.getString(i));
        }
    }

    public String getPrompt() {
        return prompt;
    }

    public List<String> getOptions() {
        return options;
    }
}
