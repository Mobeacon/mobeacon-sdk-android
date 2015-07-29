package io.mobeacon.sdk;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 27.07.15.
 */
public class GeofencingManager {
    private static final String TAG = "MobeaconGeofencingMgr";
    private List<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    public GeofencingManager(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
        mGeofenceList = new ArrayList<>();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Log.i(TAG, String.format("Building geofence request with %d geofences.", mGeofenceList.size()));
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void setLocations(List<Location> locations) {

        if (locations != null) {
            mGeofenceList.clear();
            for(Location location : locations) {
                Geofence geofence = location.getGoogleGeofenceRegion();
                if (geofence != null) {
                    mGeofenceList.add(geofence);
                    Log.i(TAG, String.format("Added %d geofences into new motoring list.", mGeofenceList.size()));
                }
            }
            resetGeofencing();
        }

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
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent());
    }
}
