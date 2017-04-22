package com.playposse.egoeater.firebase.actions;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Handles a Firebase notification that the cloud has new matches.
 */
public class NotifyNewMatchesClientAction extends FirebaseClientAction {

    private static final String LOG_TAG = NotifyNewMatchesClientAction.class.getSimpleName();

    public NotifyNewMatchesClientAction(RemoteMessage remoteMessage) {
        super(remoteMessage);
    }

    @Override
    protected void execute(RemoteMessage remoteMessage) {
        Log.i(LOG_TAG, "execute: Received notifcation of new matches.");
    }
}
