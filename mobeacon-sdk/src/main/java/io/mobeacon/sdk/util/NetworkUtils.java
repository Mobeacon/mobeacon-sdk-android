package io.mobeacon.sdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 22.07.15.
 */
public class NetworkUtils {

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public static boolean isOnline() throws NetworkUtilsException {
        if (MobeaconService.APP_CONTEXT != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) MobeaconService.APP_CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        throw new NetworkUtilsException("NetworkUtils need app context but MobeaconService.APP_CONTEXT was null");
    }
}
