package io.mobeacon.sdk.geofence;

import io.mobeacon.sdk.nearby.INearbyLocationsListener;

/**
 * Created by maxulan on 05.08.15.
 */
public interface IGeofenceTransitionEventPublisher {
    void subscribe(IGeofenceTransitionListener listener);
    void unsubscribe(IGeofenceTransitionListener listener);
}
