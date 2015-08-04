package io.mobeacon.sdk.nearby;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import io.mobeacon.sdk.AbstractGoogleLocationServicesClient;
import io.mobeacon.sdk.geofence.GeofenceMonitor;
import io.mobeacon.sdk.NotificationSender;
import io.mobeacon.sdk.geofence.IGeofenceTransitionListener;
import io.mobeacon.sdk.rest.MobeaconRestApi;
import io.mobeacon.sdk.services.MobeaconService;
import rx.functions.Action1;

/**
 * Created by maxulan on 22.07.15.
 */
public class NearbyLocationsLoader extends AbstractGoogleLocationServicesClient implements LocationListener, INearbyLocationsPublisher, IGeofenceTransitionListener {
    private static final String TAG = "MobeaconLocationMgr";
    private static final int DEFAULT_RADIUS = 5000;
    private static final int DEFAULT_LIMIT = 10;

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
    private void notifyAboutNewLocations(List<io.mobeacon.sdk.model.Location> locations) {
        if (nearbyLocationsListeners != null && locations != null) {
            for (INearbyLocationsListener listener : nearbyLocationsListeners) {
               listener.onNearbyLocationsChange(locations);
            }
        }
    }
    public void onLocationChanged(Location loc) {
        if (loc != null) {
            Log.i(TAG, String.format("location update: Latitude=%f Longitude=%f", loc.getLatitude(), loc.getLongitude()));
            mobeaconRestApi.getNearestLocations(appKey, loc.getLatitude(), loc.getLongitude(), DEFAULT_RADIUS, DEFAULT_LIMIT).subscribe(new Action1<List<io.mobeacon.sdk.model.Location>>() {
                @Override
                public void call(List<io.mobeacon.sdk.model.Location> locations) {
                    if (locations != null) {
                        Log.i(TAG, String.format("Found %d locations nearby", locations.size()));
                        notifyAboutNewLocations(locations);
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
    @Override
    public void subscribe(INearbyLocationsListener listener) {
        nearbyLocationsListeners.add(listener);
    }

    @Override
    public void unsubscribe(INearbyLocationsListener listener) {
        nearbyLocationsListeners.remove(listener);
    }

    @Override
    public void onGeofenceTransition(GeofencingEvent event) {
        //TODO on "reload radius" geofence exit - reload locations
    }
}
