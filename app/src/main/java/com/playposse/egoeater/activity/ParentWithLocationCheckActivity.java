package com.playposse.egoeater.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.playposse.egoeater.R;
import com.playposse.egoeater.clientactions.UpdateLocationClientAction;
import com.playposse.egoeater.services.PopulatePipelineService;
import com.playposse.egoeater.storage.EgoEaterPreferences;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * A {@link ParentActivity} that automatically connects to the Google Play Services API.
 */
public abstract class ParentWithLocationCheckActivity
        extends ParentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = ParentWithLocationCheckActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST = 1;
    private static final int MINIMUM_DISTANCE_TO_UPDATE_LOCATION = 1;

    @Nullable
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (shouldShowRationale) {
                // Show rational.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_dialog_title)
                        .setMessage(R.string.location_permission_dialog_message)
                        .setPositiveButton(
                                R.string.dismiss_button_label,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            } else {
                // Request permission.
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST);
            }
        } else {
            // Read location.
            checkLocationAsync();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ((requestCode == PERMISSION_REQUEST)
                && (grantResults.length == 1)
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            if ((googleApiClient != null) && (googleApiClient.isConnected())) {
                checkLocationAsync();
            }
        }
    }

    @UiThread
    private void checkLocationAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkLocationSync();
            }
        }).start();
    }

    @WorkerThread
    private void checkLocationSync() {
        //noinspection MissingPermission
        Location lastLocation =
                LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation == null) {
            requestLocation();
            return;
        }

        double currentLatitude = lastLocation.getLatitude();
        double currentLongitude = lastLocation.getLongitude();
        Double storedLatitude = EgoEaterPreferences.getLatitude(this);
        Double storedLongitude = EgoEaterPreferences.getLongitude(this);

        if ((storedLatitude != null) && (storedLongitude != null)) {
            Location currentLocation = new Location("");
            currentLocation.setLatitude(currentLatitude);
            currentLocation.setLongitude(currentLongitude);
            Location storedLocation = new Location("");
            storedLocation.setLatitude(storedLatitude);
            storedLocation.setLongitude(storedLongitude);
            if (currentLocation.distanceTo(storedLocation) < MINIMUM_DISTANCE_TO_UPDATE_LOCATION) {
                // Skip. The current location is already stored in the cloud.
                return;
            }
        }

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            new UpdateLocationClientAction(
                    getApplicationContext(),
                    currentLatitude,
                    currentLongitude,
                    city,
                    state,
                    country)
                    .execute();

            // Kick off pipeline to rebuild.
            startService(new Intent(this, PopulatePipelineService.class));
        } catch (IOException ex) {
            Log.e(LOG_TAG, "checkLocationSync: Failed to update location information.", ex);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed: Failed to connect to Google Play Services "
                + connectionResult.getErrorCode() + " - " + connectionResult.getErrorMessage());
    }

    private void requestLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(0);
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(LOG_TAG, "Got location update.");
                        LocationServices.FusedLocationApi.removeLocationUpdates(
                                googleApiClient,
                                this);

                        checkLocationAsync();
                    }
                },
                Looper.getMainLooper());
    }
}
