package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.ProfileBean;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that returns a list of public profiles by id. The idea is that the device first
 * finds nearby users by id. When the device is ready to show particular users, it requests their
 * information in a batch.
 */
public class GetProfilesByIdServerAction extends AbstractServerAction {

    private static final Logger log = Logger.getLogger(GetProfilesByIdServerAction.class.getName());

    private static final int MAX_RESULTS = 1_000;

    public static List<ProfileBean> getProfilesById(long sessionId, List<Long> profileIds)
            throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // Check max request size.
        if (profileIds.size() > MAX_RESULTS) {
            log.severe("Exceeded max result for GetProfilesByIdServerAction: "
                    + profileIds.size());
            return null;
        }

        // Query users.
        Collection<EgoEaterUser> egoEaterUsers = ofy().load()
                .type(EgoEaterUser.class)
                .ids(profileIds)
                .values();

        // Convert the Objectify entities into beans.
        List<ProfileBean> profileBeans = new ArrayList<>(egoEaterUsers.size());
        for (EgoEaterUser profile : egoEaterUsers) {
            long partnerId = profile.getId();
            if (egoEaterUser.getFuckOffList().contains(partnerId)) {
                continue;
            }
            if (egoEaterUser.getPissedOffList().contains(partnerId)) {
                continue;
            }
            profileBeans.add(new ProfileBean(profile, egoEaterUser));
        }

        log.info("Received request for " + profileIds.size()
                + " profiles and returned " + profileBeans.size());
        return profileBeans;
    }
}
