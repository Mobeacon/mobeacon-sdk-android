package io.mobeacon.sdk.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * Created by maxulan on 28.07.15.
 */
public class LocationUtils {
    // Check Wi-Fi is on
    public static boolean isWiFiEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo.isAvailable();
    }

    // Check Airplane Mode - we want airplane mode off
    public static boolean isAirplaneModeEnabled(Context context) {
        int airplaneSetting =
                Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) ;
        return airplaneSetting != 0;
    }
    // Check that Network Location Provider reports enabled
    public static boolean isNetLocEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
