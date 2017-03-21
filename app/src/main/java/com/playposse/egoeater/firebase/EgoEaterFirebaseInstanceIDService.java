package com.playposse.egoeater.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.playposse.egoeater.clientactions.UpdateFirebaseTokenInCloudClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;

/**
 * A Firebase service that receives updates of the Firebase id.
 */
public class EgoEaterFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = EgoEaterFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        String actualFirebaseToken = FirebaseInstanceId.getInstance().getToken();
        String storedFirebaseToken =
                EgoEaterPreferences.getFirebaseToken(getApplicationContext());

        if ((actualFirebaseToken != null)
                && (storedFirebaseToken != null)
                && (!actualFirebaseToken.equals(storedFirebaseToken))) {
            updateFireBaseTokenInCloud(getApplicationContext());
        }

        Log.i(LOG_TAG, "EgoEaterFirebaseInstanceIDService.onCreate completed with token: "
                + actualFirebaseToken);
    }

    @Override
    public void onTokenRefresh() {
        Log.i(LOG_TAG, "EgoEaterFirebaseInstanceIDService.onTokenRefresh started with token: "
                + FirebaseInstanceId.getInstance().getToken());

        updateFireBaseTokenInCloud(getApplicationContext());
    }

    private void updateFireBaseTokenInCloud(Context context) {
        new UpdateFirebaseTokenInCloudClientAction(context).execute();
    }
}
