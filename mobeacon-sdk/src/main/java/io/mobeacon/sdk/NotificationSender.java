package io.mobeacon.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;


import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 02.08.15.
 */
public class NotificationSender {
    private NotificationManager mNotificationManager;
    public NotificationSender(NotificationManager notificationManager) {
        mNotificationManager = notificationManager;
    }
    public void sendNotification(String title, String text){
        Notification notification = new NotificationCompat.Builder(MobeaconService.APP_CONTEXT).
                setSmallIcon(R.drawable.notification_template_icon_bg).
                setContentTitle("Test geofence notification").
                setContentText(text).
                build();
        mNotificationManager.notify(0, notification);
    }
    public void sendNotification(io.mobeacon.sdk.model.Notification notification){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MobeaconService.APP_CONTEXT).
                setSmallIcon(R.drawable.notification_template_icon_bg).
                setTicker(notification.getTitle()).
                setContentTitle(notification.getTitle()).
                setContentText(notification.getText()).
                setAutoCancel(true);//cancel if notification clicked

        if (notification.isVibrate())
            builder.setVibrate(new long[] {0, 200, 200, 600, 600});//vibrate for 200 milliseconds and then stop for 200 milliseconds. After that, it vibrates for 600 milliseconds and then stops for that long.
        mNotificationManager.notify((int)notification.getId(), builder.build());
    }

}
