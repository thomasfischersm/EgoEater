package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.firebase.NotifyProfileUpdatedFirebaseServerAction;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.io.IOException;

import javax.annotation.Nullable;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that updates the users location.
 */
public class UpdateLocationServerAction extends AbstractServerAction {

    public static UserBean updateLocation(
            long sessionId,
            double latitude,
            double longitude,
            @Nullable String city,
            @Nullable String state,
            String country) throws BadRequestException, IOException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Store location info.
        egoEaterUser.setLatitude(latitude);
        egoEaterUser.setLongitude(longitude);
        egoEaterUser.setCity(city);
        egoEaterUser.setState(state);
        egoEaterUser.setCountry(country);
        ofy().save().entity(egoEaterUser).now();

        // Update other users of the new lcoation.
        NotifyProfileUpdatedFirebaseServerAction.notifyProfileUpdated(egoEaterUser.getId());

        return new UserBean(egoEaterUser);
    }
}
