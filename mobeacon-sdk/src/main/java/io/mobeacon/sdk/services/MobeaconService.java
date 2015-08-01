package io.mobeacon.sdk.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

import io.mobeacon.sdk.GeofenceErrorMessages;
import io.mobeacon.sdk.LocationMonitor;
import io.mobeacon.sdk.R;
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
public class MobeaconService extends IntentService implements BeaconConsumer {
    public static Context APP_CONTEXT;

    private static final String TAG = "MobeaconService";

    // IntentService can perform, e.g. ACTION_INIT
    public static final String ACTION_INIT = "io.mobeacon.sdk.services.action.INIT";
    public static final String ACTION_GEOFENCE = "io.mobeacon.sdk.services.action.GEOFENCE";

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
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MobeaconService.class);
        intent.setAction(ACTION_GEOFENCE);
        intent.putExtra(EXTRA_APP_KEY, param1);
        intent.putExtra(EXTRA_GOOGLE_ID, param2);
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
            } else if (ACTION_GEOFENCE.equals(action)) {
                GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
                handleActionGeofenceDiscovery(geofencingEvent);
            }
        }
    }

    /**
     * Handle SDK init action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInit(final String appKey, final String googleProjectId) {
        Log.i(TAG, String.format("Handle init action. Thread id %s ", Thread.currentThread().getId()));

        SDKContext sdkContext = SDKContextFactory.create(MobeaconService.this, appKey);


        mobeaconRestApi.init(appKey, sdkContext).subscribe(new Action1<SDKConf>() {
            @Override
            public void call(SDKConf sdkConf) {
                completeSdkInit(appKey, googleProjectId, sdkConf);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                Log.e(TAG, String.format("SDK initialization failed: %s. Thread id %s ", e.getLocalizedMessage(), Thread.currentThread().getId()));
            }
        });

    }
    protected synchronized void completeSdkInit(String appKey, String googleProjectId, SDKConf sdkConf) {
        Log.i(TAG, String.format("SDK initialization completed. Config: isEnabled=%s. Thread id %s ", sdkConf.isEnabled(), Thread.currentThread().getId()));
        if (sdkConf.isEnabled()) {
            LocationMonitor locationManager = new LocationMonitor(appKey, mobeaconRestApi);
            beaconManager = BeaconManager.getInstanceForApplication(this);
            //set layput for estimote/aprilBrothers
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconManager.bind(this);
        }
        else {
            Log.i(TAG, "SDK disabled according to configuration.");
        }
    }
    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGeofenceDiscovery(GeofencingEvent geofencingEvent) {

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "geofence_transition_invalid_type: " + geofenceTransition);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) {
            beaconManager.unbind(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        APP_CONTEXT = getApplicationContext();
        mobeaconRestApi = MobeaconRestApiFactory.create();
        Log.i(TAG, "Network location is "+ (LocationUtils.isNetLocEnabled(this) ? "enabled": "disabled"));
        Log.i(TAG, "WiFi is "+ (LocationUtils.isWiFiEnabled(this) ? "enabled": "disabled"));
        Log.i(TAG, "AirplaneMode is " + (LocationUtils.isAirplaneModeEnabled(this) ? "enabled" : "disabled"));
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "geofence transition entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "geofence transition exited";
            default:
                return "unknown geofence transition";
        }
    }

    private void sendNotification(String text){
        Notification notification = new NotificationCompat.Builder(MobeaconService.APP_CONTEXT).
                setSmallIcon(R.drawable.notification_template_icon_bg).
                setContentTitle("Test geofence notification").
                setContentText(text).
                build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);
    }
    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "Beacon service connected");
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                region.getUniqueId();
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
            Log.i(TAG, "Beacons monitoring is started");

        } catch (RemoteException e) {    }
    }

}
