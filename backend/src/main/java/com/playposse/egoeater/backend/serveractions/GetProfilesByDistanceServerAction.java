package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Key;
import com.playposse.egoeater.backend.EgoEaterEndPoint;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.util.DataMunchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that returns profiles within a certain distance.
 *
 * <p>To conserve storage space, only the profile ids are returned. The device needs to request
 * the profile information in batches.
 */
public class GetProfilesByDistanceServerAction extends AbstractServerAction {

    private static final Logger log =
            Logger.getLogger(GetProfilesByDistanceServerAction.class.getName());

    private static final int MAX_RESULT = 10_000;

    /**
     * Returns profile ids that are within the delta.
     *
     * <p>For simplicity, the radius is a square not a circle.
     *
     * <p>For further simplicity, we ignore the curvature of the earth. (E.g. 1 degree has more
     * distance at the equator than the pole.)
     *
     * @param delta Delta is how much the GPS location can be different between the profiles.
     */
    public static List<Long> getProfileIdsByDistance(
            long sessionId,
            double delta) throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);

        // TODO: Implement a max delta check to prevent bad access.

        // Do an indexed query to find profiles
        String oppositeGender = DataMunchUtil.getOppositeGender(egoEaterUser.getGender());
        List<Key<EgoEaterUser>> egoEaterUsers = ofy().load()
                .type(EgoEaterUser.class)
                .filter("latitude>", egoEaterUser.getLatitude() - delta)
                .filter("latitude<", egoEaterUser.getLatitude() + delta)
                .filter("longitude>", egoEaterUser.getLongitude() - delta)
                .filter("longitude<", egoEaterUser.getLongitude() + delta)
                .filter("gender=", oppositeGender)
                .keys()
                .list();

        // Check for max result size.
        if (egoEaterUsers.size() > MAX_RESULT) {
            log.severe("Exceeded max result for GetProfilesByDistanceServerAction: "
                    + egoEaterUsers.size());
            return null;
        }

        // Create list of ids.
        List<Long> profileIds = new ArrayList<>(egoEaterUsers.size());
        for (Key<EgoEaterUser> key : egoEaterUsers) {
            profileIds.add(key.getId());
        }

        return profileIds;
    }
}
