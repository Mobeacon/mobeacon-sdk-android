package io.mobeacon.sdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.mobeacon.sdk.MobeaconSDK;
import io.mobeacon.sdk.model.SDKContext;
import io.mobeacon.sdk.util.DateTimeUtils;

/**
 * Created by maxulan on 19.07.15.
 */
public class SDKContextFactory {

    public static SDKContext create(Context ctx, String appKey) {
        SDKContext sdkContext = new SDKContext();

        sdkContext.setSdkVersion(MobeaconSDK.VERSION);
        sdkContext.setSdkInstallDateTime(getSdkInstallDT(ctx));

        sdkContext.setAppKey(appKey);
        AppInfo app = new AppInfo(ctx);
        sdkContext.setAppBundle(app.getPkg());
        sdkContext.setAppVersion(app.getVersion());
        sdkContext.setAppInstallDateTime(app.getInstallDateTime());

        sdkContext.setSessionStartDateTime(getSessionStartDT());

        sdkContext.setOS("Android");
        sdkContext.setOSVersion(Build.VERSION.RELEASE);
        sdkContext.setLang(getLang());
        sdkContext.setLang(getCountry());

        sdkContext.setProvider(getProvider(ctx));

        sdkContext.setDeviceManufacturer(Build.MANUFACTURER);
        sdkContext.setDeviceModel(Build.MODEL);
        sdkContext.setDeviceHardwareId(Build.SERIAL);
        sdkContext.setDevicePlatformId(getAndroidId(ctx));
        sdkContext.setDeviceType(getDeviceType(ctx));

        DisplayMetrics screen = getScreen(ctx);
        sdkContext.setScreenWidth(screen.widthPixels);
        sdkContext.setScreenHeight(screen.heightPixels);
        sdkContext.setScreenPPI(screen.densityDpi);
        return sdkContext;

    }

    private static DisplayMetrics getScreen(Context ctx) {
        return ctx.getResources().getDisplayMetrics();
    }

    private static String getSessionStartDT()
    {
        //TODO should be connected to activity life circle
        return DateTimeUtils.now();
    }
    //TODO refactor: move to separate Config class
    private static String getSdkInstallDT(Context ctx) {

        SharedPreferences settings = ctx.getSharedPreferences(MobeaconSDK.PREFERENCES, 0);
        String sdkInstallDt = settings.getString(MobeaconSDK.PREF_KEY_SDK_INSTALL_DT, null);

        if (sdkInstallDt == null) {
            sdkInstallDt = DateTimeUtils.now();

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(MobeaconSDK.PREF_KEY_SDK_INSTALL_DT, sdkInstallDt);

            // Commit the edits!
            editor.commit();
        }
        return sdkInstallDt;
    }

    private static String getLang() {
        return Locale.getDefault().getLanguage();
    }

    private static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    private static String getAndroidId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private static void getAppName(Context ctx) {
        int stringId = ctx.getApplicationInfo().labelRes;
        String hostApp = ctx.getString(stringId);
    }

    private static String getProvider(Context ctx) {
        TelephonyManager tManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tManager.getNetworkOperatorName();
    }

    /**
     * getDeviceType
     *
     * @param context
     * @return "Tablet" or "Smartphone"
     */
    public static String getDeviceType(Context context) {
        boolean tablet = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        return tablet? "Tablet": "Smartphone";
    }


    private static class AppInfo {
        private String pkg;
        private String version;
        private String installDateTime;

        private AppInfo(Context ctx) {
            try {
                PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                version = pInfo.versionName;
                pkg = pInfo.packageName;
                installDateTime = DateTimeUtils.dtToStr(new Date(pInfo.firstInstallTime));
            } catch (PackageManager.NameNotFoundException ex) {
                pkg = "";
                version = "";
                installDateTime = "";
            }
        }
        public String getPkg() {
            return pkg;
        }public String getVersion() {
            return version;
        }public String getInstallDateTime() {
            return installDateTime;
        }
    }
}