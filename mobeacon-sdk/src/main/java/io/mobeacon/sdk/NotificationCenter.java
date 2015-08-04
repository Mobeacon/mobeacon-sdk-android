package io.mobeacon.sdk;

import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.mobeacon.sdk.geofence.GeofenceErrorMessages;
import io.mobeacon.sdk.geofence.IGeofenceTransitionListener;
import io.mobeacon.sdk.model.Beacon;
import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.Notification;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.nearby.INearbyLocationsListener;
import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 04.08.15.
 */
public class NotificationCenter implements IGeofenceTransitionListener, INearbyLocationsListener{
    public static final String TAG = "NotificationCenter";

    private Map<String, Notification> mGeofenceNotifications;
    private Map<String, Notification> mBeaconNotifications;
    private NotificationSender notificationSender;
    private SDKConf sdkConf;
    private GeofenceEventProcessor geofenceEventProcessor;

    public NotificationCenter(Context ctx, SDKConf config) {
        notificationSender = new NotificationSender((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));
        mGeofenceNotifications = new HashMap<>();
        mBeaconNotifications = new HashMap<>();
        geofenceEventProcessor = new GeofenceEventProcessor();
    }

    public boolean isGeofenceNotificationEnabled() {
        return sdkConf.isGeofenceEnabled();
    }
    public boolean isBeaconNotificationEnabled() {
        return sdkConf.isBeaconsEnabled();
    }

    @Override
    public void onGeofenceTransition(GeofencingEvent event) {
        geofenceEventProcessor.process(event);
    }

    @Override
    public void onNearbyLocationsChange(List<Location> locations) {
        Log.i(TAG, String.format("Setting %d new locations to monitor.", locations.size()));
        if (locations != null) {
            mGeofenceNotifications.clear();
            mBeaconNotifications.clear();

            for(Location location : locations) {
                addGeofenceNotificationFor(location);
                addBeaconNotificationsFor(location);
            }
            Log.i(TAG, String.format("Added %d geofence notifications into new motoring list.", mGeofenceNotifications.size()));

        }
    }
    private void addGeofenceNotificationFor(Location location) {
        if (location.getGeofence() != null) {
            if (location.getGeofence().getNotification() != null ) {
                Log.i(TAG, String.format("Adding geofence notification. Request id - %s", location.getGeofenceRequestId()));
                mGeofenceNotifications.put(location.getGeofenceRequestId(), location.getGeofence().getNotification());
            }
        }
    }
    private void addBeaconNotificationsFor(Location location) {
        if (location.getBeacons() != null) {
            for (Beacon beacon : location.getBeacons()) {
                if (beacon.getNotification() !=null) {
                    mBeaconNotifications.put(String.valueOf(beacon.getId()), beacon.getNotification());
                }
            }
        }
    }

    private class GeofenceEventProcessor {
        private Random randomizer = new Random();
        public GeofenceEventProcessor() {
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
