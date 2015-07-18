package io.mobeacon.sdk;

/**
 * Created by maxulan on 15.07.15.
 */
public class MobeaconSDK {
    private static MobeaconSDK instance;

    private MobeaconSDK() {

    }

    public static MobeaconSDK getInstance() {
        if (instance == null)
        {
            instance =  new MobeaconSDK();
        }
        return instance;
    }
}
