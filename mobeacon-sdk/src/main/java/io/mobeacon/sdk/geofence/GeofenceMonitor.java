package io.mobeacon.sdk.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.mobeacon.sdk.AbstractGoogleLocationServicesClient;
import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.Notification;
import io.mobeacon.sdk.nearby.INearbyLocationsListener;
import io.mobeacon.sdk.nearby.NearbyLocationsLoader;
import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 27.07.15.
 */
public class GeofenceMonitor extends AbstractGoogleLocationServicesClient implements IGeofenceTransitionEventPublisher, INearbyLocationsListener {
    private static final String TAG = "MobeaconGeofenceMonitor";
    public static final String RELOAD_TRIGGER_GEOFENCE_ID = "MOBEACON_RELOAD_TRIGGER_GEOFENCE_ID";

    private List<Geofence> mGeofenceList;
    private Map<String, Location> mGeofenceLocations;
    private PendingIntent mGeofencePendingIntent;
    private List<IGeofenceTransitionListener> mGeofenceTransitionListeners;

    public GeofenceMonitor(Context context) {
        super(context);
        mGeofenceList = new ArrayList<Geofence>();
        mGeofenceLocations = new HashMap<String, Location>();
        mGeofenceTransitionListeners = new ArrayList<IGeofenceTransitionListener>();
    }

    public void processGeofenceIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            processGeofencingEvent(geofencingEvent);
        } else {
            Log.e(TAG, String.format("Failed to handle geofence intent"));
        }
    }

    @Override
    public void onNearbyLocationsChange(double latitude, double longitude, List<Location> nearbyLocations) {
        Log.i(TAG, String.format("Setting up geofence for %d new locations.", nearbyLocations.size()));
        if (nearbyLocations != null) {
            mGeofenceList.clear();
            mGeofenceLocations.clear();

            for (Location location : nearbyLocations) {
                Geofence geofence = location.getGoogleGeofenceRegion();
                if (geofence != null) {
                    Log.d(TAG, String.format("Adding geofence to monitor. Request id - %s", geofence.getRequestId()));
                    mGeofenceList.add(geofence);
                    mGeofenceLocations.put(geofence.getRequestId(), location);
                }
            }
            Log.d(TAG, "Adding special geofence to trigger reload when exit monitored area");
            mGeofenceList.add(buildReloadTriggerGeofence(latitude, longitude, nearbyLocations));
            Log.d(TAG, String.format("Added %d geofences into new motoring list.", mGeofenceList.size()));
            resetGeofencing();
        }
    }

    private Geofence buildReloadTriggerGeofence(double latitude, double longitude, List<Location> nearbyLocations) {
        float radius;
        if (nearbyLocations == null || nearbyLocations.size() < NearbyLocationsLoader.NUMBER_OF_MONITORED_LOCATIONS_NEARBY) {
            radius = NearbyLocationsLoader.MAX_RADIUS;
        } else {
            radius = getDistanceToFarestLocation(latitude, longitude, nearbyLocations);
        }
        return createReloadTriggerGeofence(latitude, longitude, radius * 2 / 3);
    }

    private Geofence createReloadTriggerGeofence(double latitude, double longitude, float geofenceRad) {
        return new Geofence.Builder()
                .setRequestId(RELOAD_TRIGGER_GEOFENCE_ID)
                .setCircularRegion(
                        latitude,
                        longitude,
                        geofenceRad
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private float getDistanceToFarestLocation(double latitude, double longitude, List<Location> locations) {
        float distance = 0f;
        for (Location location : locations) {
            float[] res = new float[3];
            if (location.getCoordinate() != null) {
                android.location.Location.distanceBetween(latitude, longitude, location.getCoordinate().getLatitude(), location.getCoordinate().getLongitude(), res);
                distance = Math.max(distance, res[0]);
            }
        }

        if (distance == 0f) {
            distance = NearbyLocationsLoader.MAX_RADIUS;
        }
        return distance;
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Log.i(TAG, String.format("Building geofence request with %d geofences.", mGeofenceList.size()));
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(MobeaconService.APP_CONTEXT, MobeaconService.class);
        intent.setAction(MobeaconService.ACTION_GEOFENCE);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(MobeaconService.APP_CONTEXT, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void resetGeofencing() {
        Log.i(TAG, "reseting geofencing");
        if (mGeofenceList != null && mGeofenceList.size() > 0) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());
        }
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent());
    }

    @Override
    public void subscribe(IGeofenceTransitionListener listener) {
        mGeofenceTransitionListeners.add(listener);
    }

    @Override
    public void unsubscribe(IGeofenceTransitionListener listener) {
        mGeofenceTransitionListeners.remove(listener);
    }


    private void processGeofencingEvent(GeofencingEvent geofencingEvent) {
        if (geofencingEvent != null) {
            Log.i(TAG, "received geofene event");
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(MobeaconService.APP_CONTEXT,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            if (triggeringGeofences != null && triggeringGeofences.size() > 0) {
                // Get the transition type.
                int geofenceTransition = geofencingEvent.getGeofenceTransition();
                fireGeofenceEvent(triggeringGeofences, geofenceTransition);
            }
        }
    }

    public void fireGeofenceEvent(List<Geofence> triggeringGeofences, int geofenceTransition) {
        if (triggeringGeofences != null && triggeringGeofences.size() > 0) {
            if (triggeringGeofences != null && triggeringGeofences.size() > 0) {
                for (Geofence geofence : triggeringGeofences) {
                    if (geofence.getRequestId() == RELOAD_TRIGGER_GEOFENCE_ID) {
                        fireLocationsReloadRequest();
                    } else {
                        Location location = mGeofenceLocations.get(geofence.getRequestId());
                        notifyListeners(location, geofenceTransition);
                    }
                }
            }
        }
    }

    private void notifyListeners(Location location, int geofenceTransition) {
        if (mGeofenceTransitionListeners != null && location != null) {
            for (IGeofenceTransitionListener listener : mGeofenceTransitionListeners) {
                switch (geofenceTransition) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        listener.onEnterGeofence(location);
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        listener.onExitGeofence(location);
                }
            }
        }
    }

    private void fireLocationsReloadRequest() {
        for (IGeofenceTransitionListener listener : mGeofenceTransitionListeners) {
            listener.onLeavingMonitoredRegion();
        }
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "geofence transition entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "geofence transition exited";
            default:
                return "unknown geofence transition";
        }
    }

}
