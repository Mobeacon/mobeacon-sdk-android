package io.mobeacon.sdk.model;

import java.util.List;

/**
 * Created by maxulan on 19.07.15.
 */
public class Location {
    private long id;
    private long advertiserId;
    private String name;
    private GeoPoint coordinate;
    private Geofence geofence;
    private List<Beacon> beacons;

    public Location(long id, long advertiserId, String name, GeoPoint coordinate, Geofence geofence, List<Beacon> beacons) {
        this.id = id;
        this.advertiserId = advertiserId;
        this.name = name;
        this.coordinate = coordinate;
        this.geofence = geofence;
        this.beacons = beacons;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAdvertiserId() {
        return advertiserId;
    }

    public void setAdvertiserId(long advertiserId) {
        this.advertiserId = advertiserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(GeoPoint coordinate) {
        this.coordinate = coordinate;
    }

    public Geofence getGeofence() {
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        this.geofence = geofence;
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<Beacon> beacons) {
        this.beacons = beacons;
    }
}
