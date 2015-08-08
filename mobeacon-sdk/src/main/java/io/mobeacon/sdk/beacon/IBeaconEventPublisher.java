package io.mobeacon.sdk.beacon;

import io.mobeacon.sdk.geofence.IGeofenceTransitionListener;

/**
 * Created by maxulan on 08.08.15.
 */
public interface IBeaconEventPublisher {
    void subscribe(IBeaconEventListener listener);
    void unsubscribe(IBeaconEventListener listener);
}
