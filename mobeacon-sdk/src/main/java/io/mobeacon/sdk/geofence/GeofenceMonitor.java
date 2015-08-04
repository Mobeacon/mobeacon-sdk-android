package io.mobeacon.sdk.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
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
import io.mobeacon.sdk.NotificationSender;
import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.nearby.INearbyLocationsListener;
import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 27.07.15.
 */
public class GeofenceMonitor extends AbstractGoogleLocationServicesClient implements INearbyLocationsListener {
    private static final String TAG = "MobeaconGeofencingMgr";
    public static final String RELOAD_TRIGGER_GEOFENCE_ID = "MOBEACON_RELOAD_TRIGGER_GEOFENCE_ID";

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    public GeofenceMonitor(Context context) {
        super(context);
        mGeofenceList = new ArrayList<>();
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
    public void onNearbyLocationsChange(List<Location> locations) {
        Log.i(TAG, String.format("Setting %d new locations to monitor.", locations.size()));
        if (locations != null) {
            mGeofenceList.clear();

            for(Location location : locations) {
                Geofence geofence = location.getGoogleGeofenceRegion();
                if (geofence != null) {
                    mGeofenceList.add(geofence);
                    Log.i(TAG, String.format("Adding geofence to monitor. Request id - %s", geofence.getRequestId()));
                }
            }
            Log.i(TAG, String.format("Added %d geofences into new motoring list.", mGeofenceList.size()));
            //TODO add RELOAD_TRIGGER_GEOFENCE
//            mGeofenceList.add()
            resetGeofencing();
        }
    }


}
