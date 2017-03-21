package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A base class for server actions that offers useful methods.
 */
public abstract class AbstractServerAction {

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
}
