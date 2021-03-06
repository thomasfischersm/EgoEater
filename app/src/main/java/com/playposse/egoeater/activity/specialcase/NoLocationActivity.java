package com.playposse.egoeater.activity.specialcase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.playposse.egoeater.util.geocoder.AndroidGeoCoder;
import com.playposse.egoeater.util.geocoder.GoogleMapsGeoCoder;
import com.playposse.egoeater.util.geocoder.Locale;

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

    void requestLocation(final boolean shouldCheckGps) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                requestLocationSync(shouldCheckGps);
            }
        }).start();
    }

    @WorkerThread
    private void requestLocationSync(boolean shouldCheckGps) {
        Log.d(LOG_TAG, "requestLocationSync: Starting requestLocationSync, shouldCheckGps: " + shouldCheckGps);

        // Request permission if necessary.
        if (!hasLocationPermission()) {
            requestPermission();
            Log.i(LOG_TAG, "requestLocationSync: Requesting permission.");
            return;
        }

        // Try to get the last known location or request a location.
        final Location location;
        if (!hasGpsCoordinates()) {
            Log.i(LOG_TAG, "requestLocationSync: Checking last location.");
            location = getLastLocation();
            if (location == null) {
                sendLocationRequest();
                Log.i(LOG_TAG, "requestLocationSync: Triggering Google Play Services to get " +
                        "a location.");
                return;
            }
        }

        // Try to get the Locale through GeoCoder.
        Log.i(LOG_TAG, "requestLocationSync: Trying to get locale from Android geo coder.");
        Locale locale = getLocaleFromGeoCoder();
        if ((locale == null) || (locale.hasEmptyValue())) {
            Log.i(LOG_TAG, "requestLocationSync: Failed to get local from Android geo coder: "
                    + locale);
            // Try Google Maps API as a backup.
            locale = GoogleMapsGeoCoder.reverseLookup(this);
            Log.i(LOG_TAG, "requestLocationSync: Got locale from Google maps geo coder: "
                    + locale);
        }

        // Store the location info in the cloud.
        if ((locale != null) && (!StringUtil.isEmpty(locale.getCountry()))) {
            Log.i(LOG_TAG, "requestLocationSync: Try to update the location in the cloud.");
            try {
                new UpdateLocationClientAction(
                        getApplicationContext(),
                        EgoEaterPreferences.getLatitude(this),
                        EgoEaterPreferences.getLongitude(this),
                        locale.getCity(),
                        locale.getState(),
                        locale.getCountry())
                        .executeBlocking();
                Log.i(LOG_TAG, "requestLocationSync: Succeeded updating the cloud.");
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
                Log.i(LOG_TAG, "requestLocationSync: User is ready to go to RatingActivity.");
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

        // Try to get the latest location for good measure.
        if (shouldCheckGps) {
            sendLocationRequest();
            Log.i(LOG_TAG, "requestLocationSync: Turned on antenna for good measure.");
        }
    }

    @SuppressLint("MissingPermission")
    private void sendLocationRequest() {
        if (!googleApiClient.isConnected()) {
            Log.i(LOG_TAG, "sendLocationRequest: googleApiClient is not yet connected!");
            return;
        }

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

                        requestLocation(false);
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

        return AndroidGeoCoder.getLocaleFromGeoCoder(this, latitude, longitude);
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
