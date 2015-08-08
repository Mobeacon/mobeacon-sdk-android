package io.mobeacon.sdk.beacon;

import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.location.Geofence;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mobeacon.sdk.geofence.IGeofenceTransitionListener;
import io.mobeacon.sdk.model.Beacon;
import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.nearby.INearbyLocationsListener;

/**
 * Created by maxulan on 08.08.15.
 */
public class BeaconMonitor implements IGeofenceTransitionListener, MonitorNotifier, IBeaconEventPublisher {
    private static final String TAG = "BeaconMonitor";

    private Map<Region, Pair<Location, Beacon>> mBeaconsLocations;
    private List<IBeaconEventListener> mBeaconEventListeners;
    private SDKConf mSdkConf;
    private BeaconManager mBeaconManager;

    public BeaconMonitor(SDKConf conf, BeaconManager beaconManager) {
        mSdkConf  =  conf;
        mBeaconsLocations = new HashMap<Region, Pair<Location, Beacon>>() ;
        mBeaconEventListeners = new ArrayList<IBeaconEventListener>();
        mBeaconManager = beaconManager;
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.i(TAG, "Enter beacon region event happened.");
        Pair<Location, Beacon> beaconLocation =  mBeaconsLocations.get(region);
        notifyListeners(EventType.ENTER, beaconLocation.first, beaconLocation.second);
    }

    @Override
    public void didExitRegion(Region region) {
        Log.i(TAG, "Exit beacon region event happened.");
        Pair<Location, Beacon> beaconLocation =  mBeaconsLocations.get(region);
        notifyListeners(EventType.EXIT, beaconLocation.first, beaconLocation.second);
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
    }

    @Override
    public void subscribe(IBeaconEventListener listener) {
        mBeaconEventListeners.add(listener);
    }

    @Override
    public void unsubscribe(IBeaconEventListener listener) {
        mBeaconEventListeners.remove(listener);
    }

    @Override
    public void onEnterGeofence(Location location) {
        if (location!= null && location.getBeacons() != null && location.getBeacons().size() > 0) {
            for(Beacon beacon : location.getBeacons()) {
                try {
                    Region region = getBeaconRegion(beacon);
                    mBeaconsLocations.put(region, Pair.create(location, beacon));
                    mBeaconManager.startMonitoringBeaconsInRegion(region);
                }
                catch (RemoteException re) {
                }
            }
        }
    }

    @Override
    public void onExitGeofence(Location location) {
        if (location!= null && location.getBeacons() != null && location.getBeacons().size() > 0) {
            for(Beacon beacon : location.getBeacons()) {
                try {
                    Region region = getBeaconRegion(beacon);
                    mBeaconsLocations.remove(region);
                    mBeaconManager.stopMonitoringBeaconsInRegion(getBeaconRegion(beacon));
                }
                catch (RemoteException re) {
                }
            }
        }
    }

    @Override
    public void onLeavingMonitoredRegion() {
        for (Region region : mBeaconsLocations.keySet()) {
            try {
                mBeaconManager.stopMonitoringBeaconsInRegion(region);
            }
            catch (RemoteException re) {
            }
        }
        mBeaconsLocations.clear();
    }
    private static Region getBeaconRegion(Beacon beacon) {
        return new Region(getBeaconRegionId(beacon), Identifier.parse(beacon.getUuid()), Identifier.fromInt(beacon.getMajor()), Identifier.fromInt(beacon.getMinor()));
    }
    private static String getBeaconRegionId(Beacon beacon) {
        return "mobeacon-beacon-"+beacon.getId();
    }
    private void notifyListeners(EventType eventType, Location location, Beacon beacon) {
        if (mBeaconEventListeners != null && location != null) {
            for (IBeaconEventListener listener : mBeaconEventListeners) {
                switch (eventType) {
                    case ENTER:
                        listener.onEnterBeaconArea(location, beacon);
                    case EXIT:
                        listener.onExitBeaconArea(location, beacon);

                }
            }
        }
    }
    private enum EventType {
        ENTER,
        EXIT
    }
}
