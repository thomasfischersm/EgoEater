/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.playposse.egoeater.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.PhotoBean;
import com.playposse.egoeater.backend.serveractions.DeleteProfilePhotoServerAction;
import com.playposse.egoeater.backend.serveractions.SaveProfileServerAction;
import com.playposse.egoeater.backend.serveractions.SignInServerAction;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.serveractions.UpdateFirebaseTokenServerAction;
import com.playposse.egoeater.backend.serveractions.UpdateLocationServerAction;
import com.playposse.egoeater.backend.serveractions.UploadProfilePhotoServerAction;

import java.util.logging.Logger;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "egoEaterApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.egoeater.playposse.com",
                ownerName = "backend.egoeater.playposse.com",
                packagePath = ""
        )
)
public class EgoEaterEndPoint {

    private static final Logger log = Logger.getLogger(EgoEaterEndPoint.class.getName());

    @ApiMethod(name = "signIn")
    public UserBean signIn(
            @Named("fbAccessToken") String fbAccessToken,
            @Named("firebaseToken") String firebaseToken) {

        return SignInServerAction.signIn(fbAccessToken, firebaseToken);
    }

    @ApiMethod(name = "updateFireBaseToken")
    public void updateFireBaseToken(
            @Named("sessionId") long sessionId,
            @Named("firebaseToken") String firebaseToken) throws BadRequestException {

        UpdateFirebaseTokenServerAction.updateFireBaseToken(sessionId, firebaseToken);
    }

    @ApiMethod(name = "uploadProfilePhoto")
    public UserBean uploadProfilePhoto(
            @Named("sessionId") long sessionId,
            @Named("photoIndex") int photoIndex,
            PhotoBean photoBean) throws BadRequestException {

        log.info("got a photo of size " + photoBean.getBytes().length);
        return UploadProfilePhotoServerAction.uploadProfilePhoto(sessionId, photoIndex, photoBean);
    }

    @ApiMethod(name = "deleteProfilePhoto")
    public UserBean deleteProfilePhoto(
            @Named("sessionId") long sessionId,
            @Named("photoIndex") int photoIndex) throws BadRequestException {

        return DeleteProfilePhotoServerAction.deleteProfilePhoto(sessionId, photoIndex);
    }

    @ApiMethod(name = "saveProfile")
    public UserBean saveProfile(
            @Named("sessionId") long sessionId,
            @Named("profileText") String profileText) throws BadRequestException {

        return SaveProfileServerAction.saveProfile(sessionId, profileText);
    }

    @ApiMethod(name = "updateLocation")
    public UserBean updateLocation(
            @Named("sessionId") long sessionId,
            @Named("latitude") double latitude,
            @Named("longitude") double longitude,
            @Named("city") String city,
            @Named("state") String state,
            @Named("country") String country) throws BadRequestException {

        return UpdateLocationServerAction.updateLocation(
                sessionId,
                latitude,
                longitude,
                city,
                state,
                country);
    }
}
