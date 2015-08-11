package io.mobeacon.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public void sendNotification(io.mobeacon.sdk.model.Notification notification){
        Context context = MobeaconService.APP_CONTEXT;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).
                setSmallIcon(context.getApplicationInfo().icon).
                setTicker(notification.getTitle()).
                setContentTitle(notification.getTitle()).
                setContentText(notification.getText()).
                setAutoCancel(true);//cancel if notification clicked

        PendingIntent notificationPendingIntent = buildNotificationPendingIntent(context);

        if (notificationPendingIntent != null)
            builder.setContentIntent(notificationPendingIntent);

        if (notification.isVibrate())
            builder.setVibrate(new long[] {0, 200, 200, 600, 600});//vibrate for 200 milliseconds and then stop for 200 milliseconds. After that, it vibrates for 600 milliseconds and then stops for that long.

        mNotificationManager.notify((int)notification.getId(), builder.build());
    }

    private PendingIntent buildNotificationPendingIntent(Context context) {
        PackageManager packageMgr = context.getPackageManager();
        Intent launchIntent  = packageMgr.getLaunchIntentForPackage(context.getPackageName());
        String mainActivityClassName = launchIntent.getComponent().getClassName();
        try {
            Intent resultIntent = new Intent(context, Class.forName(mainActivityClassName));
            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            return resultPendingIntent;
        } catch (ClassNotFoundException e) {

        }
        return null;
    }

}
