package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.Utils.l;
import static com.scrisstudio.seenot.Utils.le;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class ExecutorService extends AccessibilityService {
    public static final String TAG = "SeeNot-AccessibilityService";
    public static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
    public static final int KEEPER_NOTIFICATION_ID = 408, NORMAL_NOTIFICATION_ID = 488;
    public static ExecutorService mService;

    public static boolean isStart() {
        return mService != null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        l("Service starting...");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {
        le("Service interrupt");
        mService = null;
    }

    @Override
    public void onDestroy() {
        le("Service destroy");
        mService = null;
        super.onDestroy();
    }
}
