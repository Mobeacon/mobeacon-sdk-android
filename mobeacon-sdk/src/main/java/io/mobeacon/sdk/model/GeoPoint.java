package io.mobeacon.sdk.model;

/**
 * Created by maxulan on 24.07.15.
 */
public class GeoPoint {

    private double latitude;
    private double longitude;

    public GeoPoint(double longitude, double latitude) {

        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
