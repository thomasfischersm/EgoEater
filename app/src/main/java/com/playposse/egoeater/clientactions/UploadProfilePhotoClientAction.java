package com.playposse.egoeater.clientactions;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.playposse.egoeater.backend.egoEaterApi.model.PhotoBean;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.ImageUtil;

import java.io.IOException;

/**
 * A client action that uploads a new profile photo to the cloud.
 */
public class UploadProfilePhotoClientAction extends ApiClientAction<Void> {

    private static final String LOG_TAG = UploadProfilePhotoClientAction.class.getSimpleName();

    private final Bitmap bitmap;
    private final int photoIndex;

    public UploadProfilePhotoClientAction(
            Context context,
            int photoIndex,
            Bitmap bitmap,
            Callback<Void> callback) {

        super(context, callback);

        this.photoIndex = photoIndex;
        this.bitmap = bitmap;
    }

    @Override
    protected Void executeAsync() throws IOException {
        // Convert Bitmap to base64 encoded PNG file.
        byte[] bytes = ImageUtil.convertBitmapToBytes(bitmap);
        String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        PhotoBean photoBean = new PhotoBean().setBytes(base64);

        // Upload bytes to the cloud.
        UserBean userBean =
                getApi().uploadProfilePhoto(getSessionId(), photoIndex, photoBean).execute();
        Log.i(LOG_TAG, "executeAsync: Done sending");

        // Update the preferences with the new profile photo.
        EgoEaterPreferences.setUser(getContext(), userBean);
        EgoEaterPreferences.setHasFirstProfilePhoto(getContext(), true);

        AnalyticsUtil.reportPhotoUploaded(getContext(), photoIndex);

        return null;
    }
}
