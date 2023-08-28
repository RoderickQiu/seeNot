package com.scrisstudio.seenot;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.scrisstudio.seenot.service.ApplicationObserver;

public class SeeNot extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static String lastTimeDestination = "";
    public static int shouldNavigateTo = 0;

    private static String locale;

    public static Context getAppContext() {
        return SeeNot.context;
    }

    public static String getLocale() {
        return locale;
    }

    public static boolean isDebugApp() {
        try {
            ApplicationInfo info = SeeNot.context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception x) {
            return false;
        }
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLocale(getResources());
    }

    public void onCreate() {
        super.onCreate();
        SeeNot.context = getApplicationContext();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationObserver());
    }

    public static void setLocale(Resources resources) {
        locale = resources.getConfiguration().getLocales().get(0).getLanguage();
        le(locale);
        if (!locale.equals("zh")) {
            locale = "en";
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags("en-US");
            AppCompatDelegate.setApplicationLocales(appLocale);
        } else {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags("zh-CN");
            AppCompatDelegate.setApplicationLocales(appLocale);
        }
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

    public static int typesCnt = 9;

    public static String getFilterTypeName(int type) {
        switch (type) {
            case 0:
                return locale.equals("zh") ? "禁止进入此程序" : "Forbid access to app";//R.string.filter_ban_app;
            case 1:
                return locale.equals("zh") ? "禁止进入此页面" : "Forbid access to the page";//R.string.filter_ban_activity;
            case 2:
                return locale.equals("zh") ? "找到文字后退出" : "Find text and back";//R.string.filter_ban_text;
            case 5:
                return locale.equals("zh") ? "找到文字后自动点击" : "Find text and click";//R.string.filter_auto_click_text;
            case 6:
                return locale.equals("zh") ? "找到文字后反向划动" : "Find text and swipe backwards";//R.string.filter_swipe_text;
            case 3:
                return locale.equals("zh") ? "找到元素ID后退出" : "Find elem. id and back";//R.string.filter_ban_id;
            case 4:
                return locale.equals("zh") ? "找到元素ID后自动点击" : "Find elem. id and click";//R.string.filter_auto_click_id;
            case 7:
                return locale.equals("zh") ? "找到元素ID后反向划动" : "Find elem. ID and swipe backwards";//R.string.filter_swipe_id;
            case 8:
                return locale.equals("zh") ? "找到指定坐标元素后退出" : "Find elem. coord. and back";//R.string.filter_ban_coor;
            case 9:
                return locale.equals("zh") ? "找到指定坐标元素后自动点击" : "Find elem. coord. and click";//R.string.filter_auto_click_coor;
        }
        return "未知规则";//R.string.type_not_found;
    }
}
