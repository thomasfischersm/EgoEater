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
        int distance = (int) profile.getDistance();
        final String distanceStr;
        if (distance > 0) {
            distanceStr = context.getString(R.string.distance_snippet, distance);
        } else {
            distanceStr = "";
        }

        boolean hasCity = !StringUtil.isEmpty(profile.getCity());
        boolean hasState = !StringUtil.isEmpty(profile.getState());
        boolean hasCountry = !StringUtil.isEmpty(profile.getCountry());
        boolean isUsa = USA_COUNTRY.equals(profile.getCountry());

        if (hasCity && hasState && isUsa) {
            return profile.getCity() + LOCATION_SEPARATOR + profile.getState() + distanceStr;
        } else if (hasCity && hasCountry){
            return profile.getCity() + LOCATION_SEPARATOR + profile.getCountry() + distanceStr;
        } else if (hasState && hasCountry) {
            return profile.getState() + LOCATION_SEPARATOR + profile.getCountry() + distanceStr;
        } else if (hasCountry) {
            return profile.getCountry() + distanceStr;
        } else if (hasCity) {
            return profile.getCity() + distanceStr;
        } else if (hasState) {
            return profile.getState() + distanceStr;
        } else {
            return distanceStr.trim();
        }
    }

    public static String formatCityStateDistanceAndProfile(
            Context context,
            ProfileParcelable profile) {

        String profileText = profile.getProfileText();
        if (profileText != null) {
            return formatCityStateAndDistance(context, profile) + " - " + profileText;
        } else {
            return formatCityStateAndDistance(context, profile);
        }
    }
}
