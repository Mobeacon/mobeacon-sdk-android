package io.mobeacon.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
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

import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 27.07.15.
 */
public class GeofencingManager {
    private static final String TAG = "MobeaconGeofencingMgr";
    private List<Geofence> mGeofenceList;
    private Map<String, io.mobeacon.sdk.model.Notification> mGeofenceNotifications;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;


    private GeofenceEventProcessor geofenceEventProcessor;

    public GeofencingManager(GoogleApiClient googleApiClient, NotificationSender notificationSender) {
        mGoogleApiClient = googleApiClient;
        mGeofenceList = new ArrayList<>();
        mGeofenceNotifications = new HashMap<>();
        geofenceEventProcessor = new GeofenceEventProcessor(notificationSender);
    }

    public void processGeofencingEvent(GeofencingEvent geofencingEvent) {
        geofenceEventProcessor.process(geofencingEvent);
    }

    public void setLocations(List<Location> locations) {
        Log.i(TAG, String.format("Setting %d new locations to monitor.", locations.size()));
        if (locations != null) {
            mGeofenceList.clear();
            mGeofenceNotifications.clear();

            for(Location location : locations) {
                Geofence geofence = location.getGoogleGeofenceRegion();
                if (geofence != null) {
                    mGeofenceList.add(geofence);
                    Log.i(TAG, String.format("Adding geofence to monitor. Request id - %s", geofence.getRequestId()));
                    if (location.getGeofence().getNotification() != null ) {
                        mGeofenceNotifications.put(geofence.getRequestId(),location.getGeofence().getNotification());
                    }
                }
            }
            Log.i(TAG, String.format("Added %d geofences into new motoring list.", mGeofenceList.size()));

            resetGeofencing();
        }

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

    private class GeofenceEventProcessor {
        private NotificationSender notificationSender;
        private Random randomizer = new Random();
        public GeofenceEventProcessor(NotificationSender notificationSender) {
            this.notificationSender = notificationSender;
        }

        public void process(GeofencingEvent geofencingEvent) {
            Log.i(TAG, "received geofene event");
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(MobeaconService.APP_CONTEXT,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                io.mobeacon.sdk.model.Notification notification = getRandomGeofenceNotificationElseNull(triggeringGeofences);
                if (notification != null) {
                    Log.i(TAG, String.format("Geofence transition event happend. Sending notification: title='%s', text='%s'", notification.getTitle(), notification.getText()));
                    // Send notification and log the transition details.
                    notificationSender.sendNotification(notification);
                }
            } else {
                // Log the error.
                Log.e(TAG, "geofence_transition_invalid_type: " + geofenceTransition);
            }
        }

        public io.mobeacon.sdk.model.Notification getRandomGeofenceNotificationElseNull(List<Geofence> triggeringGeofences) {
            if (triggeringGeofences != null && triggeringGeofences.size() > 0) {
                int i = randomizer.nextInt(triggeringGeofences.size());
                Geofence geofence = triggeringGeofences.get(i);

                io.mobeacon.sdk.model.Notification notification = mGeofenceNotifications.get(geofence.getRequestId());
                if (notification == null) {
                    triggeringGeofences.remove(i);
                    return getRandomGeofenceNotificationElseNull(triggeringGeofences);
                }
                return notification;
            }
            return null;
        }
        /**
         * Gets transition details and returns them as a formatted string.
         *
         * @param context               The app context.
         * @param geofenceTransition    The ID of the geofence transition.
         * @param triggeringGeofences   The geofence(s) triggered.
         * @return                      The transition details formatted as String.
         */
        private String getGeofenceTransitionDetails(
                Context context,
                int geofenceTransition,
                List<Geofence> triggeringGeofences) {

            String geofenceTransitionString = getTransitionString(geofenceTransition);

            // Get the Ids of each geofence that was triggered.
            ArrayList triggeringGeofencesIdsList = new ArrayList();
            for (Geofence geofence : triggeringGeofences) {
                triggeringGeofencesIdsList.add(geofence.getRequestId());
            }
            String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

            return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
        }

        /**
         * Maps geofence transition types to their human-readable equivalents.
         *
         * @param transitionType    A transition type constant defined in Geofence
         * @return                  A String indicating the type of transition
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
}
