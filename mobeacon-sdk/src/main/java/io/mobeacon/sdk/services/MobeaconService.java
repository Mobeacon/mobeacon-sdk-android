package io.mobeacon.sdk.services;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import io.mobeacon.sdk.MobeaconSDK;
import io.mobeacon.sdk.NotificationCenter;
import io.mobeacon.sdk.beacon.BeaconMonitor;
import io.mobeacon.sdk.geofence.GeofenceMonitor;
import io.mobeacon.sdk.nearby.NearbyLocationsLoader;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import io.mobeacon.sdk.model.SDKContextFactory;
import io.mobeacon.sdk.rest.MobeaconRestApi;
import io.mobeacon.sdk.rest.MobeaconRestApiFactory;
import io.mobeacon.sdk.util.DateTimeUtils;
import io.mobeacon.sdk.util.LocationUtils;
import rx.functions.Action1;

/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class MobeaconService extends Service implements BeaconConsumer {
    public static Context APP_CONTEXT;

    private static final String TAG = "MobeaconService";

    // Intents Service can perform, e.g. ACTION_START_SERVICE
    public static final String ACTION_START_SERVICE = "io.mobeacon.sdk.services.action.START_SDK";
    public static final String ACTION_GEOFENCE = "io.mobeacon.sdk.services.action.GEOFENCE";

    private static final String EXTRA_APP_KEY = "io.mobeacon.sdk.services.extra.APP_KEY";

    /**
     * Starts this service to with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     */
    public static void start(Context context, String appKey) {
        Intent intent = new Intent(context, MobeaconService.class);
        intent.setAction(ACTION_START_SERVICE);
        intent.putExtra(EXTRA_APP_KEY, appKey);
        context.startService(intent);
    }
    private MobeaconServiceHandler mServiceHandler;
    private Looper mMainThreadLooper;

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
    private final class MobeaconServiceHandler extends Handler{
        public static final int MSG_CODE_START_SDK = 1;
        public static final int MSG_CODE_GEOFENCE_TRANSITION_EVENT = 2;
        public static final int MSG_CODE_START_BEACON_SCANNER = 3;

        public static final String MSG_DATA_GEOFENCE_INTENT = "GEOFENCE_INTENT";
        public static final String MSG_DATA_APP_KEY = "APP_KEY";

        private MobeaconRestApi mMobeaconRestApi;
        private NearbyLocationsLoader mLocationsLoader;
        private BeaconManager mBeaconManager;
        private BeaconMonitor mBeaconMonitor;
        private NotificationCenter mNotificationCenter;
        private GeofenceMonitor mGeofenceMonitor;
        private boolean mStarted = false;

        public MobeaconServiceHandler(Looper looper) {
            super(looper);
            mMobeaconRestApi = MobeaconRestApiFactory.create();
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle data;
            switch (msg.what) {
                case MSG_CODE_START_SDK:
                    mStarted = true;
                    data = msg.getData();
                    if (data!=null) {
                        startSdkConfigurationForApp(data.getString(MSG_DATA_APP_KEY));
                    }
                case MSG_CODE_GEOFENCE_TRANSITION_EVENT:
                    if(mGeofenceMonitor != null) {
                        data = msg.getData();
                        if (data!=null) {
                            Intent geofenceIntent = (Intent) data.getParcelable(MSG_DATA_GEOFENCE_INTENT);
                            if (geofenceIntent !=null) {
                                mGeofenceMonitor.processGeofenceIntent(geofenceIntent);
                            }
                        }

                    } else {
                        Log.e(TAG, String.format("Failed to handle geofence intent:  mGeofenceMonitor=null. Thread id %s.", Thread.currentThread().getId()));
                    }
                default:

            }
        }

        public void stop() {
            if (mBeaconManager != null) {
                mBeaconManager.unbind(MobeaconService.this);
            }
        }

        public boolean isStarted() {
            return mStarted;
        }

        public void startMonitorBeacons() {
            if (mBeaconManager != null && mBeaconMonitor != null) {
                mBeaconManager.setMonitorNotifier(mBeaconMonitor);
            }
        }

        public void sendSdkInitMsg(String appKey) {
            if(appKey != null) {
                Message msg = obtainMessage();
                msg.what = MobeaconServiceHandler.MSG_CODE_START_SDK;
                Bundle b = new Bundle(1);
                b.putString(MSG_DATA_APP_KEY, appKey);
                msg.setData(b);
                sendMessage(msg);
            }
        }

        public void sendGeofencingEventMsg(Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent!= null) {
                Message msg = mServiceHandler.obtainMessage();
                msg.what = MobeaconServiceHandler.MSG_CODE_GEOFENCE_TRANSITION_EVENT;
                Bundle b = new Bundle(1);
                b.putParcelable(MobeaconServiceHandler.MSG_DATA_GEOFENCE_INTENT, intent);
                msg.setData(b);
                mServiceHandler.sendMessage(msg);
            }
        }

        private void startSdkConfigurationForApp(final String appKey) {
            Log.i(TAG, String.format("Getting SDK config for App by key %s. Thread id %s ", appKey, Thread.currentThread().getId()));

            SDKContext sdkContext = SDKContextFactory.create(getApplicationContext(), appKey);

            mMobeaconRestApi.init(appKey, sdkContext).subscribe(new Action1<SDKConf>() {
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
                mLocationsLoader = new NearbyLocationsLoader(getApplicationContext(), appKey, mMobeaconRestApi);
                mNotificationCenter = new NotificationCenter(getApplicationContext(), sdkConf);

                if(sdkConf.isGeofenceEnabled()) {
                    mGeofenceMonitor = new GeofenceMonitor(getApplicationContext());
                    mLocationsLoader.subscribe(mGeofenceMonitor);
                    mGeofenceMonitor.subscribe(mNotificationCenter);
                    Log.i(TAG, "Geofence notifications enabled.");
                    if (sdkConf.isBeaconsEnabled()) {
                        mBeaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
                        //set layput for estimote/aprilBrothers
                        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
                        mBeaconManager.bind(MobeaconService.this);
                        mBeaconMonitor = new BeaconMonitor(sdkConf, mBeaconManager);
                        mBeaconMonitor.subscribe(mNotificationCenter);
                        mGeofenceMonitor.subscribe(mBeaconMonitor);
                    }
                    else {
                        Log.i(TAG, "Beacons disabled according to configuration.");
                    }
                }
                else {
                    Log.i(TAG, "Geofence and beacons monitoring disabled according to configuration.");
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

            if (ACTION_START_SERVICE.equals(action)) {
                final String appKey = intent.getStringExtra(EXTRA_APP_KEY);
                if(appKey != null){
                    saveAppKeyPreference(appKey);
                    mServiceHandler.sendSdkInitMsg(appKey);
                }
            }
            else if (ACTION_GEOFENCE.equals(action)) {
                Log.i(TAG, String.format("Handle geofence intent action %s ", action));
                if(mServiceHandler.isStarted()) {
                    mServiceHandler.sendGeofencingEventMsg(intent);
                }
                else {
                    String appKey = retrieveAppKeyPreference();
                    if (appKey != null) {
                        Log.w(TAG, "Service handler is not started. Skipping geofence intent and trying to restart handler.");
                        mServiceHandler.sendSdkInitMsg(appKey);
                    }
                    else {
                        Log.w(TAG, "Failed to process geofence intent: service handler is not started.");
                    }
                }
            }
        }
    }

    private void saveAppKeyPreference(String appKey) {
        if (appKey != null) {
            SharedPreferences settings = getApplicationContext().getSharedPreferences(MobeaconSDK.PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(MobeaconSDK.PREF_KEY_APP_KEY, appKey);
            // Commit the edits!
            editor.commit();
        }
    }

    private String retrieveAppKeyPreference() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MobeaconSDK.PREFERENCES, 0);
        return settings.getString(MobeaconSDK.PREF_KEY_APP_KEY, null);
    }
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
        mServiceHandler.stop();
        mMainThreadLooper.quitSafely();
        super.onDestroy();
    }
    //    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "Beacon service connected");
        mServiceHandler.startMonitorBeacons();
    }

}
