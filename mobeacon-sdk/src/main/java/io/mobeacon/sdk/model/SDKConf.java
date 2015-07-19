package io.mobeacon.sdk.model;

import com.google.gson.annotations.Expose;

/**
 * Created by maxulan on 19.07.15.
 */
public class SDKConf {
    @Expose
    private boolean enabled;

    @Expose
    private boolean geofenceEnabled;

    @Expose
    private boolean beaconsEnabled;

    @Expose
    private int maxNotificationsDaily;

    @Expose
    private int maxNotificationsFrequencySec;

    public SDKConf() {
        enabled = true;
        geofenceEnabled = true;
        beaconsEnabled = true;
        maxNotificationsDaily = -1; // less than 0 means "unlimited"
        maxNotificationsFrequencySec = -1; // less than 0 means "unlimited"
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isGeofenceEnabled() {
        return geofenceEnabled;
    }

    public void setGeofenceEnabled(boolean geofenceEnabled) {
        this.geofenceEnabled = geofenceEnabled;
    }

    public boolean isBeaconsEnabled() {
        return beaconsEnabled;
    }

    public void setBeaconsEnabled(boolean beaconsEnabled) {
        this.beaconsEnabled = beaconsEnabled;
    }

    public int getMaxNotificationsDaily() {
        return maxNotificationsDaily;
    }

    public void setMaxNotificationsDaily(int maxNotificationsDaily) {
        this.maxNotificationsDaily = maxNotificationsDaily;
    }

    public int getMaxNotificationsFrequencySec() {
        return maxNotificationsFrequencySec;
    }

    public void setMaxNotificationsFrequencySec(int maxNotificationsFrequencySec) {
        this.maxNotificationsFrequencySec = maxNotificationsFrequencySec;
    }
}
