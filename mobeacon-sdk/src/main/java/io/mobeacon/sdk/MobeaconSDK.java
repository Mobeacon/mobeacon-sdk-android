package io.mobeacon.sdk;

import android.content.Context;

import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 15.07.15.
 */
public class MobeaconSDK {
    public static final String TAG = "MobeaconSDK";

    public static final String VERSION = "1.0";
    public static final String PREFERENCES = "MOBEACON_SDK_PREFERENCES_FILE";

    public static final String PREF_KEY_SDK_INSTALL_DT = "sdkInstallDateTime";

    private static MobeaconSDK instance;

    public static synchronized MobeaconSDK getInstance(Context ctx, String appKey, String logLevel) {
        if (instance == null)
        {
            instance = new MobeaconSDK(ctx, appKey, logLevel);
        }
        return instance;
    }

    private final String appKey, logLevel;
    private final Context ctx;

    private MobeaconSDK(Context ctx, String appKey, String logLevel) {
        this.ctx = ctx;
        this.appKey = appKey;
        this.logLevel = logLevel;
        MobeaconService.start(ctx, appKey);
    }

    public String getAppKey() {
        return appKey;
    }

    public String getLogLevel() {
        return logLevel;
    }
}
