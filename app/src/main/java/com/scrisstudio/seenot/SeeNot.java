package com.scrisstudio.seenot;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.scrisstudio.seenot.service.ApplicationObserver;

public class SeeNot extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static String lastTimeDestination = "";
    public static int shouldNavigateTo = 0;

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
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationObserver());
    }

    public static final String TAG = "SeeNot-AccessibilityService";

    //log
    public static void l(Object input) {
        if (input != null)
            Log.w(TAG, input + " " + System.currentTimeMillis());
        else Log.w(TAG, "NULL" + " " + System.currentTimeMillis());
    }

    //log-error
    public static void le(Object input) {
        if (input != null)
            Log.e(TAG, input + " " + System.currentTimeMillis());
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

    public static int typesCnt = 7;

    public static String getFilterTypeName(int type) {
        switch (type) {
            case 0:
                return "禁止进入此程序";//R.string.filter_ban_app;
            case 1:
                return "禁止进入此页面";//R.string.filter_ban_activity;
            case 2:
                return "找到文字后退出";//R.string.filter_ban_text;
            case 5:
                return "找到文字后自动点击";//R.string.filter_auto_click_text;
            case 6:
                return "找到文字后反向划动";//R.string.filter_swipe_text;
            case 3:
                return "找到元素后退出";//R.string.filter_ban_id;
            case 4:
                return "找到元素后自动点击";//R.string.filter_auto_click_id;
            case 7:
                return "找到元素后反向划动";//R.string.filter_swipe_id;
        }
        return "未知规则";//R.string.type_not_found;
    }
}
