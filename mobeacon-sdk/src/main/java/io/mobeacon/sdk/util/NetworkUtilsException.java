package io.mobeacon.sdk.util;

/**
 * Created by maxulan on 24.07.15.
 */
public class NetworkUtilsException extends Exception {
    public NetworkUtilsException() {
    }

    public NetworkUtilsException(String detailMessage) {
        super(detailMessage);
    }

    public NetworkUtilsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NetworkUtilsException(Throwable throwable) {
        super(throwable);
    }
}
