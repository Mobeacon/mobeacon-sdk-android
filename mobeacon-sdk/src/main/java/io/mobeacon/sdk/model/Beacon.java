package io.mobeacon.sdk.model;

/**
 * Created by maxulan on 24.07.15.
 */
public class Beacon {
    private long id;
    private String uuid;
    private int major;
    private int minor;
    private String proximity;
    private Notification notification;

    public Beacon(long id, String uuid, int major, int minor, String proximity) {
        this.id = id;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.proximity = proximity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getProximity() {
        return proximity;
    }

    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

}
