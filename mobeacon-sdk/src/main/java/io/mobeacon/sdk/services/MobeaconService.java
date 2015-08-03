package io.mobeacon.sdk.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.*;
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
public class MobeaconService extends Service{//} implements BeaconConsumer {
    public static Context APP_CONTEXT;

    private static final String TAG = "MobeaconService";

    // IntentService can perform, e.g. ACTION_INIT
    public static final String ACTION_INIT = "io.mobeacon.sdk.services.action.INIT";
    public static final String ACTION_GEOFENCE = "io.mobeacon.sdk.services.action.GEOFENCE";

    private static final String EXTRA_APP_KEY = "io.mobeacon.sdk.services.extra.APP_KEY";

    /**
     * Starts this service to with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void start(Context context, String appKey) {
        Intent intent = new Intent(context, MobeaconService.class);
        intent.setAction(ACTION_INIT);
        intent.putExtra(EXTRA_APP_KEY, appKey);
        context.startService(intent);
    }
    private MobeaconServiceHandler mServiceHandler;

    public MobeaconService() {
        super();
    }

    /**
     * A service message handler.
     *
     * All the high level logic goes here
     * e.g. initialization of geofence and beacon scanners, dispatching of geofence transition events, etc.
     *
     */
    private final class MobeaconServiceHandler extends Handler {
        public static final int MSG_CODE_INIT = 1;
        public static final int MSG_CODE_GEOFENCE = 2;

        public static final String MSG_KEY_GEOFENCE_INTENT = "GEOFENCE_INTENT";

        private MobeaconRestApi mobeaconRestApi;
        private BeaconManager beaconManager;

        public MobeaconServiceHandler(Looper looper) {
            super(looper);
            mobeaconRestApi = MobeaconRestApiFactory.create();
        }
        @Override
        public void handleMessage(Message msg) {
            Bundle data;
            switch (msg.what) {
                case MSG_CODE_INIT:
                    data = msg.getData();
                    if (data!=null) {
                        getSdkConfigForApp(data.getString(EXTRA_APP_KEY));
                    }
                case MSG_CODE_GEOFENCE:
                    if(LocationMonitor.instance().getGeofencingManager() != null) {
                        data = msg.getData();
                        if (data!=null) {
                            Intent geofenceIntent = (Intent)data.getParcelable(MSG_KEY_GEOFENCE_INTENT);
                            if (geofenceIntent !=null) {
                                GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(geofenceIntent);
                                LocationMonitor.instance().getGeofencingManager().processGeofencingEvent(geofencingEvent);
                            }
                        }

                    } else {
                        Log.e(TAG, String.format("Failed to handle geofence intent:  LocationMonitor.instance().getGeofencingManager()=%s", LocationMonitor.instance().getGeofencingManager()));
                    }
                default:

            }
        }

        private void getSdkConfigForApp(final String appKey) {
            Log.i(TAG, String.format("Getting SDK config for App by key %s. Thread id %s ", appKey, Thread.currentThread().getId()));

            SDKContext sdkContext = SDKContextFactory.create(MobeaconService.APP_CONTEXT, appKey);

            mobeaconRestApi.init(appKey, sdkContext).subscribe(new Action1<SDKConf>() {
                @Override
                public void call(SDKConf sdkConf) {
                    Log.i(TAG, String.format("SDK config received successfully. Thread id %s ", Thread.currentThread().getId()));
                    configureSDK(appKey, sdkConf);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable e) {
                    Log.e(TAG, String.format("Failed to get SDK configuration for App by key %s. Thread id %s ", appKey, e.getLocalizedMessage(), Thread.currentThread().getId()));
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
                    beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
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
    }

    private void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(TAG, String.format("Handle intent action %s ", action));

            if (ACTION_INIT.equals(action)) {
                final String appKey = intent.getStringExtra(EXTRA_APP_KEY);
                if(appKey != null){
                    // For each start request, send a message to start a job and deliver the
                    // start ID so we know which request we're stopping when we finish the job
                    Message msg = mServiceHandler.obtainMessage();
                    msg.what = MobeaconServiceHandler.MSG_CODE_INIT;
                    Bundle b = new Bundle(1);
                    b.putString(EXTRA_APP_KEY, appKey);
                    msg.setData(b);
                    mServiceHandler.sendMessage(msg);
                }
            }
            else if (ACTION_GEOFENCE.equals(action)) {
                Log.i(TAG, String.format("Handle geofence intent action %s ", action));
                GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
                if (geofencingEvent!= null) {
                    Message msg = mServiceHandler.obtainMessage();
                    msg.what = MobeaconServiceHandler.MSG_CODE_GEOFENCE;
                    Bundle b = new Bundle(1);
                    b.putParcelable(MobeaconServiceHandler.MSG_KEY_GEOFENCE_INTENT, intent);
                    msg.setData(b);
                    mServiceHandler.sendMessage(msg);
                }
            }
        }
    }
    private Looper mMainThreadLooper;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, " === creating service ===");
        APP_CONTEXT = getApplicationContext();

        Log.i(TAG, "Network location is "+ (LocationUtils.isNetLocEnabled(this) ? "enabled": "disabled"));
        Log.i(TAG, "WiFi is " + (LocationUtils.isWiFiEnabled(this) ? "enabled" : "disabled"));
        Log.i(TAG, "AirplaneMode is " + (LocationUtils.isAirplaneModeEnabled(this) ? "enabled" : "disabled"));
        HandlerThread thread = new HandlerThread("MobeaconServiceMainThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mMainThreadLooper = thread.getLooper();
        mServiceHandler = new MobeaconServiceHandler(mMainThreadLooper);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " === destroying service ===");
        mMainThreadLooper.quitSafely();
        super.onDestroy();

//        if (beaconManager != null) {
//            beaconManager.unbind(this);
//        }
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
