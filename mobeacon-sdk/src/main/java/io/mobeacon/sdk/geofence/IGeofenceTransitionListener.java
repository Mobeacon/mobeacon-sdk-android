package io.mobeacon.sdk.geofence;

import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by maxulan on 04.08.15.
 */
public interface IGeofenceTransitionListener {
    void onGeofenceTransition(GeofencingEvent event);
}
