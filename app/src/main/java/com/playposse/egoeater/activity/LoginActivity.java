package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SignInClientAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LoginActivity extends ParentActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private LoginButton loginButton;

    private CallbackManager callbackManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions("public_profile", "email");
        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(LOG_TAG, "Facebook login successful: " + loginResult.getAccessToken());
                Log.i(LOG_TAG, "app id " + loginResult.getAccessToken().getApplicationId());
                Log.i(LOG_TAG, "token " + loginResult.getAccessToken().getToken().length());
                onFbLoginCompleted(loginResult);
            }

            @Override
            public void onCancel() {
                Log.e(LOG_TAG, "Facebook login was canceled.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(LOG_TAG, "Facebook login failed: " + error.getMessage());
            }
        });
        Log.i(LOG_TAG, "Facebook callback registered.");

        // Apparently, the session ID is dead or something else requires trying to login again if
        // there is an access token but no valid session id.
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        debug();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.i(LOG_TAG, "LoginActivity.onActivityResult has been called.");
    }

    private void onFbLoginCompleted(LoginResult loginResult) {
        Log.i(LOG_TAG, "onFbLoginCompleted: Got FB login.");
        showLoadingProgress();
        new SignInClientAction(
                this,
                loginResult.getAccessToken().getToken(),
                new ApiClientAction.Callback<UserBean>() {
                    @Override
                    public void onResult(UserBean data) {
                        onCloudSignInCompleted(data);
                    }
                }).execute();
    }

    private void onCloudSignInCompleted(UserBean data) {
        Log.i(LOG_TAG, "onCloudSignInCompleted: Got session id from the server: "
                + data.getSessionId());
        dismissLoadingProgress();
    }

    public static void debug() {
        outputKey("86:C8:B5:86:20:1C:4E:84:62:12:00:3E:09:6B:9E:91"); // MD5
        outputKey("AE:D6:52:11:CE:E2:FB:AC:3E:A9:ED:AF:C3:4B:DA:04:47:8C:84:74"); // SHA1
    }

    private static void outputKey(String shaStr) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (String number : shaStr.split(":")) {
            outputStream.write(Integer.parseInt(number, 16));
        }
        byte[] sha = outputStream.toByteArray();
        String base64 = Base64.encodeToString(sha, 0);
        Log.i(LOG_TAG, shaStr + " -> " + base64);
    }
}
