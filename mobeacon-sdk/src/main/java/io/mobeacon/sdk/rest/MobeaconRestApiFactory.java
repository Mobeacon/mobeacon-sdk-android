package io.mobeacon.sdk.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import io.mobeacon.sdk.util.NetworkUtils;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by maxulan on 20.07.15.
 */
public class MobeaconRestApiFactory {
    private static final String BASE_URL = "http://192.168.0.103:8080";
//    private static final String BASE_URL = "http://10.0.2.2:8080";


    public static MobeaconRestApi create()
    {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(BASE_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        return new MultithreadedMobeaconRestApi(restAdapter.create(MobeaconRestApi.class));
    }

    private static class MultithreadedMobeaconRestApi implements MobeaconRestApi{
        private static final String TAG = "MobeaconRestApi";
        public static final long DEFAULT_CALL_ATTEMPT_INTERVAL_SEC = 5;

        private MobeaconRestApi api;
        private Scheduler networkScheduler;

        public MultithreadedMobeaconRestApi(MobeaconRestApi mobeaconRestApi) {
            this.api = mobeaconRestApi;
            this.networkScheduler = Schedulers.io();
        }

        @Override
        public Observable<SDKConf> init(String appKey, SDKContext ctx) {
            return wrap(api.init(appKey, ctx));
        }

        @Override
        public Observable<List<Location>> getNearestLocations(String appKey, double latitude, double longitude, float radius, int limit) {
            return wrap(api.getNearestLocations(appKey, latitude, longitude, radius, limit));
        }

        private <T> Observable<T> wrap(Observable<T> observable) {
            return observable.observeOn(networkScheduler).subscribeOn(networkScheduler).retry(new Func2<Integer, Throwable, Boolean>() {

                @Override
                public Boolean call(Integer attempts, Throwable throwable) {
                    Log.e(TAG, "REST API call failed");
                    if (throwable != null) {
                        Log.e(TAG, String.format("REST API call failed: %s %s caused by %s. Thread id %s", throwable.getClass(), throwable.getLocalizedMessage(), throwable.getCause()==null?"null":throwable.getCause().getClass(), Thread.currentThread().getId()));
                        if (throwable.getCause() instanceof ConnectException) {
                            Log.e(TAG, "Failed to connect to REST API: " + throwable.getLocalizedMessage());
                            return waitAndRetry();
                        }
                    }
                    if (attempts != null && attempts < 3) {
                        return waitAndRetry();
                    }
                    return false;
                }

                private boolean waitAndRetry() {
                    try {
                        Log.e(TAG, String.format("Waiting for %s sec and then retry...", DEFAULT_CALL_ATTEMPT_INTERVAL_SEC));
                        TimeUnit.SECONDS.sleep(DEFAULT_CALL_ATTEMPT_INTERVAL_SEC);
                        try {
                            while (!NetworkUtils.isOnline()) {
                                TimeUnit.SECONDS.sleep(DEFAULT_CALL_ATTEMPT_INTERVAL_SEC);
                            }
                        } catch (Exception e) {
                            return true;
                        }
                        return true;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });
        }
    }
}
