package io.mobeacon.sdk.nearby;

/**
 * Created by maxulan on 04.08.15.
 */
public interface INearbyLocationsPublisher {
    void subscribe(INearbyLocationsListener listener);
    void unsubscribe(INearbyLocationsListener listener);
}
