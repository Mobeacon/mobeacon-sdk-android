package io.mobeacon.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * Created by maxulan on 19.07.15.
 */
public class SDKConf implements Parcelable{
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte)(enabled?1:0));
        dest.writeByte((byte)(geofenceEnabled?1:0));
        dest.writeByte((byte)(beaconsEnabled?1:0));
        dest.writeInt(maxNotificationsDaily);
        dest.writeInt(maxNotificationsFrequencySec);
    }

    public static final Parcelable.Creator<SDKConf> CREATOR = new Parcelable.Creator<SDKConf> () {
        public SDKConf createFromParcel(Parcel in) {
            return new SDKConf(in);
        }
        public SDKConf[] newArray(int size) {
            return new SDKConf[size];
        }
    };
    private SDKConf(Parcel in) {
        enabled = in.readByte() == 1;
        geofenceEnabled = in.readByte() == 1;
        beaconsEnabled = in.readByte() == 1;
        maxNotificationsDaily = in.readInt();
        maxNotificationsFrequencySec = in.readInt();
    }
}
