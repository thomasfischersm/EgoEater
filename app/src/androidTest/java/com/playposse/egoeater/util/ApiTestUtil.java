package com.playposse.egoeater.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.EgoEaterApi;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.UploadProfilePhotoToServletClientAction;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.TestUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A utility for handling Facebook API access to create Endpoint tests.
 */
public class ApiTestUtil {

    private static final String LOG_TAG = ApiTestUtil.class.getSimpleName();

    public static final FakeUser[] fakeUsers = new FakeUser[]{
            new FakeUser("119454888594171", "April Doe", "april_yhldleh_doe@tfbnw.net", "2220 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.april_profile, "female", "01/01/1999"),
            new FakeUser("127642391104452", "Cindy Doe", "cindy_lvffjkc_doe@tfbnw.net", "2110 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.cindy_profile, "female", "01/01/1997"),
            new FakeUser("105165483368049", "James Doe", "pszrjrlwux_1491601134@tfbnw.net", "2100 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.james_profile, "male", "01/01/1998"),
            new FakeUser("108501693032719", "Henry Doe", "nafvgfkhrl_1491602576@tfbnw.net", "2230 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.henry_profile, "male", "01/01/1996"),
            new FakeUser("103096803579916", "Asher Doe", "lxbwimnocj_1491955670@tfbnw.net", "2240 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.asher_profile, "male", "01/01/1995"),
            new FakeUser("106239366596830", "Oliver Doe", "henlizaqva_1491955672@tfbnw.net", "2250 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.oliver_profile, "male", "01/01/1994"),
            new FakeUser("106663763220741", "Atticus Doe", "keparaflgy_1491955422@tfbnw.net", "2260 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.atticus_profile, "male", "01/01/1993"),
            new FakeUser("109263179626868", "Milo Doe", "hcbxeolefx_1491957637@tfbnw.net", "2270 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.milo_profile, "male", "01/01/1992"),
            new FakeUser("105536490001009", "Jack Doe", "xoiksdeada_1491957640@tfbnw.net", "2280 4th Street, Santa Monica, CA 90404", com.playposse.egoeater.test.R.raw.jack_profile, "male", "01/01/1991"),
    };

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void initFakeUsers(Context context)
            throws IOException, ExecutionException, InterruptedException {

        Map<String, FakeUser> fBUserIdToFakeUserMap = new HashMap<>();
        for (FakeUser fakeUser : fakeUsers) {
            fBUserIdToFakeUserMap.put(fakeUser.getfBUserId(), fakeUser);
        }

        Log.i(LOG_TAG, "0. initFakeUsers: starting");
        readFbAccessTokens(fBUserIdToFakeUserMap);
        Log.i(LOG_TAG, "1. initFakeUsers: finished reading access tokens");
        createMissingFbTestUsers(fBUserIdToFakeUserMap);
        Log.i(LOG_TAG, "2. initFakeUsers: finished creating test users");
        signInUsers(fBUserIdToFakeUserMap);
        Log.i(LOG_TAG, "3. initFakeUsers: finished signing users in");
        updateLocation(context, fBUserIdToFakeUserMap);
        Log.i(LOG_TAG, "4. initFakeUsers: finished updating the location");
        updateProfilePhoto(context, fBUserIdToFakeUserMap);
        Log.i(LOG_TAG, "5. initFakeUsers: finished updating the profile photo");
    }

    private static TestUser createFbTestUser(String name) {
        FacebookClient facebookClient = createFacebookClient();
        return facebookClient.publish(
                FacebookSecrets.APP_ID + "/accounts/test-users",
                TestUser.class,
                Parameter.with("permissions", "public_profile, email, user_birthday"),
                Parameter.with("installed", "true"),
                Parameter.with("name", name));
    }

    @NonNull
    private static FacebookClient createFacebookClient() {
        return new DefaultFacebookClient(
                FacebookSecrets.APP_TOKEN,
                FacebookSecrets.APP_SECRET,
                Version.VERSION_2_8);
    }

    private static void readFbAccessTokens(Map<String, FakeUser> fBUserIdToFakeUserMap) {
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

            FakeUser fakeUser = fBUserIdToFakeUserMap.get(fbUserId);
            if (fakeUser != null) {
                fakeUser.setFbAccessToken(accessToken);
            }
        }
    }

    /**
     * Checks if a user doesn't have an access_token. If it doesn't, try to create a test FB user.
     */
    private static void createMissingFbTestUsers(Map<String, FakeUser> fBUserIdToFakeUserMap) {
        for (FakeUser fakeUser : fBUserIdToFakeUserMap.values()) {
            if (fakeUser.getFbAccessToken() == null) {
                TestUser fbTestUser = createFbTestUser(fakeUser.getName());
                fakeUser.setTestUser(fbTestUser);
                Log.i(LOG_TAG, "createMissingFbTestUsers: Created FB test user id: "
                        + fakeUser.getName() + " " + fbTestUser.getId()
                        + " email: " + fbTestUser.getEmail());
            }
        }
    }

    private static void signInUsers(Map<String, FakeUser> fBUserIdToFakeUserMap)
            throws IOException {

        for (FakeUser fakeUser : fBUserIdToFakeUserMap.values()) {
            UserBean userBean = signIn(fakeUser, "1234");

            // Verify gender. If the gender is wrong, it has to be correctly manually in FB.
            if (!fakeUser.getGender().equals(userBean.getGender())) {
                Log.w(LOG_TAG, "signInUsers: The gender for user " + fakeUser.getfBUserId()
                        + " is wrong. It is " + userBean.getGender()
                        + ". It should be " + fakeUser.getGender());
            }

            // Verify dob.
            if (!fakeUser.getDob().equals(userBean.getBirthday())) {
                Log.w(LOG_TAG, "signInUsers: The dob for user " + fakeUser.getfBUserId()
                        + " is wrong. It is " + userBean.getBirthday()
                        + ". It should be " + fakeUser.getDob());
            }
        }
    }

    private static UserBean signIn(
            FakeUser fakeUser,
            String firebaseToken) throws IOException {

        UserBean userBean = instantiateApi()
                .signIn(fakeUser.getFbAccessToken(), firebaseToken)
                .execute();

        fakeUser.setSessionId(userBean.getSessionId());
        fakeUser.setProfileId(userBean.getUserId());
        return userBean;
    }

    @SuppressWarnings("Since15")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @TargetApi(Build.VERSION_CODES.N)
    private static void updateLocation(
            final Context context,
            Map<String, FakeUser> fBUserIdToFakeUserMap)
            throws IOException, ExecutionException, InterruptedException {

        final EgoEaterApi api = instantiateApi();
        List<Future<Void>> futures = new ArrayList<>();

        for (final FakeUser fakeUser : fBUserIdToFakeUserMap.values()) {
            // Find GPS information.
            final CompletableFuture<Void> future = new CompletableFuture<>();
            futures.add(future);
            Log.i(LOG_TAG, "updateLocation: Initiate getting location for " + fakeUser.getName());
            HttpGeoCoder.geoCode(context, fakeUser.getAddress(), new HttpGeoCoder.GeoCodeCallback() {
                @Override
                public void onGeoResponse(final String gpsCoordinate) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String[] coordinates = gpsCoordinate.split(",");
                                double latitude = Double.valueOf(coordinates[0]);
                                double longitude = Double.valueOf(coordinates[1]);

                                // Find address information.
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses1 =
                                        geocoder.getFromLocation(latitude, longitude, 1);
                                String city = addresses1.get(0).getLocality();
                                String state = addresses1.get(0).getAdminArea();
                                String country = addresses1.get(0).getCountryName();

                                // Update cloud.
                                api.updateLocation(
                                        fakeUser.getSessionId(),
                                        latitude,
                                        longitude,
                                        city,
                                        state,
                                        country).execute();
                            } catch (IOException ex) {
                                Log.e(
                                        LOG_TAG,
                                        "onGeoResponse: Failed to update location for "
                                                + fakeUser.getAddress(),
                                        ex);
                            }
                            Log.i(LOG_TAG, "run: Done processing location for "
                                    + fakeUser.getName());
                            future.complete(null);
                        }
                    }).start();
                }
            });
        }

        FutureUtil.waitForAll(futures);
    }

    private static void updateProfilePhoto(
            Context context,
            Map<String, FakeUser> fBUserIdToFakeUserMap)
            throws IOException, ExecutionException, InterruptedException {

        final EgoEaterApi api = instantiateApi();

        List<FakeUser> fakeUsers = new ArrayList<>(fBUserIdToFakeUserMap.values());
        for (int i = 0; i < fakeUsers.size(); i++) {
            FakeUser fakeUser = fakeUsers.get(i);
            Long sessionId = fakeUser.getSessionId();
            boolean isMale = fakeUser.getGender().equals("male");

            if (isMale) {
                updateProfilePhoto(context, sessionId, 0, com.playposse.egoeater.test.R.raw.male_profile_photo0);
            } else {
                updateProfilePhoto(context, sessionId, 0, com.playposse.egoeater.test.R.raw.female_profile_photo0);
            }

            if (i % 3 < 2) {
                if (isMale) {
                    updateProfilePhoto(context, sessionId, 1, com.playposse.egoeater.test.R.raw.male_profile_photo1);
                } else {
                    updateProfilePhoto(context, sessionId, 1, com.playposse.egoeater.test.R.raw.female_profile_photo1);
                }
            }

            if (i % 3 < 1) {
                if (isMale) {
                    updateProfilePhoto(context, sessionId, 2, com.playposse.egoeater.test.R.raw.male_profile_photo2);
                } else {
                    updateProfilePhoto(context, sessionId, 2, com.playposse.egoeater.test.R.raw.female_profile_photo2);
                }
            }
        }
    }

    private static void updateProfilePhoto(
            Context context,
            long sessionId,
            int photoIndex,
            int photoResId) throws IOException, InterruptedException {

//        Log.i(LOG_TAG, "updateProfilePhoto: About to upload profile photo.");
//        byte[] profilePhoto = IoUtil.readRawResource(context, photoResId);
//        Log.i(LOG_TAG, "updateProfilePhoto: Read bytes. (Photo size " + profilePhoto.length + ")");
//
//        byte[] pngPhoto = convertJpgToPng(profilePhoto);
//        Log.i(LOG_TAG, "updateProfilePhoto: Convert to png. (Photo size " + pngPhoto.length);
//
//        String base64 = Base64.encodeToString(pngPhoto, Base64.DEFAULT);
//        PhotoBean photoBean = new PhotoBean().setBytes(base64);
//        Log.i(LOG_TAG, "updateProfilePhoto: ... encode done. (Encoded size "
//                + base64.length() + ")");
//
//        EgoEaterApi api = instantiateApi();
//        api.uploadProfilePhoto(sessionId, photoIndex, photoBean).execute();
//        Log.i(LOG_TAG, "updateProfilePhoto: ...upload done.");

        byte[] profilePhoto = IoUtil.readRawResource(context, photoResId);
        byte[] pngPhoto = convertJpgToPng(profilePhoto);
        UploadProfilePhotoToServletClientAction.getBlocking(
                context,
                sessionId,
                photoIndex,
                pngPhoto);
    }

    public static EgoEaterApi instantiateApi() {
        HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                httpRequest.setConnectTimeout(200 * 1000);
                httpRequest.setReadTimeout(100 * 1000);
            }
        };

        return new EgoEaterApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                httpRequestInitializer)
                .setApplicationName("Ego Eater")
                .setRootUrl("https://ego-eater.appspot.com/_ah/api/")
                .build();
    }

    private static byte[] convertJpgToPng(byte[] jpgContent) throws IOException {
        // Read JPG.
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpgContent, 0, jpgContent.length);

        // Write PNG.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output); //100-best quality
        output.close();

        return output.toByteArray();
    }
}
