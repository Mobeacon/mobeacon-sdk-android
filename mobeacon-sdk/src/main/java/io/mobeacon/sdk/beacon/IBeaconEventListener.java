package io.mobeacon.sdk.beacon;

import io.mobeacon.sdk.model.Beacon;
import io.mobeacon.sdk.model.Location;

/**
 * Created by maxulan on 08.08.15.
 */
public interface IBeaconEventListener {
    void onEnterBeaconArea(Location location, Beacon beacon);
    void onExitBeaconArea(Location location, Beacon beacon);
}
