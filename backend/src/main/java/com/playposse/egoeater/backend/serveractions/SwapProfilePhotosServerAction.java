package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.ProfilePhoto;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that swaps the position of two profile photos.
 */
public class SwapProfilePhotosServerAction extends AbstractServerAction {

    public static UserBean swapProfilePhotos(
            long sessionId,
            int sourcePhotoId,
            int destinationPhotoId) throws BadRequestException {

        // Validate input.
        if ((sourcePhotoId < 0) || (sourcePhotoId > 2)
                || (destinationPhotoId < 0) || (destinationPhotoId >2)) {
            throw new BadRequestException(
                    "At least one of the photoIds is out of range. Source: " + sourcePhotoId
                            + " Destination: " + destinationPhotoId);
        }

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Build temporary list with null values.
        List<ProfilePhoto> photos = egoEaterUser.getProfilePhotos();
        while (photos.size() < 3) {
            photos.add(null);
        }

        // Swap photos.
        Collections.swap(photos, sourcePhotoId, destinationPhotoId);

        // Remove null values.
        for (int i = photos.size() - 1; i >= 0; i--) {
            if (photos.get(i) == null) {
                photos.remove(i);
            }
        }

        // Persist.
        ofy().save().entity(egoEaterUser).now();

        return new UserBean(egoEaterUser);
    }
}
