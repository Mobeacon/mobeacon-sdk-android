package io.mobeacon.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import io.mobeacon.sdk.model.SDKContextFactory;
import io.mobeacon.sdk.rest.RestClient;
import io.mobeacon.sdk.util.DateTimeUtils;

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
            SDKContext sdkContext = SDKContextFactory.create(ctx, appKey);
            SDKConf sdkConfig = new RestClient().getSDKInitializationService().init(appKey, sdkContext);
            Log.i(TAG, String.format("SDK initialization completed. Config: isEnabled=%s", sdkConfig.isEnabled()));
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
