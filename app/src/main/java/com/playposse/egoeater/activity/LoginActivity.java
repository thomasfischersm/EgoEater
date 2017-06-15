package com.playposse.egoeater.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.SignInClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.services.PopulatePipelineService;
import com.playposse.egoeater.util.SimpleAlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LoginActivity extends ParentActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private static final String HAS_SHOWN_SESSION_EXPIRATION_DIALOG =
            "hasShownSessionExpirationDialog";

    private TextView logoTextView;
    private Button loginButton;

    private CallbackManager callbackManager;
    private boolean hasShownSessionExpirationDialog = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logoTextView = (TextView) findViewById(R.id.logoTextView);
        loginButton = (Button) findViewById(R.id.loginButton);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(
                        LoginActivity.this,
                        Arrays.asList("public_profile", "email", "user_birthday"));
            }
        });

        // Load the external font for the logo.
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/nexa_bold.otf");
        logoTextView.setTypeface(typeface);

        showSessionExpiredDialogIfRequested(savedInstanceState);
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

        // Kick off building the pipeline.
        PopulatePipelineService.startService(this, PipelineLogTable.SIGN_IN_TRIGGER);

        dismissLoadingProgress();
        GlobalRouting.onLoginComplete(this);
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

    private void showSessionExpiredDialogIfRequested(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            hasShownSessionExpirationDialog =
                    savedInstanceState.getBoolean(HAS_SHOWN_SESSION_EXPIRATION_DIALOG, false);
        }

        if (ExtraConstants.hasSessionExpired(getIntent()) && !hasShownSessionExpirationDialog) {
            SimpleAlertDialog.alert(
                    this,
                    R.string.session_expired_dialog_title,
                    R.string.session_expired_dialog_message);
            hasShownSessionExpirationDialog = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(HAS_SHOWN_SESSION_EXPIRATION_DIALOG, hasShownSessionExpirationDialog);
    }
}
