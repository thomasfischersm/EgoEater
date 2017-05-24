package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that saves the profile.
 */
public class SaveProfileClientAction extends ApiClientAction<Void> {

    private String profileText;

    public SaveProfileClientAction(Context context, String profileText, Callback<Void> callback) {
        super(context, callback);

        // Prevent the call failing due to a missing parameter.
        if ((profileText == null) || (profileText.equals(""))) {
            this.profileText = " ";
        } else {
            this.profileText = profileText;
        }
    }

    @Override
    protected Void executeAsync() throws IOException {
        // Call cloud
        UserBean userBean = getApi().saveProfile(getSessionId(), profileText).execute();

        // Store the change in the preferences.
        EgoEaterPreferences.setUser(getContext(), userBean);

        return null;
    }
}
