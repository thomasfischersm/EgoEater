package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;

/**
 * A client action that deletes a profile photo.
 */
public class DeleteProfilePhotoClientAction extends ApiClientAction<Void> {

    private final int photoIndex;

    public DeleteProfilePhotoClientAction(
            Context context,
            int photoIndex,
            Callback<Void> callback) {

        super(context, callback);

        this.photoIndex = photoIndex;
    }

    @Override
    protected Void executeAsync() throws IOException {
        UserBean userBean = getApi().deleteProfilePhoto(getSessionId(), photoIndex).execute();
        EgoEaterPreferences.setUser(getContext(), userBean);
        return null;
    }
}
