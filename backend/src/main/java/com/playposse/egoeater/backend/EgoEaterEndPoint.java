/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.playposse.egoeater.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.playposse.egoeater.backend.serveractions.SignInServerAction;
import com.playposse.egoeater.backend.beans.UserBean;

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

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "signIn")
    public UserBean signIn(
            @Named("fbAccessToken") String fbAccessToken,
            @Named("firebaseToken") String firebaseToken) {

        return SignInServerAction.signIn(fbAccessToken, firebaseToken);
    }

}
