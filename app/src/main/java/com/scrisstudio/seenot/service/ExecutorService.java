package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;

import java.util.ArrayList;

public class ExecutorService extends AccessibilityService {
    public static final String TAG = "SeeNot-AccessibilityService";
    public static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
    public static final int KEEPER_NOTIFICATION_ID = 408, NORMAL_NOTIFICATION_ID = 488;
    public static ExecutorService mService;
    public static Boolean isServiceRunning = false;
    private static ArrayList<RuleInfo> rulesList;
    private static Boolean isSplitScreenAcceptable = true, isForegroundServiceRunning = false, isFirstTimeInvokeService = true;
    private String currentHomePackage;
    private SharedPreferences sharedPreferences;
    public static NotificationManager normalNotificationManager;
    private final Handler mHandler = new Handler();
    public NotificationChannel normalNotificationChannel;

    public static boolean isStart() {
        return mService != null;
    }

    public static void sendSimpleNotification(String title, String content) {
        ///TODO Intent intent = new Intent(mService, PermissionGrantActivity.class);
        ///TODO PendingIntent pi = PendingIntent.getActivity(mService, 0, intent, 0);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(mService, CHANNEL_NORMAL_NOTIFICATION_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                ///TODO .setContentIntent(pi)
                .setAutoCancel(true)
                .setShowWhen(true);
        normalNotificationManager.notify(NORMAL_NOTIFICATION_ID, nb.build());
    }

    public void setForegroundService() {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.layout_foreground_notification);
        String channelName = getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_SERVICE_KEEPER_ID, channelName, importance);
        channel.setDescription(getString(R.string.channel_description));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_SERVICE_KEEPER_ID);
        builder.setSmallIcon(R.drawable.ic_notification)
                .setCustomContentView(notificationLayout)
                .setOngoing(true);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        startForeground(KEEPER_NOTIFICATION_ID, builder.build());
        isForegroundServiceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        l("Service starting...");

        Intent homePkgIntent = new Intent(Intent.ACTION_MAIN);
        homePkgIntent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(homePkgIntent, PackageManager.MATCH_DEFAULT_ONLY);
        currentHomePackage = resolveInfo.activityInfo.packageName;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());

        mHandler.postDelayed(() -> {
            try {
                if (!isStart()) {
                    Toast.makeText(SeeNot.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
                    le("Service invoking didn't respond, a manual start might be needed.");
                    sendSimpleNotification("不见君服务未开启", "不见君服务没有成功开启，请前往系统无障碍设置手动开启");
                }
            } catch (Exception ignored) {
            }
        }, 2000);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;

        if (isFirstTimeInvokeService) {
            isFirstTimeInvokeService = false;

            l("Service invoking...");

            if (sharedPreferences != null) {
                isServiceRunning = sharedPreferences.getBoolean("master-switch", true);
                isSplitScreenAcceptable = sharedPreferences.getBoolean("split", true);
            }

            try {
                setForegroundService();
            } catch (Exception e) {
                Toast.makeText(SeeNot.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
                le("Starting foreground service failed, err message: " + e);
            }
        } else le("Service already invoked");
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
        isFirstTimeInvokeService = true;
        if (isForegroundServiceRunning) {
            stopForeground(true);
            isForegroundServiceRunning = false;
        }
        mService = null;
        super.onDestroy();
    }
}
