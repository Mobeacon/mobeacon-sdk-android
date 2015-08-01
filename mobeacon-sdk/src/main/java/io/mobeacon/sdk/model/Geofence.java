package io.mobeacon.sdk.model;

/**
 * Created by maxulan on 24.07.15.
 */
public class Geofence {

    private int radius;
    private Notification notification;

    public Geofence(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
