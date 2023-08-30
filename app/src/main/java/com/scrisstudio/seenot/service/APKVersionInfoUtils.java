package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.getLocale;
import static com.scrisstudio.seenot.SeeNot.le;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.struct.FetcherInfo;

import java.util.ArrayList;
import java.util.Date;

import io.github.g00fy2.versioncompare.Version;

/**
 * 获取当前APK的版本号和版本名
 */
public class APKVersionInfoUtils {

    /**
     * 获取当前apk的版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取当前apk的版本名
     *
     * @param context 上下文
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionName
            versionName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static void openVersionDialog(Context context, FetcherInfo fetched, Resources resources, SharedPreferences sharedPreferences, boolean isAuto) {
        if (!isAuto) Toast.makeText(context,
                resources.getString(R.string.update_check_in_progress), Toast.LENGTH_LONG).show();
        if (!sharedPreferences.getBoolean("auto-check-update", true) && isAuto)
            return;//when ban auto update check
        long time = new Date().getTime();
        if (!isAuto || (time - sharedPreferences.getLong("last-update-check", 0) > 86400000)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("last-update-check", time);
            editor.apply();

            if (new Version(fetched.getVersion()) //version compare
                    .isHigherThan(APKVersionInfoUtils.getVersionName(context))) {
                ArrayList<String> updaterMsg = fetched.getUpdater();
                StringBuilder stringBuilder = new StringBuilder();
                int cnt = 1;
                for (String s : updaterMsg) {
                    stringBuilder.append((cnt++));
                    stringBuilder.append(". ").append(s).append(" ");
                }
                try {
                    new MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.update_title)
                            .setMessage(resources.getString(R.string.update_msg) + stringBuilder)
                            .setPositiveButton(R.string.update_now, (dialogInterface, i) -> {
                                Uri uri = Uri.parse("https://seenot.r-q.name/" + getLocale() + "/?download=true");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                context.startActivity(intent);
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();
                } catch (Exception e) {
                    le("Update dialog ERR: " + e);
                }
            } else {
                if (!isAuto) Toast.makeText(context,
                        resources.getString(R.string.no_update_cur), Toast.LENGTH_LONG).show();
            }
        }
    }

}