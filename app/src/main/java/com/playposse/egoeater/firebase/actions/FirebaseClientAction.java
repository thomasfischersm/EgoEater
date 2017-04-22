package com.playposse.egoeater.firebase.actions;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;
import com.playposse.egoeater.util.ToastUtil;

/**
 * A base class for Firebase actions.
 */
public abstract class FirebaseClientAction {

    private final RemoteMessage remoteMessage;

    private Context applicationContext;

    public FirebaseClientAction(RemoteMessage remoteMessage) {
        this.remoteMessage = remoteMessage;
    }

    protected abstract void execute(RemoteMessage remoteMessage);

    public void execute(Context applicationContext) {
        this.applicationContext = applicationContext;

        execute(remoteMessage);
    }

    protected Context getApplicationContext() {
        return applicationContext;
    }

    protected void startActivity(Intent intent) {
        getApplicationContext().startActivity(intent);
    }

    protected void sendToast(String message) {
        ToastUtil.sendToast(getApplicationContext(), message);
    }

    protected String getString(int resId) {
        return applicationContext.getString(resId);
    }
}
