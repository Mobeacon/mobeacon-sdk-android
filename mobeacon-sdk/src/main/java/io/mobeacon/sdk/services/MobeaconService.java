package io.mobeacon.sdk.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.mobeacon.sdk.LocationManager;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import io.mobeacon.sdk.model.SDKContextFactory;
import io.mobeacon.sdk.rest.MobeaconRestApi;
import io.mobeacon.sdk.rest.RestClient;
import io.mobeacon.sdk.util.NetworkUtils;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MobeaconService extends IntentService {
    private static final String TAG = "MobeaconService";


    // IntentService can perform, e.g. ACTION_INIT
    private static final String ACTION_INIT = "io.mobeacon.sdk.services.action.INIT";
    private static final String ACTION_BAZ = "io.mobeacon.sdk.services.action.BAZ";

    private static final String EXTRA_APP_KEY = "io.mobeacon.sdk.services.extra.APP_KEY";
    private static final String EXTRA_GOOGLE_ID = "io.mobeacon.sdk.services.extra.GOOGLE_ID";

    private Scheduler networkScheduler = Schedulers.io();

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionInit(Context context, String appKey, String googleId) {
        Intent intent = new Intent(context, MobeaconService.class);
        intent.setAction(ACTION_INIT);
        intent.putExtra(EXTRA_APP_KEY, appKey);
        intent.putExtra(EXTRA_GOOGLE_ID, googleId);
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
        intent.setAction(ACTION_BAZ);
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
            if (ACTION_INIT.equals(action)) {
                final String appKey = intent.getStringExtra(EXTRA_APP_KEY);
                final String googleId = intent.getStringExtra(EXTRA_GOOGLE_ID);
                handleActionInit(appKey, googleId);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_APP_KEY);
                final String param2 = intent.getStringExtra(EXTRA_GOOGLE_ID);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle SDK init action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInit(final String appKey, final String googleId) {
        Log.i(TAG, String.format("Handle init action. Thread id %s ", Thread.currentThread().getId()));

        SDKContext sdkContext = SDKContextFactory.create(MobeaconService.this, appKey);
        MobeaconRestApi api = new RestClient().getMobeaconRestApi();
        final Context context = MobeaconService.this.getApplicationContext();
        api.init(appKey, sdkContext).observeOn(networkScheduler).subscribeOn(networkScheduler).retry(new Func2<Integer, Throwable, Boolean>() {
            public static final long DEFAULT_CALL_ATTEMPT_INTERVAL_SEC = 5;


            @Override
            public Boolean call(Integer attempts, Throwable throwable) {
                Log.e(TAG, String.format("Failed to execute REST API call: %s %s caused by %s. Thread id %s", throwable.getClass(), throwable.getLocalizedMessage(), throwable.getCause().getClass(), Thread.currentThread().getId()));

                if (throwable.getCause() instanceof ConnectException) {
                    Log.e(TAG, "Failed to connect to REST API: " + throwable.getLocalizedMessage());
                    return waitAndRetry();
                } else if (attempts < 3) {
                    return waitAndRetry();
                }
                return false;
            }

            private boolean waitAndRetry() {
                try {
                    Log.e(TAG, String.format("Waiting for %s sec and then retry...", DEFAULT_CALL_ATTEMPT_INTERVAL_SEC));
                    TimeUnit.SECONDS.sleep(DEFAULT_CALL_ATTEMPT_INTERVAL_SEC);
                    while (!NetworkUtils.isOnline(context)) {
                        TimeUnit.SECONDS.sleep(DEFAULT_CALL_ATTEMPT_INTERVAL_SEC);
                    }
                    return true;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }

        }).subscribe(new Action1<SDKConf>() {
            @Override
            public void call(SDKConf sdkConf) {
                //TODO init sdk notification service
                Log.i(TAG, String.format("SDK initialization completed. Config: isEnabled=%s. Thread id %s ", sdkConf.isEnabled(), Thread.currentThread().getId()));
                LocationManager locationManager = new LocationManager(context);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                 Log.e(TAG, String.format("SDK initialization failed: %s. Thread id %s ", e.getLocalizedMessage(), Thread.currentThread().getId()));
            }
        });

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
