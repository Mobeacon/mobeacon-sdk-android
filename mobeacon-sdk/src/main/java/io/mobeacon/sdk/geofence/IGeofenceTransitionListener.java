package io.mobeacon.sdk.geofence;

import com.google.android.gms.location.GeofencingEvent;

import io.mobeacon.sdk.model.Location;

/**
 * Created by maxulan on 04.08.15.
 */
public interface IGeofenceTransitionListener {
    void onEnterGeofence(Location location);
    void onExitGeofence(Location location);
    void onLeavingMonitoredRegion();
}
