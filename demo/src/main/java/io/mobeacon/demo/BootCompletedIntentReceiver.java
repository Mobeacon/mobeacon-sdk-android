package io.mobeacon.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.mobeacon.sdk.services.MobeaconService;

/**
 * Created by maxulan on 03.08.15.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            MobeaconService.start(context, MainActivity.MOBEACON_APP_KEY);
        }
    }
}
