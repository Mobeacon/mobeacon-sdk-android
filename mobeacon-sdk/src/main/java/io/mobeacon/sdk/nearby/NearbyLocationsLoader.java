package io.mobeacon.sdk.nearby;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import io.mobeacon.sdk.AbstractGoogleLocationServicesClient;
import io.mobeacon.sdk.geofence.IGeofenceTransitionListener;
import io.mobeacon.sdk.rest.MobeaconRestApi;
import rx.functions.Action1;

/**
 * Created by maxulan on 22.07.15.
 */
public class NearbyLocationsLoader extends AbstractGoogleLocationServicesClient implements LocationListener, INearbyLocationsPublisher, IGeofenceTransitionListener {
    private static final String TAG = "NearbyLocationsLoader";
    public static final float MAX_RADIUS = 5000f;//in meters
    public static final int NUMBER_OF_MONITORED_LOCATIONS_NEARBY = 10;

    private LocationRequest mLocationRequest;
    private MobeaconRestApi mobeaconRestApi;

    private List<INearbyLocationsListener> nearbyLocationsListeners;
    private String appKey;

    public NearbyLocationsLoader(Context context, String appKey, MobeaconRestApi mobeaconRestApi) {
        super(context);
        this.mobeaconRestApi = mobeaconRestApi;
        this.appKey = appKey;
        nearbyLocationsListeners = new ArrayList<>();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        Log.i(TAG, "Starting location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        Log.i(TAG, "Stopping location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    private void notifyAboutNewLocations(double latitude, double longitude, List<io.mobeacon.sdk.model.Location> locations) {
        if (nearbyLocationsListeners != null && locations != null) {
            for (INearbyLocationsListener listener : nearbyLocationsListeners) {
               listener.onNearbyLocationsChange(latitude, longitude, locations);
            }
        }
    }
    public void onLocationChanged(final Location loc) {
        if (loc != null) {
            Log.i(TAG, String.format("location update: Latitude=%f Longitude=%f", loc.getLatitude(), loc.getLongitude()));
            mobeaconRestApi.getNearestLocations(appKey, loc.getLatitude(), loc.getLongitude(), MAX_RADIUS, NUMBER_OF_MONITORED_LOCATIONS_NEARBY).subscribe(new Action1<List<io.mobeacon.sdk.model.Location>>() {
                @Override
                public void call(List<io.mobeacon.sdk.model.Location> nearbyLocations) {
                    if (nearbyLocations != null) {
                        Log.i(TAG, String.format("Found %d locations nearby", nearbyLocations.size()));
                        notifyAboutNewLocations(loc.getLatitude(), loc.getLongitude(), nearbyLocations);
                        //from here we use Geofence instad of GPS to save energy
                        stopLocationUpdates();
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.e(TAG, String.format("Failed to find locations nearby: %s", throwable.getLocalizedMessage()), throwable);
                }
            });
        }
        else  {
            Log.e(TAG, "Failed to update location");
        }
    }
    @Override
    public void subscribe(INearbyLocationsListener listener) {
        nearbyLocationsListeners.add(listener);
    }

    @Override
    public void unsubscribe(INearbyLocationsListener listener) {
        nearbyLocationsListeners.remove(listener);
    }

    @Override
    public void onLeavingMonitoredRegion() {
        startLocationUpdates();
    }

    @Override
    public void onEnterGeofence(io.mobeacon.sdk.model.Location location) {
        //do nothing
    }

    @Override
    public void onExitGeofence(io.mobeacon.sdk.model.Location location) {
        //do nothing
    }
}
