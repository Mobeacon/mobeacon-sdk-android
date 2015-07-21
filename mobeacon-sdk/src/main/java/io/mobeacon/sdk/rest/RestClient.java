package io.mobeacon.sdk.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by maxulan on 20.07.15.
 */
public class RestClient {
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private MobeaconRestAPI mobeaconRestAPI;

    public RestClient()
    {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(BASE_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        mobeaconRestAPI = restAdapter.create(MobeaconRestAPI.class);

    }

    public MobeaconRestAPI getMobeaconRestAPI()
    {
        return mobeaconRestAPI;
    }
}
