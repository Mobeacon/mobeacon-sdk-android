package io.mobeacon.sdk.model;

/**
 * Created by maxulan on 24.07.15.
 */
public class Geofence {

    private int radius;

    public Geofence(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
