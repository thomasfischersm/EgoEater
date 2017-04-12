package com.playposse.egoeater.backend.util;

import com.playposse.egoeater.backend.EgoEaterEndPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility that transforms raw data from Facebook and other sources into privacy sensitive data
 * that can be displayed to the user.
 */
public class DataMunchUtil {

    private static final Logger log = Logger.getLogger(DataMunchUtil.class.getName());

    private static final String FULL_FB_DATE_FORMAT = "MM/dd/yyyy";
    private static final char DATE_SEPARATOR = '/';
    private static final double METERS_IN_A_MILE = 1609.34;
    private static final String MALE_GENDER = "male";
    private static final String FEMALE_GENDER = "female";

    /**
     * Calculates the birthday based on the FB provided information. If it cannot be determined,
     * null is returned.
     */
    public static Integer getAge(String birthday) {
        if (StringUtil.isEmpty(birthday)) {
            return null;
        }

        String[] dateParts = birthday.split(birthday, DATE_SEPARATOR);
        try {
            switch (dateParts.length) {
                case 2:
                    // A fully formed date: mm/dd/yyyy
                    Calendar now = Calendar.getInstance();
                    Calendar dob = Calendar.getInstance();
                    dob.setTime(new SimpleDateFormat(FULL_FB_DATE_FORMAT, Locale.US).parse(birthday));

                    int age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                    if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    return age;
                case 1:
                    // Only a day and month: mm/dd. Can't determine the age.
                    return null;
                case 0:
                    // Only a year: yyyy
                    Calendar today = Calendar.getInstance();
                    int year = Integer.parseInt(birthday);
                    // Note, this may be off by a year because we the exact day is unknown.
                    return today.get(Calendar.YEAR) - year;
                default:
                    // Unexpected format. Give up!
                    return null;
            }
        } catch (ParseException ex) {
            log.log(Level.SEVERE, "Couldn't parse FB date: " + birthday, ex);
            return null;
        }
    }

    /**
     * Calculate getDistance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Miles
     */
    public static double getDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance) / METERS_IN_A_MILE;
    }

    public static String getOppositeGender(String gender) {
        switch (gender) {
            case MALE_GENDER:
                return FEMALE_GENDER;
            case FEMALE_GENDER:
                return MALE_GENDER;
            default:
                return null;
        }
    }
}
