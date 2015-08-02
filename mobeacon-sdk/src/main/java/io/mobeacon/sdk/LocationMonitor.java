package io.mobeacon.sdk;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import io.mobeacon.sdk.rest.MobeaconRestApi;
import io.mobeacon.sdk.services.MobeaconService;
import rx.functions.Action1;

/**
 * Created by maxulan on 22.07.15.
 */
public class LocationMonitor implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private static final String TAG = "MobeaconLocationMgr";
    private static final int DEFAULT_RADIUS = 5000;
    private static final int DEFAULT_LIMIT = 10;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MobeaconRestApi mobeaconRestApi;

   private GeofencingManager mGeofencingManager;
    private String appKey;

    private static LocationMonitor INSTANCE = new LocationMonitor();

    private LocationMonitor() {
        buildGoogleApiClient();
        createLocationRequest();
    }
    public static LocationMonitor instance() {
        return INSTANCE;
    }
    public static LocationMonitor init(String appKey, MobeaconRestApi mobeaconRestApi) {
        INSTANCE.mobeaconRestApi = mobeaconRestApi;
        INSTANCE.appKey = appKey;
        INSTANCE.mGoogleApiClient.connect();
        return INSTANCE;
    }

    public synchronized GeofencingManager getGeofencingManager() {
        return mGeofencingManager;
    }

    public synchronized void enableGeofencingNotifications(NotificationSender notificationSender) {
        if (mGeofencingManager == null) {
            this.mGeofencingManager = new GeofencingManager(mGoogleApiClient, notificationSender);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MobeaconService.APP_CONTEXT)
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
            Log.i(TAG, String.format("Current location. Latitude=%f Longitude=%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
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

    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void onLocationChanged(Location loc) {
        if (loc != null) {
            Log.i(TAG, String.format("location update: Latitude=%f Longitude=%f", loc.getLatitude(), loc.getLongitude()));
            mobeaconRestApi.getNearestLocations(appKey, loc.getLatitude(), loc.getLongitude(), DEFAULT_RADIUS, DEFAULT_LIMIT).subscribe(new Action1<List<io.mobeacon.sdk.model.Location>>() {
                @Override
                public void call(List<io.mobeacon.sdk.model.Location> locations) {
                    if (locations != null) {
                        Log.i(TAG, String.format("Found %d locations nearby", locations.size()));
                        if (mGeofencingManager != null) {
                            mGeofencingManager.setLocations(locations);
                        }
                        //from here we use Geofence instad of GPS to save energy
                        stopLocationUpdates();
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.i(TAG, String.format("Failed to find locations nearby: %s", throwable.getLocalizedMessage()));
                }
            });
        }
        else  {
            Log.e(TAG, "Failed to update location");
        }
    }

}
