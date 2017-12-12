package com.playposse.egoeater.clientactions;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that signs in the user.
 */
public class SignInClientAction extends ApiClientAction<UserBean> {

    private static final String LOG_TAG = SignInClientAction.class.getSimpleName();

    private final Context context;
    private final String fbAccessToken;

    public SignInClientAction(
            Context context,
            String fbAccessToken,
            @Nullable Callback<UserBean> callback) {

        super(context, callback);

        this.context = context;
        this.fbAccessToken = fbAccessToken;
    }

    @Override
    protected UserBean executeAsync() throws IOException {
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();

        UserBean userBean = getApi().signIn(fbAccessToken, firebaseToken).execute();
        Log.i(LOG_TAG, "executeAsync: Called to cloud for signIn has completed.");

        EgoEaterPreferences.setUser(context, userBean);

        return userBean;
    }
}
