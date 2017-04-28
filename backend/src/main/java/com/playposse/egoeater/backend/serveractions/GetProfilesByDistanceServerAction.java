package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.playposse.egoeater.backend.EgoEaterEndPoint;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.GeoTest;
import com.playposse.egoeater.backend.util.DataMunchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that returns profiles within a certain distance.
 * <p>
 * <p>To conserve storage space, only the profile ids are returned. The device needs to request
 * the profile information in batches.
 */
public class GetProfilesByDistanceServerAction extends AbstractServerAction {

    private static final Logger log =
            Logger.getLogger(GetProfilesByDistanceServerAction.class.getName());

    private static final int MAX_RESULT = 10_000;
    private static final int MAX_DELTA = 30;
    private static final int EARTH_CIRCUMFERENCE = 24_901;
    private static final double MILES_PER_DEGREE_AT_EQUATOR = EARTH_CIRCUMFERENCE / 360;

    /**
     * Returns profile ids that are within the delta.
     * <p>
     * <p>For simplicity, the radius is a square not a circle.
     * <p>
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
        log.info("Received getProfileIdsByDistance action for " + egoEaterUser.getName()
                + " and " + delta + " miles.");

        // Check for max delta.
        if (delta > MAX_DELTA) {
            throw new BadRequestException("The delta exceeded the max: " + delta);
        }

        // Prepare filter values.
        String oppositeGender = DataMunchUtil.getOppositeGender(egoEaterUser.getGender());
        double latitude = egoEaterUser.getLatitude();
        double longitude = egoEaterUser.getLongitude();

        // Convert delta miles to delta degrees.
        delta = Math.abs(delta / MILES_PER_DEGREE_AT_EQUATOR / Math.cos(Math.toRadians(latitude)));
        double minLatitude = latitude - delta;
        double maxLatitude = latitude + delta;
        double minLongitude = longitude - delta;
        double maxLongitude = longitude + delta;
        log.info("select * from EgoEaterUser where gender='" + oppositeGender
                + "' and latitude>" + minLatitude
                + " and latitude<" + maxLatitude
                + " and longitude>" + minLongitude
                + " and longitude<" + maxLongitude);

        // Do an indexed query to find profiles
        log.info("Querying for " + oppositeGender + " (" + latitude + ", " + longitude
                + ") and delta " + delta);
        List<Key<EgoEaterUser>> intermediateKeys = ofy().load()
                .type(EgoEaterUser.class)
                .filter("longitude >", minLongitude)
                .filter("longitude <", maxLongitude)
                .filter("gender =", oppositeGender)
                .keys()
                .list();

        log.info("Initial index result is " + intermediateKeys.size());

        if (intermediateKeys.size() == 0) {
            // No point in trying the next query if there are no results here.
            return new ArrayList<>();
        }

        List<Key<EgoEaterUser>> egoEaterUsers = ofy().load()
                .type(EgoEaterUser.class)
                .filterKey("IN", intermediateKeys)
                .filter("latitude >", minLatitude)
                .filter("latitude <", maxLatitude)
                .keys()
                .list();
        log.info("Got0 " + egoEaterUsers.size() + " profiles.");

        // Check for max result size.
        if (egoEaterUsers.size() > MAX_RESULT) {
            log.severe("Exceeded max result for GetProfilesByDistanceServerAction: "
                    + egoEaterUsers.size());
            return null;
        }

        // Create list of ids.
        List<Long> profileIds = new ArrayList<>(egoEaterUsers.size());
        for (Key<EgoEaterUser> key : egoEaterUsers) {
            long partnerId = key.getId();
            if (egoEaterUser.getFuckOffList().contains(partnerId)) {
                continue;
            }
            if (egoEaterUser.getPissedOffList().contains(partnerId)) {
                continue;
            }
            profileIds.add(partnerId);
        }

        return profileIds;
    }
}
