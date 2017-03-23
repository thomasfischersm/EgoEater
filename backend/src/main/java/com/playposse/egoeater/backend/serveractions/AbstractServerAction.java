package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.ProfilePhoto;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A base class for server actions that offers useful methods.
 */
public abstract class AbstractServerAction {

    protected static final String BUCKET_NAME = "ego-eater.appspot.com";

    protected static EgoEaterUser loadUser(long sessionId) throws BadRequestException {
        List<EgoEaterUser> egoEaterUsers =
                ofy()
                        .load()
                        .type(EgoEaterUser.class)
                        .filter("sessionId", sessionId)
                        .list();

        if (egoEaterUsers.size() != 1) {
            throw new BadRequestException("The session id " + sessionId +
                    " resulted in an unexpected number of users: " + egoEaterUsers.size());
        }

        return egoEaterUsers.get(0);
    }

    protected static List<ProfilePhoto> deleteProfilePhoto(
            int photoIndex,
            EgoEaterUser egoEaterUser,
            Storage storage) {

        List<ProfilePhoto> profilePhotos = egoEaterUser.getProfilePhotos();
        if (photoIndex < profilePhotos.size() ) {
            ProfilePhoto oldProfilePhoto = profilePhotos.get(photoIndex);
            deleteFile(oldProfilePhoto.getFileName(), storage);
            profilePhotos.remove(photoIndex);
        }
        return profilePhotos;
    }

    protected static void deleteFile(String fileName, Storage storage) {
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        storage.delete(blobId);
    }
}
