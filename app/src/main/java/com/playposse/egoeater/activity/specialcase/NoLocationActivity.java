package com.playposse.egoeater.activity.specialcase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.activity.RatingActivity;
import com.playposse.egoeater.activity.base.ParentActivity;
import com.playposse.egoeater.clientactions.UpdateLocationClientAction;
import com.playposse.egoeater.contentprovider.EgoEaterContract.PipelineLogTable;
import com.playposse.egoeater.services.PopulatePipelineService;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.AnalyticsUtil;
import com.playposse.egoeater.util.StringUtil;
import com.playposse.egoeater.util.geocoder.GoogleMapsGeoCoder;
import com.playposse.egoeater.util.geocoder.Locale;

import java.io.IOException;
import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * An {@link Activity} where the user is placed when the location information is missing. This
 * activity will try to get the location information and then allows the user to proceed to the
 * {@link RatingActivity}.
 */
public class NoLocationActivity extends ParentActivity<NoLocationFragment> {

    private static final String LOG_TAG = NoLocationActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST = 1;
    private static final int MINIMUM_DISTANCE_TO_UPDATE_LOCATION = 1000;


    @Nullable private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMainFragment(new NoLocationFragment());

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new LocalConnectionCallbacks())
                    .addOnConnectionFailedListener(new LocalConnectionFailedListener())
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST) {
            getContentFragment().refreshView();

            boolean isSuccess = (grantResults.length == 1)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            AnalyticsUtil.reportReceivedPermissionResult(getApplication(), isSuccess);
        }
    }

    boolean hasLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasGpsCoordinates() {
        Double latitude = EgoEaterPreferences.getLatitude(this);
        Double longitude = EgoEaterPreferences.getLongitude(this);
        return (latitude != null) && (longitude != null) && (latitude != 0) && (longitude != 0);
    }

    private boolean hasPartialLocation() {
        return !StringUtil.isEmpty(EgoEaterPreferences.getCountry(this));
    }

    boolean hasFullLocation() {
        return !StringUtil.isEmpty(EgoEaterPreferences.getCity(this))
                && !StringUtil.isEmpty(EgoEaterPreferences.getState(this))
                && !StringUtil.isEmpty(EgoEaterPreferences.getCountry(this));
    }

    void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST);

        AnalyticsUtil.reportRequestLocationPermission(getApplication());
    }

    void requestLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                requestLocationSync();
            }
        }).start();
    }

    @WorkerThread
    private void requestLocationSync() {
        // Request permission if necessary.
        if (!hasLocationPermission()) {
            requestPermission();
            return;
        }

        // Try to get the last known location or request a location.
        final Location location;
        if (!hasGpsCoordinates()) {
            location = getLastLocation();
            if (location == null) {
                sendLocationRequest();
                return;
            }
        }

        // Try to get the Locale through GeoCoder.
        Locale locale = getLocaleFromGeoCoder();
        if ((locale == null) || (locale.hasEmptyValue())) {
            // Try Google Maps API as a backup.
            locale = GoogleMapsGeoCoder.reverseLookup(this);
        }

        // Store the location info in the cloud.
        if ((locale != null) && (!StringUtil.isEmpty(locale.getCountry()))) {
            try {
                new UpdateLocationClientAction(
                        getApplicationContext(),
                        EgoEaterPreferences.getLatitude(this),
                        EgoEaterPreferences.getLongitude(this),
                        locale.getCity(),
                        locale.getState(),
                        locale.getCountry())
                        .executeBlocking();
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "requestLocation: Failed to update location.", ex);
                Crashlytics.logException(ex);
                return;
            }

            // Kick off pipeline to rebuild.
            PopulatePipelineService.startService(
                    this,
                    PipelineLogTable.LOCATION_UPDATE_TRIGGER);

            if (hasPartialLocation()) {
                // Has at least a country. Let's continue to the ratings activity.
                GlobalRouting.onStartComparing(this);
                return;
            }
        }

        // Okay, still don't have the minimum address, let's cycle again through our attempts.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContentFragment().refreshView();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void sendLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(0);
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(LOG_TAG, "Got location update.");
                        LocationServices.FusedLocationApi.removeLocationUpdates(
                                googleApiClient,
                                this);

                        requestLocation();
                    }
                },
                Looper.getMainLooper());
    }

    @Nullable
    private Location getLastLocation() {
        @SuppressLint("MissingPermission")
        Location lastLocation =
                LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            // Store the location.
            EgoEaterPreferences.setLatitude(this, lastLocation.getLatitude());
            EgoEaterPreferences.setLongitude(this, lastLocation.getLongitude());
        }

        return lastLocation;
    }

    @Nullable
    private Locale getLocaleFromGeoCoder() {
        double latitude = EgoEaterPreferences.getLatitude(this);
        double longitude = EgoEaterPreferences.getLongitude(this);

        try {
            Geocoder geocoder = new Geocoder(this, java.util.Locale.getDefault());
            List<Address> addresses =
                    geocoder.getFromLocation(latitude, longitude, 1);

            if ((addresses == null) || (addresses.size() == 0)) {
                String errorMsg = "checkLocationSync: The location service returned no result " +
                        "for " + latitude + ", " + longitude + ".";
                Log.i(LOG_TAG, errorMsg);
                Crashlytics.logException(new IllegalStateException(errorMsg));
                return null;
            }

            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            return new Locale(city, state, country);
        } catch (IOException ex) {
            String msg = "getLocaleFromGeoCoder: Failed to look up location from GeoCoder for: "
                    + latitude + ", " + longitude;
            Log.e(LOG_TAG, msg, ex);
            Crashlytics.logException(new Exception(msg, ex));
            return null;
        }
    }

    /**
     * A simple implementation of {@link GoogleApiClient.ConnectionCallbacks}.
     */
    private class LocalConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            getContentFragment().refreshView();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(LOG_TAG, "onConnectionSuspended: Google Play suspended the connection for " +
                    "location.");
        }
    }

    /**
     * A simple implementation of {@link GoogleApiClient.OnConnectionFailedListener}.
     */
    private class LocalConnectionFailedListener
            implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            String msg = "onConnectionFailed: Failed to connect to Google Play Services "
                    + connectionResult.getErrorCode() + " - " + connectionResult.getErrorMessage();
            Log.e(LOG_TAG, msg);
            Crashlytics.logException(new Exception(msg));
        }
    }

}
