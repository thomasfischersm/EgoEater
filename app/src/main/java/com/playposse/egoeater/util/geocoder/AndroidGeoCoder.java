package com.playposse.egoeater.util.geocoder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.playposse.egoeater.util.AnalyticsUtil;

import java.io.IOException;
import java.util.List;

/**
 * A helper class to use the default Android geo coder.
 */
public final class AndroidGeoCoder {

    private static final String LOG_TAG = AndroidGeoCoder.class.getSimpleName();

    private AndroidGeoCoder() {}

    @Nullable
    public static Locale getLocaleFromGeoCoder(
            Context context,
            double latitude,
            double longitude) {

        try {
            Geocoder geocoder = new Geocoder(context, java.util.Locale.getDefault());
            List<Address> addresses =
                    geocoder.getFromLocation(latitude, longitude, 1);

            if ((addresses == null) || (addresses.size() == 0)) {
                String errorMsg = "checkLocationSync: The location service returned no result " +
                        "for " + latitude + ", " + longitude + ".";
                Log.i(LOG_TAG, errorMsg);
                Crashlytics.logException(new IllegalStateException(errorMsg));
                AnalyticsUtil.reportAndroidGeoCoderResult(
                        context,
                        false,
                        false);
                return null;
            }

            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            Locale locale = new Locale(city, state, country);

            AnalyticsUtil.reportAndroidGeoCoderResult(
                    context,
                    true,
                    !locale.hasEmptyValue());
            return locale;
        } catch (IOException ex) {
            String msg = "getLocaleFromGeoCoder: Failed to look up location from GeoCoder for: "
                    + latitude + ", " + longitude;
            Log.e(LOG_TAG, msg, ex);
            Crashlytics.logException(new Exception(msg, ex));
            AnalyticsUtil.reportAndroidGeoCoderResult(context, false, false);
            return null;
        }
    }
}
