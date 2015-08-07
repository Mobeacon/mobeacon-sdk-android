package io.mobeacon.sdk.nearby;

import java.util.List;

import io.mobeacon.sdk.model.Location;

/**
 * Created by maxulan on 04.08.15.
 */
public interface INearbyLocationsListener {
    void onNearbyLocationsChange(double latitude, double longitude, List<Location> nearbyLocations);
}
