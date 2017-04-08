package com.playposse.egoeater.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A substitute for {@link android.location.Geocoder}. The {@link android.location.Geocoder}
 * provided by Android doesn't work reliably with all addresses. Making an HTTP call to Google Maps
 * is more reliable, yet slower.
 */
public class HttpGeoCoder {

    private static final String LOG_CAT = HttpGeoCoder.class.getSimpleName();

    private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private static final String STATUS_KEY = "status";
    private static final String OK_STATUS = "OK";
    private static final String RESULTS_KEY = "results";
    private static final String GEOMETRY_KEY = "geometry";
    private static final String LOCATION_KEY = "location";
    private static final String LAT_KEY = "lat";
    private static final String LNG_KEY = "lng";

    private static final Object VOLLEY_TAG = HttpGeoCoder.class;


    /**
     * Resolves an address to GPS coordinates.
     */
    public static void geoCode(
            Context context,
            final String address,
            final GeoCodeCallback callback)
            throws UnsupportedEncodingException {

        String url = URL + URLEncoder.encode(address, "UTF-8");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject rootObject) {
                        try {
                            String status = rootObject.getString(STATUS_KEY);
                            if (!OK_STATUS.equalsIgnoreCase(status)) {
                                Log.e(LOG_CAT, "Failed to look up address: " + status);
                            }

                            JSONArray resultsArray = rootObject.getJSONArray(RESULTS_KEY);
                            if (resultsArray.length() < 1) {
                                Log.w(LOG_CAT, "Address didn't return a result: " + address);
                                return;
                            }

                            JSONObject resultsObject = resultsArray.getJSONObject(0);
                            JSONObject geometryObject = resultsObject.getJSONObject(GEOMETRY_KEY);
                            JSONObject locationObject = geometryObject.getJSONObject(LOCATION_KEY);
                            String lat = locationObject.getString(LAT_KEY);
                            String lng = locationObject.getString(LNG_KEY);
                            String gpsString = GeoUtil.formatGpsString(lat, lng);

                            callback.onGeoResponse(gpsString);
                        } catch (JSONException ex) {
                            Log.e(LOG_CAT, "Failed to parse geo JSON response.", ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_CAT, "Failed to geo code: " + address, error);
                    }
                });


        VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
//        volleySingleton.getRequestQueue().cancelAll(VOLLEY_TAG);
        request.setTag(VOLLEY_TAG);
        volleySingleton.addToRequestQueue(request);
    }

    /**
     * Callback interface that returns the GPS coordinate from the network call to Google Maps.
     */
    public interface GeoCodeCallback {

        void onGeoResponse(String gpsCoordinate);
    }
}
