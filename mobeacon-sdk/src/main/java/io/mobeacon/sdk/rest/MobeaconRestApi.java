package io.mobeacon.sdk.rest;

import java.util.List;

import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by maxulan on 19.07.15.
 */
public interface MobeaconRestApi {
    @POST("/app/{appKey}/init")
    Observable<SDKConf> init(@Path("appKey") String appKey, @Body SDKContext ctx);

    @GET("/app/{appKey}/geo")
    Observable<List<Location>> getNearestLocations(@Path("appKey") String appKey, @Query("lat") double latitude, @Query("lon") double longitude, @Query("rad") double radius, @Query("n") int limit);
}
