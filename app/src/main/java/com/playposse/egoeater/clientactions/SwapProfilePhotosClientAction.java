package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that swaps the position of two profile photos.
 */
public class SwapProfilePhotosClientAction extends ApiClientAction<UserBean> {

    private final int sourcePhotoId;
    private final int destinationPhotoId;

    public SwapProfilePhotosClientAction(
            Context context,
            int sourcePhotoId,
            int destinationPhotoId,
            Callback<UserBean> callback) {

        super(context, callback);

        this.sourcePhotoId = sourcePhotoId;
        this.destinationPhotoId = destinationPhotoId;
    }

    @Override
    protected UserBean executeAsync() throws IOException {
        UserBean userBean = getApi()
                .swapProfilePhotos(getSessionId(), sourcePhotoId, destinationPhotoId)
                .execute();

        // Store changes in the preferences.
        EgoEaterPreferences.setUser(getContext(), userBean);

        return userBean;
    }
}
