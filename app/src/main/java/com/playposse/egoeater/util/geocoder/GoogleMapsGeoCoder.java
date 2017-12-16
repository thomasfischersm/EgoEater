package com.playposse.egoeater.util.geocoder;

import android.content.Context;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.geocoder.retrofit.AddressComponent;
import com.playposse.egoeater.util.geocoder.retrofit.GeoResultRoot;
import com.playposse.egoeater.util.geocoder.retrofit.GoogleMapsGeoService;
import com.playposse.egoeater.util.geocoder.retrofit.Result;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A class that calls Google maps to translate a GPS location into city, state, and country. The
 * {@link Geocoder} provided by Android is not very reliable.
 */
public final class GoogleMapsGeoCoder {

    private static final String LOG_TAG = GoogleMapsGeoCoder.class.getSimpleName();

    private static final String GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyC95I6rrKx99zKHlP8pzyTm9rxDzfIOpOw";
    private static final String LAT_LNG_SEPARATOR = ",";
    private static final String OK_STATUS = "OK";

    private GoogleMapsGeoCoder() {
    }

    @Nullable
    @WorkerThread
    public static Locale reverseLookup(Context context) {
        String language = java.util.Locale.getDefault().getCountry();
        Double latitude = EgoEaterPreferences.getLatitude(context);
        Double longitude = EgoEaterPreferences.getLongitude(context);
        if ((latitude == null) || (longitude == null)) {
            return null;
        }

        // Prepare.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Execute call.
        GeoResultRoot geoResultRoot;
        try {
            GoogleMapsGeoService remoteService = retrofit.create(GoogleMapsGeoService.class);
            String latLng = latitude + LAT_LNG_SEPARATOR + longitude;
            Call<GeoResultRoot> recipeCall =
                    remoteService.get(latLng, language, GOOGLE_MAPS_API_KEY);
            Response<GeoResultRoot> response = recipeCall.execute();
            geoResultRoot = response.body();
        } catch (IOException ex) {
            String msg = "reverseLookup: Failed reverse geo lookup with Google maps for "
                    + latitude + ", " + longitude;
            Log.e(LOG_TAG, msg, ex);
            Crashlytics.logException(new Exception(msg, ex));
            AnalyticsUtil.reportGoogleMapsGeoCoderResult(context, false, false);
            return null;
        }

        // Check response status.
        if ((geoResultRoot == null) || !OK_STATUS.equals(geoResultRoot.getStatus())) {
            String status = (geoResultRoot != null) ? geoResultRoot.getStatus() : null;
            String msg = "reverseLookup: Google Maps geocoder failed: " + geoResultRoot
                    + " status: " + geoResultRoot.getStatus();
            Log.e(LOG_TAG, msg);
            Crashlytics.logException(new Exception(msg));
            return null;
        }

        // Find address components.
        String city = getFirstLongNameByType(geoResultRoot, AddressComponent.LOCALITY_TYPE);
        String state = getFirstLongNameByType(
                geoResultRoot,
                AddressComponent.ADMINISTRATIVE_AREA_LEVEL_1_TYPE);
        String country = getFirstLongNameByType(geoResultRoot, AddressComponent.COUNTRY_TYPE);
        Locale locale = new Locale(city, state, country);
        Log.i(LOG_TAG, "reverseLookup: Got info from Google maps ("
                + latitude + "," + longitude + "): "
                + city + ", " + state + ", " + country);
        AnalyticsUtil.reportGoogleMapsGeoCoderResult(
                context,
                true,
                !locale.hasEmptyValue());

        return locale;
    }

    @Nullable
    private static String getFirstLongNameByType(GeoResultRoot root, String componentType) {
        if ((root == null) || (root.getResults() == null) || (root.getResults().size() == 0)) {
            return null;
        }

        for (Result result : root.getResults()) {
            if (result.getAddressComponents() != null) {
                for (AddressComponent addressComponent : result.getAddressComponents()) {
                    if ((addressComponent != null) && (addressComponent.getTypes() != null)) {
                        for (String type : addressComponent.getTypes()) {
                            if (componentType.equals(type)) {
                                return addressComponent.getLongName();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
