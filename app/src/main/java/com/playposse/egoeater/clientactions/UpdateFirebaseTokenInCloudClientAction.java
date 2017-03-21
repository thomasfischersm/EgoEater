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
        Long sessionId = EgoEaterPreferences.getSessionId(context);

        EgoEaterPreferences.setFirebaseToken(context, firebaseToken);

        if (sessionId != null) {
            // The sessionId may not have been set if the user has never logged in. So, skip the
            // call to the cloud. The user will register eventually. That call will set the
            // Firebase token in the cloud.
            getApi()
                    .updateFireBaseToken(sessionId, firebaseToken)
                    .execute();
        }

        return null;
    }
}
