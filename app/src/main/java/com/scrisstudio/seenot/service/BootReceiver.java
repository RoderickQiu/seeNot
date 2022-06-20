package com.scrisstudio.seenot.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //software auto start
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) {
            Intent serviceIntent = new Intent(context, ExecutorService.class);
            context.startService(serviceIntent);
            Toast.makeText(context, "seeNot is launching ...", Toast.LENGTH_LONG).show();
        }
    }

}