package com.playposse.egoeater.clientactions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.MatchBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.ImageUtil;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A variation of the {@link UploadProfilePhotoClientAction} that uses the servlet intead of the
 * Google Endpoint API.
 */
public class UploadProfilePhotoToServletClientAction extends ApiClientAction<String> {

    private static final String LOG_TAG =
            UploadProfilePhotoToServletClientAction.class.getSimpleName();

    private static final MediaType PNG_MEDIA_TYPE = MediaType.parse("image/png");
    private static final int MAX_WIDTH = 1_024;
    private static final int MAX_HEIGHT = (int) (MAX_WIDTH / 3.0 * 2.0);
    private static final String SESSION_ID_FIELD_NAME = "sessionId";
    private static final String PHOTO_INDEX_FIELD_NAME = "photoIndex";

    private final long sessionId;
    private final int photoIndex;

    private Bitmap bitmap;
    private byte[] fileContent;

    public UploadProfilePhotoToServletClientAction(
            Context context,
            int photoIndex,
            Bitmap bitmap,
            Callback<String> callback) {

        super(context, callback);

        this.photoIndex = photoIndex;
        this.bitmap = bitmap;

        this.sessionId = getSessionId();
    }

    public UploadProfilePhotoToServletClientAction(
            Context context,
            long sessionId,
            int photoIndex,
            byte[] fileContent,
            Callback<String> callback) {

        super(context, callback);

        this.photoIndex = photoIndex;
        this.fileContent = fileContent;
        this.sessionId = sessionId;

        this.bitmap = null;
    }

    @Override
    protected String executeAsync() throws IOException {
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeByteArray(fileContent, 0, fileContent.length);
        }
        bitmap = ImageUtil.downScaleIfNecessary(bitmap, MAX_WIDTH, MAX_HEIGHT);
        this.fileContent = ImageUtil.convertBitmapToBytes(bitmap);

        ProfilePhotoUploadService profilePhotoUploadService = createProfilePhotoUploadService();


        RequestBody fileRequestBody = RequestBody.create(PNG_MEDIA_TYPE, fileContent);
        MultipartBody.Part sessionIdPart = MultipartBody.Part
                .createFormData(SESSION_ID_FIELD_NAME, Long.toString(sessionId));
        MultipartBody.Part photoIndexPart = MultipartBody.Part
                .createFormData(PHOTO_INDEX_FIELD_NAME, Long.toString(photoIndex));

        Response<ResponseBody> response =
                profilePhotoUploadService.uploadProfilePhoto(
                        fileRequestBody,
                        sessionIdPart,
                        photoIndexPart)
                        .execute();

        String photoUrl = response.body().string(); // Warning! Body can only be read once!

        Log.i(LOG_TAG, "executeAsync: Call to cloud was " + response.isSuccessful()
                + " " + response.code()
                + " " + photoUrl);

        storePhotoUrlInPreferences(getContext(), photoIndex, photoUrl);

        return photoUrl;
    }

    private static void storePhotoUrlInPreferences(
            Context context,
            int photoIndex,
            String photoUrl) {

        switch (photoIndex) {
            case 0:
                EgoEaterPreferences.setProfilePhotoUrl0(context, photoUrl);
                break;
            case 1:
                EgoEaterPreferences.setProfilePhotoUrl1(context, photoUrl);
                break;
            case 2:
                EgoEaterPreferences.setProfilePhotoUrl2(context, photoUrl);
                break;
        }
    }

    private ProfilePhotoUploadService createProfilePhotoUploadService() {
        // Create extra long timeouts.
        OkHttpClient okClient = new OkHttpClient.Builder()
                .connectTimeout(30, SECONDS)
                .writeTimeout(30, SECONDS)
                .readTimeout(30, SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ego-eater.appspot.com")
                .client(okClient)
                .build();

        return retrofit.create(ProfilePhotoUploadService.class);
    }

    public static String getBlocking(
            Context context,
            long sessionId,
            int photoIndex,
            byte[] fileContent) throws InterruptedException {

        return new UploadProfilePhotoToServletClientAction(
                context,
                sessionId,
                photoIndex,
                fileContent,
                null)
                .executeBlocking();
    }

    private interface ProfilePhotoUploadService {

        @Multipart
        @POST("/uploadProfilePhoto")
        Call<ResponseBody> uploadProfilePhoto(
                @Part("photoContent") RequestBody file,
                @Part MultipartBody.Part sessionIdPart,
                @Part MultipartBody.Part photoIndexPart);
    }
}
