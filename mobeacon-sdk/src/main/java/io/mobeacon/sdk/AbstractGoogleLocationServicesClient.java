package io.mobeacon.sdk;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 04.08.15.
 */
public abstract class AbstractGoogleLocationServicesClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleLocationApiClient";

    protected GoogleApiClient mGoogleApiClient;

    protected AbstractGoogleLocationServicesClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(TAG, "Google play services connection established");
    }

    @Override
    public void onConnectionSuspended(int var1) {
        Log.e(TAG, "Google play services connection suspended");
    }

    public void onConnectionFailed(ConnectionResult var1) {
        Log.e(TAG, "Google play services connection failed: " + var1.toString());
    }
}
