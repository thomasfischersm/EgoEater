package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.playposse.egoeater.backend.beans.PhotoBean;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.ProfilePhoto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that receive a profile photo. The photo is stored in Google Cloud Storage and the
 * {@link EgoEaterUser} is updated.
 */
public class UploadProfilePhotoServerAction extends AbstractServerAction {

    private static final Random random = new Random();
    public static final String PNG_FILE_EXTENSION = ".png";

    public static UserBean uploadProfilePhoto(long sessionId, int photoIndex, PhotoBean photoBean)
            throws BadRequestException {

        // Verify input.
        if ((photoIndex < 0) || (photoIndex > 2)) {
            throw new BadRequestException(
                    "The photoIndex is outside of the expected range: " + photoIndex);
        }

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Remove old photo if necessary.
        Storage storage = StorageOptions.getDefaultInstance().getService();
        List<ProfilePhoto> profilePhotos = deleteProfilePhoto(photoIndex, egoEaterUser, storage);

        // Store new photo in Cloud Storage.
        String fileName = generateUniqueFilename();
        String url = createFile(fileName, photoBean.getBytes(), storage);

        // Update EgoEaterUser.
        ProfilePhoto profilePhoto = new ProfilePhoto(fileName, url);
        if (photoIndex < profilePhotos.size()) {
            profilePhotos.add(photoIndex, profilePhoto);
        } else {
            profilePhotos.add(profilePhoto);
        }
        ofy().save().entity(egoEaterUser).now();

        return new UserBean(egoEaterUser);
    }

    private static String generateUniqueFilename() {
        return System.currentTimeMillis() + "" + random.nextLong() + PNG_FILE_EXTENSION;
    }

    private static String createFile(String fileName, byte[] fileContent, Storage storage) {
        List<Acl> acls = new ArrayList<>();
        acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, fileName)
                .setAcl(acls)
                .build();
        Blob blob = storage.create(
                blobInfo,
                fileContent);
        return blob.getMediaLink();
    }
}
