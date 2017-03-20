package com.playposse.egoeater.clientactions;

import android.content.Context;
import android.support.annotation.Nullable;

import com.playposse.egoeater.backend.egoEaterApi.EgoEaterApi;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that signs in the user.
 */
public class SignInClientAction extends ApiClientAction<UserBean> {

    private final Context context;
    private final String fbAccessToken;
    private final String firebaseToken;

    public SignInClientAction(
            Context context,
            String fbAccessToken,
            String firebaseToken,
            @Nullable Callback<UserBean> callback) {

        super(callback);

        this.context = context;
        this.fbAccessToken = fbAccessToken;
        this.firebaseToken = firebaseToken;
    }

    @Override
    protected UserBean executeAsync() throws IOException {
        UserBean userBean = getApi().signIn(fbAccessToken, firebaseToken).execute();

        EgoEaterPreferences.setUser(context, userBean);

        return userBean;
    }
}
