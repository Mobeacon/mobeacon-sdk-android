package io.mobeacon.sdk.rest;

import io.mobeacon.sdk.model.SDKConf;
import io.mobeacon.sdk.model.SDKContext;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by maxulan on 19.07.15.
 */
public interface SDKInitializationService {
    @POST("/app/{appKey}/init")
    public SDKConf init(@Path("appKey") String appKey, @Body SDKContext ctx);
}

