package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that receives a new Firebase token from the client.
 */
public class UpdateFirebaseTokenServerAction extends AbstractServerAction {

    public static void updateFireBaseToken(long sessionId, String firebaseToken)
            throws BadRequestException {

        EgoEaterUser egoEaterUser = loadUser(sessionId);
        egoEaterUser.setFirebaseToken(firebaseToken);
        ofy().save().entity(egoEaterUser);
    }
}
