package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that updates the Firebase token in the cloud.
 */
public class UpdateFirebaseTokenInCloudClientAction extends ApiClientAction<Void> {

    private final Context context;

    public UpdateFirebaseTokenInCloudClientAction(Context context) {
        this.context = context;
    }

    @Override
    protected Void executeAsync() throws IOException {
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();
        long sessionId = EgoEaterPreferences.getSessionId(context);

        EgoEaterPreferences.setFirebaseToken(context, firebaseToken);

        getApi()
                .updateFireBaseToken(sessionId, firebaseToken)
                .execute();

        return null;
    }
}
