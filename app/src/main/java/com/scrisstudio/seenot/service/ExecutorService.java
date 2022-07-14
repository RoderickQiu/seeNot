package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;

import java.util.ArrayList;
import java.util.List;

public class ExecutorService extends AccessibilityService {
    public static final String TAG = "SeeNot-AccessibilityService";
    public static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
    public static final int KEEPER_NOTIFICATION_ID = 408, NORMAL_NOTIFICATION_ID = 488;
    public static ExecutorService mService;
    public static Boolean isServiceRunning = false, isForegroundServiceRunning = false,
            isFirstTimeInvokeService = true, isSoftInputPanelOn = false, hasSoftInputPanelJustFound = false,
            lastTimeClassCapable = false;
    public static int foregroundWindowId = 0;
    private String currentHomePackage = "", foregroundClassName = "",
            foregroundPackageName = "com.scrisstudio.seenot", lastTimePackageName = "",
            lastTimeClassName = "com.scrisstudio.seenot.MainActivity";
    private ComponentName lastTimeComponentName = new ComponentName("com.scrisstudio.seenot",
            "com.scrisstudio.seenot.MainActivity");
    private ActivityInfo lastTimeActivityInfo = new ActivityInfo();
    private SharedPreferences sharedPreferences;
    public static NotificationManager normalNotificationManager;
    private final Handler mHandler = new Handler();
    public NotificationChannel normalNotificationChannel;
    private static ArrayList<RuleInfo> rulesList, currentRules = new ArrayList<>();
    private final Gson gson = new Gson();
    private Rect nodeSearcherRect = new Rect();
    private PackageManager packageManager;


    public static boolean isStart() {
        return mService != null;
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.default_notification_channel);
        int importance = NotificationManager.IMPORTANCE_LOW;
        normalNotificationChannel = new NotificationChannel(CHANNEL_NORMAL_NOTIFICATION_ID, name, importance);
        normalNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        normalNotificationChannel.setBypassDnd(true);
        normalNotificationManager = getSystemService(NotificationManager.class);
        normalNotificationManager.createNotificationChannel(normalNotificationChannel);
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

        createNotificationChannel();

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
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());
                if (sharedPreferences != null) {
                    rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
                    }.getType());
                }
            }

            packageManager = getPackageManager();

            try {
                setForegroundService();
            } catch (Exception e) {
                Toast.makeText(SeeNot.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
                le("Starting foreground service failed, err message: " + e);
            }
        } else le("Service already invoked");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            try {
                foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
            } catch (Exception e) {
                foregroundPackageName = event.getPackageName().toString();
            }
            ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);
            le(cName);

            if (isServiceRunning) {
                if (!isSoftInputPanelOn && !foregroundPackageName.equals(lastTimePackageName)) {
                    lastTimePackageName = foregroundPackageName;
                    currentRules.clear();
                    for (int i = 0; i < rulesList.size(); i++) {
                        if (rulesList.get(i).getFor().equals(lastTimePackageName) && !foregroundPackageName.equals("") && rulesList.get(i).getStatus()) {
                            currentRules.add(rulesList.get(i));
                        }
                    }
                    le("Current rules changed, " + currentRules);
                }

                hasSoftInputPanelJustFound = false;
                for (int i = 0; i < getWindows().size(); i++) {
                    if (currentRules.size() > 0) {
                        try {
                            //查找输入法是否打开：TYPE_INPUT_METHOD = 2
                            if (getWindows().get(i).getType() == 2) {
                                hasSoftInputPanelJustFound = true;
                                if (!isSoftInputPanelOn) {
                                    isSoftInputPanelOn = true;
                                    l("Input method is enabled.");
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                //execute rules
                if (!foregroundPackageName.equals(currentHomePackage) && !foregroundPackageName.equals("com.scrisstudio.seenot") && !foregroundPackageName.equals("")) {
                    if (hasRealActivity(cName) && isCapableClass(foregroundClassName)) {
                        //TODO
                    }
                }
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isCapableClass(event.getClassName().toString())) {
                foregroundClassName = event.getClassName().toString();
                foregroundWindowId = event.getWindowId();
                l(event.getClassName().toString() + event.getWindowId());
            }
        }
    }


    private boolean hasRealActivity(ComponentName componentName) {
        if (componentName.equals(lastTimeComponentName))
            return lastTimeActivityInfo != null;
        else {
            lastTimeComponentName = componentName;
            try {
                lastTimeActivityInfo = packageManager.getActivityInfo(componentName, 0);
            } catch (Exception e) {
                le("ActivityInfo not found.");
            }
            return lastTimeActivityInfo != null;
        }
    }

    private boolean isCapableClass(String className) {
        if (className.equals(lastTimeClassName))
            return lastTimeClassCapable;
        else {
            if (className == null) return false;
            else if (className.contains("Activity")) return true;
            else
                return !className.startsWith("android.widget.") && !className.startsWith("android.view.") && !className.startsWith("androidx.") && !className.startsWith("com.android.systemui");
        }
    }

    AccessibilityNodeInfo getRightWindowNode() {
        //使兼容多窗模式
        for (int i = 0; i < getWindows().size(); i++) {
            if (getWindows().get(i).getId() == foregroundWindowId) {
                return getWindows().get(i).getRoot();
            }
        }
        return getRootInActiveWindow();
    }

    private Rect nodeSearcher(ArrayList<Integer> indices) {
        try {
            AccessibilityNodeInfo node = getRightWindowNode();
            for (int i = 0; i < indices.size(); i++) {
                node = node.getChild(indices.get(i));
            }

            node.getBoundsInScreen(nodeSearcherRect);
            return nodeSearcherRect;
        } catch (Exception e) {
            le("Node search really went wrong: " + e);
            return null;
        }
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
