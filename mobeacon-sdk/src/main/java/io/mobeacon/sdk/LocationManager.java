package io.mobeacon.sdk;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by maxulan on 22.07.15.
 */
public class LocationManager implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private static final String TAG = "MobeaconLocationManager";

    private Context ctx;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public LocationManager(Context ctx) {
        this.ctx = ctx;
        buildGoogleApiClient();
        createLocationRequest();
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i(TAG, String.format("Current location. Latitude=%d Longitude=%d", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
        else  {
            Log.e(TAG, "Connected but failed to receive last location");
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int var1) {
        Log.e(TAG, "Google play services connection suspended");
    }

    public void onConnectionFailed(ConnectionResult var1){
        Log.e(TAG, "Google play services connection failed: " + var1.toString());
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    public void onLocationChanged(Location loc) {
        if (loc != null) {
            Log.i(TAG, String.format("location update: Latitude=%d Longitude=%d", loc.getLatitude(), loc.getLongitude()));
        }
        else  {
            Log.e(TAG, "Failed to update location");
        }
    }

}
