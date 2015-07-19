package io.mobeacon.sdk.rest;

import java.util.List;

import io.mobeacon.sdk.model.Location;
import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by maxulan on 19.07.15.
 */
public interface NearestLocationsService {
    @GET("/app/{appKey}/geo")
    public List<Location> near(@Path("appKey") String appKey, @Query("lat")double latitude, @Query("lon")double longitude, @Query("rad")double radius, @Query("n")int limit);
}
