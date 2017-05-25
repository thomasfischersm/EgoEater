package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.firebase.NotifyProfileUpdatedFirebaseServerAction;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.io.IOException;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that deletes a profile photo.
 */
public class DeleteProfilePhotoServerAction extends AbstractServerAction {

    public static UserBean deleteProfilePhoto(long sessionId, int photoIndex)
            throws BadRequestException, IOException {

        // Verify input.
        if ((photoIndex < 1) || (photoIndex > 2)) {
            throw new BadRequestException(
                    "The photoIndex is outside of the expected range: " + photoIndex);
        }

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Verify that photo exists.
        if (photoIndex >= egoEaterUser.getProfilePhotos().size()) {
            throw new BadRequestException("The photoIndex doesn't exist: " + photoIndex);
        }

        // Remove old photo.
        Storage storage = StorageOptions.getDefaultInstance().getService();
        deleteProfilePhoto(photoIndex, egoEaterUser, storage);
        ofy().save().entity(egoEaterUser).now();

        NotifyProfileUpdatedFirebaseServerAction.notifyProfileUpdated(egoEaterUser.getId());

        return new UserBean(egoEaterUser);
    }
}
