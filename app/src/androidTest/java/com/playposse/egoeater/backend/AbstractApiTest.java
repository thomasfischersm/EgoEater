package com.playposse.egoeater.backend;

import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.EgoEaterApi;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.util.ApiTestUtil;
import com.playposse.egoeater.util.FacebookSecrets;
import com.playposse.egoeater.util.FakeUser;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;

import java.io.IOException;
import java.util.Map;

/**
 * A base class for making remote calls to the API.
 */
public class AbstractApiTest {

    private static final String LOG_TAG = AbstractApiTest.class.getSimpleName();

    private EgoEaterApi egoEaterApi;
    private FakeUser fakeUser;

    protected EgoEaterApi getApi() {
        if (egoEaterApi == null) {
            egoEaterApi = ApiTestUtil.instantiateApi();
        }
        return egoEaterApi;
    }

    protected FakeUser getFakeUser() throws IOException {
        if (fakeUser == null) {
            FakeUser fakeUser = ApiTestUtil.fakeUsers[0];
            readFbAccessTokens(fakeUser);
            signIn(fakeUser, "5678");
            this.fakeUser = fakeUser;
        }
        return fakeUser;
    }

    private static FacebookClient createFacebookClient() {
        return new DefaultFacebookClient(
                FacebookSecrets.APP_TOKEN,
                FacebookSecrets.APP_SECRET,
                Version.VERSION_2_8);
    }

    private static void readFbAccessTokens(FakeUser fakeUser) {
        // Query FB test user ids.
        FacebookClient facebookClient = createFacebookClient();
        JsonObject testUsersResponse = facebookClient.fetchObject(
                FacebookSecrets.APP_ID + "/accounts/test-users",
                JsonObject.class,
                Parameter.with("limit", 100));

        // Query FB test users individually.
        JsonArray dataArray = testUsersResponse.getJsonArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
            JsonObject jsonUser = dataArray.getJsonObject(i);
            String fbUserId = jsonUser.getString("id");
            String accessToken = jsonUser.getString("access_token");
            Log.i(LOG_TAG, "readFbAccessTokens: Read FB user id: " + fbUserId
                    + " access token: " + accessToken);

            if (fbUserId.equals(fakeUser.getfBUserId())) {
                fakeUser.setFbAccessToken(accessToken);
                return;
            }
        }
    }

    private UserBean signIn(
            FakeUser fakeUser,
            String firebaseToken) throws IOException {

        UserBean userBean = getApi()
                .signIn(fakeUser.getFbAccessToken(), firebaseToken)
                .execute();

        fakeUser.setSessionId(userBean.getSessionId());
        return userBean;
    }

    protected long getSessionId() throws IOException {
        return getFakeUser().getSessionId();
    }
}
