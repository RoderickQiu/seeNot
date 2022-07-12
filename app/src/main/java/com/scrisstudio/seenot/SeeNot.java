package com.scrisstudio.seenot;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

public class SeeNot extends Application {

    public static final Void voided = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static String lastTimeDestination = "";

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

    public static final String TAG = "SeeNot-AccessibilityService";

    //log
    public static void l(Object input) {
        if (input != null)
            Log.w(TAG, input.toString() + " " + System.currentTimeMillis());
        else Log.w(TAG, "NULL" + " " + System.currentTimeMillis());
    }

    //log-error
    public static void le(Object input) {
        if (input != null)
            Log.e(TAG, input.toString() + " " + System.currentTimeMillis());
        else Log.e(TAG, "NULL" + " " + System.currentTimeMillis());
    }

    public static String getManufacturer() {
        String brand = android.os.Build.BRAND.toLowerCase();
        switch (brand) {
            case "xiaomi":
                return "miui";
            case "huawei":
            case "honor":
                return "emui";
            case "oppo":
            case "oneplus":
            case "realme":
                return "coloros";
            case "vivo":
            case "iqoo":
                return "funtouchos";
            default:
                return "other";
        }
    }
}
