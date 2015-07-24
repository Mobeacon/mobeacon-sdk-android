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
        return getInstance(ctx, appKey, null, logLevel);
    }

    public static synchronized MobeaconSDK getInstance(Context ctx, String appKey, String googleId, String logLevel) {
        if (instance == null)
        {
            instance = new MobeaconSDK(ctx, appKey, googleId, logLevel);
        }
        return instance;
    }

    private final String appKey, googleId, logLevel;
    private final Context ctx;

    private MobeaconSDK(Context ctx, String appKey, String googleId, String logLevel) {
        this.ctx = ctx;
        this.appKey = appKey;
        this.googleId = googleId;
        this.logLevel = logLevel;
        MobeaconService.startActionInit(ctx, appKey, googleId);
    }

    public String getAppKey() {
        return appKey;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getLogLevel() {
        return logLevel;
    }
}
