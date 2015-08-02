package io.mobeacon.sdk.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import io.mobeacon.sdk.LocationMonitor;
import io.mobeacon.sdk.NotificationSender;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import io.mobeacon.sdk.model.SDKContextFactory;
import io.mobeacon.sdk.rest.MobeaconRestApi;
import io.mobeacon.sdk.rest.MobeaconRestApiFactory;
import io.mobeacon.sdk.util.LocationUtils;
import rx.functions.Action1;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MobeaconService extends IntentService{//} implements BeaconConsumer {
    public static Context APP_CONTEXT;

    private static final String TAG = "MobeaconService";

    // IntentService can perform, e.g. ACTION_INIT
    public static final String ACTION_INIT = "io.mobeacon.sdk.services.action.INIT";
    public static final String ACTION_CONFIG_SDK = "io.mobeacon.sdk.services.action.CONFIG_SDK";
    public static final String ACTION_GEOFENCE = "io.mobeacon.sdk.services.action.GEOFENCE";

    private static final String EXTRA_SDK_CONFIG = "io.mobeacon.sdk.services.extra.SDK_CONFIG";

    private static final String EXTRA_APP_KEY = "io.mobeacon.sdk.services.extra.APP_KEY";
    private static final String EXTRA_GOOGLE_ID = "io.mobeacon.sdk.services.extra.GOOGLE_ID";

    private MobeaconRestApi mobeaconRestApi;
    private BeaconManager beaconManager;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionInit(Context context, String appKey, String googleProjectId) {
        Intent intent = new Intent(context, MobeaconService.class);
        intent.setAction(ACTION_INIT);
        intent.putExtra(EXTRA_APP_KEY, appKey);
        intent.putExtra(EXTRA_GOOGLE_ID, googleProjectId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionConfig(Context context, String appKey, SDKConf conf) {
        Intent intent = new Intent(context, MobeaconService.class);
        intent.setAction(ACTION_CONFIG_SDK);
        intent.putExtra(EXTRA_APP_KEY, appKey);
        intent.putExtra(EXTRA_SDK_CONFIG, conf);
        context.startService(intent);
    }

    public MobeaconService() {
        super("MobeaconService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(TAG, String.format("Handle intent action %s ", action));

            if (ACTION_INIT.equals(action)) {
                final String appKey = intent.getStringExtra(EXTRA_APP_KEY);
                final String googleId = intent.getStringExtra(EXTRA_GOOGLE_ID);
                handleActionInit(appKey, googleId);
            }
            else if(ACTION_CONFIG_SDK.equals(action)) {
                final String appKey = intent.getStringExtra(EXTRA_APP_KEY);
                final SDKConf conf = intent.getParcelableExtra(EXTRA_SDK_CONFIG);
                configureSDK(appKey, conf);
            }
            else if (ACTION_GEOFENCE.equals(action)) {
                Log.i(TAG, String.format("Handle geofence intent action %s ", action));

                if(LocationMonitor.instance().getGeofencingManager() != null) {
                    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
                    LocationMonitor.instance().getGeofencingManager().processGeofencingEvent(geofencingEvent);
                }
                else {
                    Log.e(TAG, String.format("Failed to handle geofence intent:  LocationMonitor.instance().getGeofencingManager()=%s", LocationMonitor.instance().getGeofencingManager()));
                }
            }
        }
    }

    /**
     * Handle SDK init action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInit(final String appKey, final String googleProjectId) {
        Log.i(TAG, String.format("Handle init action. Thread id %s ", Thread.currentThread().getId()));

        SDKContext sdkContext = SDKContextFactory.create(MobeaconService.APP_CONTEXT, appKey);

        mobeaconRestApi.init(appKey, sdkContext).subscribe(new Action1<SDKConf>() {
            @Override
            public void call(SDKConf sdkConf) {
                MobeaconService.startActionConfig(MobeaconService.APP_CONTEXT, appKey, sdkConf);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                Log.e(TAG, String.format("SDK initialization failed: %s. Thread id %s ", e.getLocalizedMessage(), Thread.currentThread().getId()));
            }
        });

    }
    protected synchronized void configureSDK(String appKey, SDKConf sdkConf) {
        Log.i(TAG, String.format("SDK initialization completed. Config: isEnabled=%s. Thread id %s ", sdkConf.isEnabled(), Thread.currentThread().getId()));
        if (sdkConf.isEnabled()) {
            LocationMonitor.init(appKey, mobeaconRestApi);

            if(sdkConf.isGeofenceEnabled()) {
                LocationMonitor.instance().enableGeofencingNotifications(new NotificationSender((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)));
                Log.i(TAG, "Geofence notifications enabled.");
            }
            else {
                Log.i(TAG, "Geofence disabled according to configuration.");
            }
            if (sdkConf.isBeaconsEnabled()) {
                beaconManager = BeaconManager.getInstanceForApplication(this);
                //set layput for estimote/aprilBrothers
                beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
//                beaconManager.bind(this);
            }
            else {
                Log.i(TAG, "Beacons disabled according to configuration.");
            }
        }
        else {
            Log.i(TAG, "SDK disabled according to configuration.");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, " === destroying service ===");
        if (beaconManager != null) {
//            beaconManager.unbind(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, " === creating service ===");
        APP_CONTEXT = getApplicationContext();
        mobeaconRestApi = MobeaconRestApiFactory.create();
        Log.i(TAG, "Network location is "+ (LocationUtils.isNetLocEnabled(this) ? "enabled": "disabled"));
        Log.i(TAG, "WiFi is "+ (LocationUtils.isWiFiEnabled(this) ? "enabled": "disabled"));
        Log.i(TAG, "AirplaneMode is " + (LocationUtils.isAirplaneModeEnabled(this) ? "enabled" : "disabled"));
    }

//    @Override
//    public void onBeaconServiceConnect() {
//        Log.i(TAG, "Beacon service connected");
//        beaconManager.setMonitorNotifier(new MonitorNotifier() {
//            @Override
//            public void didEnterRegion(Region region) {
//                region.getUniqueId();
//                Log.i(TAG, "I just saw an beacon for the first time!");
//            }
//
//            @Override
//            public void didExitRegion(Region region) {
//                Log.i(TAG, "I no longer see an beacon");
//            }
//
//            @Override
//            public void didDetermineStateForRegion(int state, Region region) {
//                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
//            }
//        });
//
//        try {
//            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
//            Log.i(TAG, "Beacons monitoring is started");
//
//        } catch (RemoteException e) {    }
//    }

}
