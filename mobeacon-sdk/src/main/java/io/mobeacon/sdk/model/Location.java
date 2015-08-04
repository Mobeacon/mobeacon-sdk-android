package io.mobeacon.sdk.model;

import android.provider.SyncStateContract;

import java.util.List;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.GeofencingApi;

/**
 * Created by maxulan on 19.07.15.
 */
public class Location {
    private Long id;
    private Long advertiserId;
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

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public com.google.android.gms.location.Geofence getGoogleGeofenceRegion() {
        String requestId  = getGeofenceRequestId();
        if (coordinate != null && geofence!=null && requestId != null ) {
            return new com.google.android.gms.location.Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(requestId)
                    .setCircularRegion(
                            this.coordinate.getLatitude(),
                            this.coordinate.getLongitude(),
                            this.geofence.getRadius()
                    )
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        }
        else return null;
    }
    public String getGeofenceRequestId() {
        if (geofence != null) {
            if (id != null) {
                return id.toString();
            }
        }
        return null;
    }
}
