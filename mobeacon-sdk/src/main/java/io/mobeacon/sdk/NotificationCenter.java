package io.mobeacon.sdk;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import io.mobeacon.sdk.geofence.IGeofenceTransitionListener;
import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.SDKConf;

/**
 * Created by maxulan on 04.08.15.
 */
public class NotificationCenter implements IGeofenceTransitionListener{
    public static final String TAG = "NotificationCenter";

    private NotificationSender mNotificationSender;
    private SDKConf mSdkConf;

    public NotificationCenter(Context ctx, SDKConf config) {
        mNotificationSender = new NotificationSender((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));
    }

    public boolean isGeofenceNotificationEnabled() {
        return mSdkConf.isGeofenceEnabled();
    }

    public boolean isBeaconNotificationEnabled() {
        return mSdkConf.isBeaconsEnabled();
    }


    @Override
    public void onEnterGeofence(Location location) {
        // Get the transition details as a String.
        if (location.getGeofence()!= null && location.getGeofence().getNotification() != null) {
            io.mobeacon.sdk.model.Notification notification = location.getGeofence().getNotification();
            Log.i(TAG, String.format("Geofence enter event happend. Sending notification: title='%s', text='%s'", notification.getTitle(), notification.getText()));
            // Send notification and log the transition details.
            mNotificationSender.sendNotification(notification);
        }
    }

    @Override
    public void onExitGeofence(Location location) {
        // Get the transition details as a String.
        if (location.getGeofence()!= null && location.getGeofence().getNotification() != null) {
            io.mobeacon.sdk.model.Notification notification = location.getGeofence().getNotification();
            Log.i(TAG, String.format("Geofence exit event happend. Sending notification: title='%s', text='%s'", notification.getTitle(), notification.getText()));
            // Send notification and log the transition details.
            mNotificationSender.sendNotification(notification);
        }
    }

    @Override
    public void onLeavingMonitoredRegion() {
    }

}
