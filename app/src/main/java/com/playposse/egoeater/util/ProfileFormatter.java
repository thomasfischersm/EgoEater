package com.playposse.egoeater.util;

import android.content.Context;

import com.playposse.egoeater.R;
import com.playposse.egoeater.storage.ProfileParcelable;

import static com.playposse.egoeater.util.EgoEaterConstants.LOCATION_SEPARATOR;
import static com.playposse.egoeater.util.EgoEaterConstants.USA_COUNTRY;

/**
 * A utility that formats text with profile information.
 */
public final class ProfileFormatter {

    private ProfileFormatter() {
    }

    public static String formatNameAndAge(Context context, ProfileParcelable profile) {
        StringBuilder sb = new StringBuilder();
        sb.append(profile.getFirstName());

        if (profile.getAge() > 0) {
            sb.append(context.getString(R.string.age_separator));
            sb.append(profile.getAge());
        }
        return sb.toString();
    }

    public static String formatCityStateAndDistance(Context context, ProfileParcelable profile) {
        if (StringUtil.isEmpty(profile.getCity())
                || StringUtil.isEmpty(profile.getState())
                || StringUtil.isEmpty(profile.getCountry())) {
            // Skip. The data is incomplete.
            return "";
        }

        int distance = (int) profile.getDistance();
        final String distanceStr;
        if (distance > 0) {
            distanceStr = context.getString(R.string.distance_snippet, distance);
        } else {
            distanceStr = "";
        }

        if (profile.getCountry().equals(USA_COUNTRY)) {
            return profile.getCity() + LOCATION_SEPARATOR + profile.getState() + distanceStr;
        } else {
            return profile.getCity() + LOCATION_SEPARATOR + profile.getCountry() + distanceStr;
        }
    }

    public static String formatCityStateDistanceAndProfile(
            Context context,
            ProfileParcelable profile) {

        return formatCityStateAndDistance(context, profile) + " - " + profile.getProfileText();
    }
}
