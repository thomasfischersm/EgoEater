package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.firebase.NotifyProfileUpdatedFirebaseServerAction;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.util.DataMunchUtil;

import java.io.IOException;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that sets the age override. Most users get their age imported from Facebook.
 */
public class UpdateBirthdayOverrideServerAction extends AbstractServerAction {

    private static final int FUTURE_AGE = 0;
    private static final int MAXIMUM_AGE = 100;


    public static UserBean updateBirthdayOverride(long sessionId, String birthdayOverride)
            throws BadRequestException, IOException {

        // Verify that the age is parsable.
        Integer age = DataMunchUtil.getAge(birthdayOverride);
        if (age == null) {
            throw new BadRequestException("Couldn't parse birthday override: '" + birthdayOverride
                    + "' for session id: " + sessionId);
        }

        if ((age <= FUTURE_AGE) || (age >= MAXIMUM_AGE)) {
            throw new BadRequestException("The birthday override is outside of the valid range: "
                    + birthdayOverride + "' for session id: " + sessionId);
        }

        // Verify proper user session.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Save the birthday override.
        egoEaterUser.setBirthdayOverride(birthdayOverride);
        ofy().save().entity(egoEaterUser).now();

        // Notify other users of the change.
        NotifyProfileUpdatedFirebaseServerAction.notifyProfileUpdated(egoEaterUser.getId());

        return new UserBean(egoEaterUser);
    }
}
