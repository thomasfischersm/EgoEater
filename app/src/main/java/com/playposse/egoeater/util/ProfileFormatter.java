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

    private ProfileFormatter() {}

    public static String formatNameAndAge(Context context, ProfileParcelable profile) {
        StringBuilder sb = new StringBuilder();
        sb.append(profile.getFirstName());

        if (profile.getAge() > 0) {
            sb.append(context.getString(R.string.age_speparator));
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

        String distance = context.getString(R.string.distance_snippet, (int) profile.getDistance());

        if (profile.getCountry().equals(USA_COUNTRY)) {
            return profile.getCity() + LOCATION_SEPARATOR + profile.getState() + distance;
        } else {
            return profile.getCity() + LOCATION_SEPARATOR + profile.getCountry() + distance;
        }
    }
}
