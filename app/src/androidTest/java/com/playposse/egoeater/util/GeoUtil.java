package com.playposse.egoeater.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * A utility for handling the formatting of GPS locations.
 */

public class GeoUtil {

    private static final String LOG_CAT = GeoUtil.class.getSimpleName();

    private static final String COORDINATE_SEPARATOR = ", ";

    public static String toStr(Address address) {
        String latitude = Location.convert(address.getLatitude(), Location.FORMAT_DEGREES);
        String longitude = Location.convert(address.getLongitude(), Location.FORMAT_DEGREES);

        // start debug
        Log.i(LOG_CAT, "GPS in degrees: " + (latitude +  COORDINATE_SEPARATOR + longitude));

        String la = Location.convert(address.getLatitude(), Location.FORMAT_MINUTES);
        String lo = Location.convert(address.getLongitude(), Location.FORMAT_MINUTES);
        Log.i(LOG_CAT, "GPS in minutes: " + (latitude +  COORDINATE_SEPARATOR + longitude));
        // end debug

        return latitude +  COORDINATE_SEPARATOR + longitude;
    }

    @Nullable
    public static Address fromStr(Context context, String gpsLocation) throws IOException {
        String[] split = gpsLocation.split(COORDINATE_SEPARATOR);
        double latitude = Location.convert(split[0]);
        double longitude = Location.convert(split[1]);
        List<Address> addressList = new Geocoder(context).getFromLocation(latitude, longitude, 1);

        if ((addressList != null) && (addressList.size() == 1)) {
            return addressList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Computes the distance between two GPS locations in meters.
     */
    public static float distanceBetween(Location location0, Location location1) {
        float[] result = new float[1];
        Location.distanceBetween(
                location0.getLatitude(),
                location0.getLongitude(),
                location1.getLatitude(),
                location1.getLongitude(),
                result);
        return result[0];
    }

    /**
     * Converts an {@link Address} to a {@link Location}.
     */
    public static Location toLocation(Address address) {
        Location location = new Location("");
        location.setLatitude(address.getLatitude());
        location.setLongitude(address.getLongitude());
        return location;
    }

    /**
     * Converts a string with a GPS location to a {@link Location} object.
     */
    public static Location toLocation(String gpsLocation) {
        String[] split = gpsLocation.split(COORDINATE_SEPARATOR);
        double latitude = Location.convert(split[0]);
        double longitude = Location.convert(split[1]);

        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    /**
     * Formats a GPS string.
     */
    public static String formatGpsString(String latitude, String longitude) {
        return latitude + COORDINATE_SEPARATOR + longitude;
    }
}
