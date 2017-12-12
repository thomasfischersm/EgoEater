package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.firebase.FirebaseServerAction;
import com.playposse.egoeater.backend.firebase.NotifyProfileUpdatedFirebaseServerAction;
import com.playposse.egoeater.backend.schema.DeactivationLog;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.util.RefUtil;

import java.io.IOException;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that closes the users account.
 */
public class UpdateAccountStateServerAction extends AbstractServerAction {

    public static UserBean deactivateAccount(long sessionId)
            throws BadRequestException, IOException {

        return updateAccount(sessionId, false);
    }

    public static UserBean reactivateAccount(long sessionId)
            throws BadRequestException, IOException {

        return updateAccount(sessionId, true);
    }

    private static UserBean updateAccount(long sessionId, boolean newActiveState)
            throws BadRequestException, IOException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Deactivate user.
        egoEaterUser.setActive(newActiveState);
        ofy().save().entity(egoEaterUser).now();

        // Notify other users of update via Firebase.
        NotifyProfileUpdatedFirebaseServerAction.notifyProfileUpdated(egoEaterUser.getId());

        // Log action.
        DeactivationLog log =
                new DeactivationLog(RefUtil.createUserRef(egoEaterUser), newActiveState);
        ofy().save().entity(log);

        return new UserBean(egoEaterUser);
    }
}
