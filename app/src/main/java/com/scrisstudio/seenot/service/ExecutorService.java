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
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.WindowManager;
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
    public static final int MODE_ASSIGNER = 0, MODE_EXECUTOR = 1;
    public static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
    public static final int KEEPER_NOTIFICATION_ID = 408, NORMAL_NOTIFICATION_ID = 488;
    public static ExecutorService mService;
    public static Boolean isServiceRunning = false, isForegroundServiceRunning = false,
            isFirstTimeInvokeService = true, isSoftInputPanelOn = false, hasSoftInputPanelJustFound = false,
            lastTimeClassCapable = false;
    public static int foregroundWindowId = 0;
    public static String currentHomePackage = "", foregroundClassName = "",
            foregroundPackageName = "com.scrisstudio.seenot", lastTimePackageName = "",
            lastTimeClassName = "com.scrisstudio.seenot.MainActivity";
    private ComponentName lastTimeComponentName = new ComponentName("com.scrisstudio.seenot",
            "com.scrisstudio.seenot.MainActivity"), cName = lastTimeComponentName;
    private ActivityInfo lastTimeActivityInfo = new ActivityInfo();
    public static SharedPreferences sharedPreferences;
    public static NotificationManager normalNotificationManager;
    private final Handler mHandler = new Handler();
    public NotificationChannel normalNotificationChannel;
    public static LayoutInflater inflater;
    public static WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
    private static ArrayList<RuleInfo> rulesList = new ArrayList<>();
    private static ArrayList<FilterInfo> currentFilters = new ArrayList<>(), tempFilters;
    private static FilterInfo tempFilter;
    private final Gson gson = new Gson();
    private Rect nodeSearcherRect = new Rect();
    private PackageManager packageManager;
    private long lastContentChangedTime = 0, lastHandlerRunningTime = 0, contentChangeTime = 0, handlerTime = 0;
    public static Resources resources;


    public static boolean isStart() {
        return mService != null;
    }

    public static void setServiceBasicInfo(SharedPreferences sharedPreferences, int mode) {
        Gson gson = new Gson();
        ExecutorService.sharedPreferences = sharedPreferences;

        rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());
        isServiceRunning = mode == MODE_EXECUTOR && sharedPreferences.getBoolean("master-switch", true);

        le("Set service basic info, " + isServiceRunning);
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
        Intent intent = new Intent(mService, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mService, 0, intent, 0);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(mService, CHANNEL_NORMAL_NOTIFICATION_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi)
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
        inflater = LayoutInflater.from(this);


        if (isFirstTimeInvokeService) {
            isFirstTimeInvokeService = false;

            l("Service invoking...");

            resources = MainActivity.resources;

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
            if (isCapableClass(event.getPackageName().toString())) {
                foregroundPackageName = event.getPackageName().toString();
            }

            if (isServiceRunning) {
                if (!isSoftInputPanelOn && !foregroundPackageName.equals(lastTimePackageName)) {
                    lastTimePackageName = foregroundPackageName;
                    currentFilters.clear();
                    for (int i = 0; i < rulesList.size(); i++) {
                        if (rulesList.get(i).getFor().equals(lastTimePackageName) && !foregroundPackageName.equals("")) {
                            tempFilters = rulesList.get(i).getFilter();
                            for (int j = 0; j < rulesList.get(i).getFilterLength(); j++) {
                                if (!tempFilters.get(j).getStatus()) continue;
                                currentFilters.add(tempFilters.get(j));
                            }
                        }
                    }
                    le("Current filters changed, " + currentFilters);
                }

                //execute rules
                if (!foregroundPackageName.equals(currentHomePackage) && !foregroundPackageName.equals("com.scrisstudio.seenot")
                        && !foregroundPackageName.equals("") && hasRealActivity(cName) && isCapableClass(foregroundClassName)) {

                    handlerTime = SystemClock.uptimeMillis();
                    if (handlerTime - lastHandlerRunningTime >= 256) { // have a little pause
                        lastHandlerRunningTime = handlerTime;

                        for (int i = 0; i < getWindows().size(); i++) {
                            try {
                                // find if input method open：TYPE_INPUT_METHOD = 2
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

                        for (int i = 0; i < currentFilters.size(); i++) {
                            tempFilter = currentFilters.get(i);
                            switch (tempFilter.getType()) {
                                case 0:
                                    performGlobalAction(GLOBAL_ACTION_HOME);
                                    Toast.makeText(SeeNot.getAppContext(), resources.getString(SeeNot.getFilterTypeName(tempFilter.getType())), Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    if (foregroundClassName.equals(tempFilter.getParam1())) {
                                        performGlobalAction(GLOBAL_ACTION_BACK);
                                        Toast.makeText(SeeNot.getAppContext(), resources.getString(SeeNot.getFilterTypeName(tempFilter.getType())) + "：\"" + tempFilter.getParam1() + "\"", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 2:
                                    wordFinder(getRootInActiveWindow(), true, tempFilter.getParam1());
                                    break;
                            }
                        }
                    }
                }
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isCapableClass(event.getClassName().toString())) {
                foregroundClassName = event.getClassName().toString();
                foregroundWindowId = event.getWindowId();
                if (hasRealActivity(new ComponentName(foregroundPackageName, foregroundClassName)))
                    cName = new ComponentName(foregroundPackageName, foregroundClassName);
                l(foregroundClassName + foregroundWindowId);
            }
        }
    }


    private boolean hasRealActivity(ComponentName componentName) {
        if (componentName.equals(lastTimeComponentName))
            return lastTimeActivityInfo != null;
        else {
            lastTimeComponentName = componentName;
            lastTimeActivityInfo = null;
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
            else if (className.contains("Activity") && !className.contains("seenot"))
                return true;
            else
                return !className.startsWith("android.widget.") && !className.startsWith("android.view.")
                        && !className.startsWith("androidx.") && !className.startsWith("com.android.systemui")
                        && !className.startsWith("android.app") && !className.contains("seenot");
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

    public void wordFinder(AccessibilityNodeInfo root, boolean isOnlyVisible, String word) {
        if (!isServiceRunning || root == null) return;

        ArrayList<AccessibilityNodeInfo> queue = new ArrayList<>();
        queue.add(root);

        while (queue.size() > 0) {
            AccessibilityNodeInfo info = queue.remove(0);
            if (!isOnlyVisible || info.isVisibleToUser()) {
                if (info.getChildCount() != 0) {
                    for (int i = 0; i < info.getChildCount(); i++) {
                        if (info.getChild(i) != null) {
                            queue.add(info.getChild(i));
                        }
                    }
                }

                String text = null;
                if (info.getText() != null) text = info.getText().toString();
                else if (info.getContentDescription() != null)
                    text = info.getContentDescription().toString();

                if (text != null) {
                    if (text.equals(word)) {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        Toast.makeText(SeeNot.getAppContext(), resources.getString(SeeNot.getFilterTypeName(tempFilter.getType()))
                                + "：\"" + word + "\"", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
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
