package com.scrisstudio.seenot;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class SeeNot extends Application {

    public static final Void voided = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getAppContext() {
        return SeeNot.context;
    }

    public static boolean isDebugApp() {
        try {
            ApplicationInfo info = SeeNot.context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception x) {
            return false;
        }
    }

    public void onCreate() {
        super.onCreate();
        SeeNot.context = getApplicationContext();
    }
}
