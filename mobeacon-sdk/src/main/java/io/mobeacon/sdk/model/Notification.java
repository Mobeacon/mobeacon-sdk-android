package io.mobeacon.sdk.model;

import android.support.v4.app.NotificationCompat;

import io.mobeacon.sdk.R;
import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 01.08.15.
 */
public class Notification {
    private long id;
    private String title;
    private String text;
    private boolean vibrate;
    private String actionUrl;

    public Notification(long id, String title, String text, boolean vibrate, String actionUrl) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.vibrate = vibrate;
        this.actionUrl = actionUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public android.app.Notification toAndroidNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MobeaconService.APP_CONTEXT).
                setSmallIcon(R.drawable.notification_template_icon_bg).
                setTicker(title).
                setContentTitle(title).
                setContentText(text).
                setAutoCancel(true);//cancel if notification clicked

        if (vibrate)
            builder.setVibrate(new long[] {0, 200, 200, 600, 600});//vibrate for 200 milliseconds and then stop for 200 milliseconds. After that, it vibrates for 600 milliseconds and then stops for that long.
        return builder.build();
    }
}
