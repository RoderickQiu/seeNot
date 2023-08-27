package com.scrisstudio.seenot.service;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;
import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.SeeNot.typesCnt;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.struct.FilterInfo;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.timed.RuleTimedAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.internal.Intrinsics;

public class ExecutorService extends AccessibilityService {
    public static final String TAG = "SeeNot-AccessibilityService";
    public static final int MODE_ASSIGNER = 0, MODE_EXECUTOR = 1;
    public static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
    public static final int KEEPER_NOTIFICATION_ID = 408;
    public static ExecutorService mService;
    public static Boolean isServiceRunning = false, isForegroundServiceRunning = false,
            isFirstTimeInvokeService = true, lastTimeClassCapable = false, isDarkModeOn = false;
    public static int foregroundWindowId = 0;
    public static String currentHomePackage = "", foregroundClassName = "",
            foregroundPackageName = "com.scrisstudio.seenot", lastTimePackageName = "",
            lastTimeClassName = "com.scrisstudio.seenot.MainActivity",
            packageName = "";
    public static SharedPreferences sharedPreferences;
    public static NotificationManager normalNotificationManager;
    private final Handler mHandler = new Handler();
    public NotificationChannel normalNotificationChannel;
    public static LayoutInflater inflater;
    public static WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
    private static Map<Integer, ArrayList<Integer>> timedCorrespList = new HashMap<>();
    private static Map<Integer, Long> timedLastCheckedList = new HashMap<>();
    private static Map<Integer, Integer> timedLastResultList = new HashMap<>();
    private static ArrayList<TimedInfo> timedList = new ArrayList<>();
    private static ArrayList<RuleInfo> rulesList = new ArrayList<>();
    private static ArrayList<FilterInfo> currentFilters = new ArrayList<>(), tempFilters;
    private static FilterInfo tempFilter;
    private long lastHandlerRunningTime = 0, handlerTime = 0;
    private static PeriodicWorkRequest stayRequest;
    private static Random random = new Random();
    public static Resources resources;
    public static ArrayList<View> mFloatingViews = new ArrayList<>();
    public static WindowManager mWindowManager;
    public static Process logCatProcess;
    private static final String[] filterTypeNames = new String[typesCnt];

    public static boolean isStart() {
        return mService != null;
    }

    public static void setServiceBasicInfo(SharedPreferences sharedPreferences, int mode) {
        Gson gson = new Gson();
        ExecutorService.sharedPreferences = sharedPreferences;

        timedList = gson.fromJson(sharedPreferences.getString("timed", "{}"), new TypeToken<List<TimedInfo>>() {
        }.getType());
        rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());
        isServiceRunning = mode == MODE_EXECUTOR && sharedPreferences.getBoolean("master-switch", true);

        for (int i = 0; i < timedList.size(); i++) {
            int idFor = timedList.get(i).getIdFor();
            ArrayList<Integer> list = timedCorrespList.get(idFor);
            if (list == null) list = new ArrayList<>();
            list.add(i);
            timedCorrespList.put(idFor, list);
        }

        for (int i = 0; i <= sharedPreferences.getInt("rule-id-max", 0); i++) {
            timedLastCheckedList.put(i, 0L);
            timedLastResultList.put(i, 0);
        }

        le("Set service basic info, " + isServiceRunning);
    }

    private static boolean isTodayCovered(TimedInfo timed) {
        if (timed.getScope() != 0) {
            Calendar calendar = Calendar.getInstance();
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            weekday = (weekday == 1) ? 7 : weekday - 1;
            return RuleTimedAdapter.getRealScope(timed.getScope()).contains(weekday);
        } else {
            return (new Date().getDate() == new Date(timed.getFirstLaunchTime()).getDate())
                    && (new Date().getMonth() == new Date(timed.getFirstLaunchTime()).getMonth());
        }
    }

    public static int isTimedHavingEffect(int ruleId, boolean isInstant) {
        try {
            if (SystemClock.uptimeMillis() - timedLastCheckedList.get(ruleId) > 60000 || isInstant) {
                timedLastCheckedList.put(ruleId, SystemClock.uptimeMillis());
                ArrayList<Integer> list = timedCorrespList.get(ruleId);
                if (list == null) return 0;
                else if (list.size() == 0) return 0;
                else {
                    int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(new Date()));
                    int minute = Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(new Date()));
                    Date start, end;
                    for (int i : list) {
                        if (!timedList.get(i).getStatus()) continue;
                        if (!isTodayCovered(timedList.get(i))) continue; // judge week day
                        start = new Date(timedList.get(i).getStartTime());
                        end = new Date(timedList.get(i).getEndTime());
                        l("START" + new SimpleDateFormat("HH:mm", Locale.CHINA).format(start));
                        l("END" + new SimpleDateFormat("HH:mm", Locale.CHINA).format(end));
                        l("NOW" + new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
                        if ((Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(start)) < hour
                                || (Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(start)) == hour &&
                                Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(start)) <= minute))
                                && (Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(end)) > hour
                                || (Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(end)) == hour &&
                                Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(end)) >= minute))) {
                            timedLastResultList.put(ruleId, timedList.get(i).getMode() ? 1 : -1);
                            return timedList.get(i).getMode() ? 1 : -1;
                        }
                    }
                }
                timedLastResultList.put(ruleId, 0);
                return 0;
            } else return timedLastResultList.get(ruleId);
        } catch (Exception e) {
            le("Timed parse ERR: " + e.getLocalizedMessage());
            return 0;
        }
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.default_notification_channel);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        normalNotificationChannel = new NotificationChannel(CHANNEL_NORMAL_NOTIFICATION_ID, name, importance);
        normalNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        normalNotificationChannel.setBypassDnd(true);
        normalNotificationManager = getSystemService(NotificationManager.class);
        normalNotificationManager.createNotificationChannel(normalNotificationChannel);
    }

    public static void sendSimpleNotification(String title, String content) {
        try {
            Intent intent = new Intent(mService, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder nb = new NotificationCompat.Builder(mService, CHANNEL_NORMAL_NOTIFICATION_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            normalNotificationManager.notify(random.nextInt(), nb.build());
        } catch (Exception e) {
            le("ERR: " + e.getMessage() + " (mService)");
            try {
                Intent intent = new Intent(SeeNot.getAppContext(), MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(SeeNot.getAppContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Builder nb = new NotificationCompat.Builder(SeeNot.getAppContext(), CHANNEL_NORMAL_NOTIFICATION_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setShowWhen(true)
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                normalNotificationManager.notify(random.nextInt(), nb.build());
            } catch (Exception e1) {
                le("ERR: " + e1.getMessage() + " (SeeNot Context)");
            }
        }
    }

    public void setForegroundService() {
        mService = this;
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.layout_foreground_notification);
        String channelName = getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_SERVICE_KEEPER_ID, channelName, importance);
        channel.setDescription(getString(R.string.channel_description));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SeeNot.getAppContext(), CHANNEL_SERVICE_KEEPER_ID);
        builder.setSmallIcon(R.drawable.ic_notification)
                .setCustomContentView(notificationLayout)
                .setOngoing(true)
                .setPriority(PRIORITY_LOW);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(SeeNot.getAppContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        startForeground(KEEPER_NOTIFICATION_ID, builder.build());
        isForegroundServiceRunning = true;
    }

    public static boolean isNightMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        l("Start command");
        try {
            setForegroundService();
        } catch (Exception e) {
            Toast.makeText(SeeNot.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
            le("1Starting foreground service failed, err message: " + e);
        }
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

        packageName = MainActivity.packageName;

        mHandler.postDelayed(() -> {
            try {
                if (!isStart()) {
                    Toast.makeText(SeeNot.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
                    le("Service invoking didn't respond, a manual start might be needed.");
                    sendSimpleNotification(resources.getString(R.string.service_not_enabled_short),
                            resources.getString(R.string.service_start_failed));
                }
            } catch (Exception ignored) {
            }
        }, 4000);

        WorkManager.getInstance(this).cancelAllWorkByTag("stay-request");
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
        inflater = LayoutInflater.from(this);

        l("Service connected");

        isDarkModeOn = isNightMode(SeeNot.getAppContext());

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.gravity = 51;
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        layoutParams.format = 1;
        layoutParams.flags = 40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }

        try {
            FloatingOperatorUtil.addView(0, 0, 1, 2, 0);
        } catch (Exception e) {
            le("Failed adding floating, ERR: " + e.getLocalizedMessage());
            Toast.makeText(SeeNot.getAppContext(), R.string.floating_start_failed, Toast.LENGTH_LONG).show();
        }

        if (isFirstTimeInvokeService) {
            isFirstTimeInvokeService = false;

            l("Service invoking...");

            for (int i = 0; i < typesCnt; i++) {
                filterTypeNames[i] = SeeNot.getFilterTypeName(i);
            }

            if (sharedPreferences != null) {
                setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
            }
        } else le("Service already invoked");

        try {
            setForegroundService();
        } catch (Exception e) {
            Toast.makeText(SeeNot.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
            le("2Starting foreground service failed, err message: " + e);
        }

        WorkManager.getInstance(this).cancelAllWorkByTag("stay-request");

        stayRequest = new PeriodicWorkRequest
                .Builder(TimedWorkManager.class, 15, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                        PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .addTag("stay-request")
                .build();

        WorkManager.getInstance(SeeNot.getAppContext()).enqueueUniquePeriodicWork("stay", ExistingPeriodicWorkPolicy.REPLACE, stayRequest);

        // make log persist
        File logDir = this.getExternalFilesDir(null);
        if (logDir != null) {
            Intrinsics.checkNotNullExpressionValue(logDir, "publicAppDirectory");
            File logDirectory = new File(logDir.getAbsolutePath() + "/logs");
            if (!logDirectory.exists()) {
                if (!logDirectory.mkdir())
                    le("Log mkdir failed. ");
            }

            File logFile = new File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt");

            try {
                Runtime.getRuntime().exec("logcat -c");
                logCatProcess = Runtime.getRuntime().exec("logcat -f " + logFile +
                        " \"SeeNot-AccessibilityService:I SeeNot-Dialog:I *:W\"");
            } catch (IOException e) {
                le("Write to log file failed, ERR: " + e.getLocalizedMessage());
            }

            l("Logging persist service started, file: " + logFile.getAbsolutePath());
        }
    }

    private void alertWindowStateChange(String packageName, String className, int windowId) {
        if (!foregroundClassName.equals(className) || !foregroundPackageName.equals(packageName))
            l("Window state change: " + packageName + "/" + className + " " + windowId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (event.getPackageName() == null) return;//fix crash
            if (!event.getPackageName().toString().contains("seenot")) {
                alertWindowStateChange(event.getPackageName().toString(), foregroundClassName, foregroundWindowId);
                foregroundPackageName = event.getPackageName().toString();
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            if (event.getClassName() == null || event.getPackageName() == null) return;//fix crash
            if (isCapableClass(event.getClassName().toString()) && !event.getPackageName().toString().contains("seenot")) {
                alertWindowStateChange(event.getPackageName().toString(), event.getClassName().toString(), foregroundWindowId);
                foregroundPackageName = event.getPackageName().toString();
                foregroundClassName = event.getClassName().toString();
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (event.getContentChangeTypes() == 0) return;
            if (isServiceRunning) {
                if (!foregroundPackageName.equals(lastTimePackageName)) {
                    lastTimePackageName = foregroundPackageName;
                    currentFilters.clear();
                    int effect;
                    for (int i = 0; i < rulesList.size(); i++) {
                        if (rulesList.get(i).getFor().equals(lastTimePackageName) && !foregroundPackageName.equals("")) {
                            if (!rulesList.get(i).getStatus() && (rulesList.get(i).getReopenTime() != 0
                                    && rulesList.get(i).getReopenTime() < new Date().getTime()))
                                rulesList.get(i).setStatus(true);
                            effect = isTimedHavingEffect(rulesList.get(i).getId(), false);
                            le("EFFECT" + effect);
                            if (!rulesList.get(i).getStatus()) {
                                if (effect != 1) continue;
                            } else {
                                if (effect == -1) continue;
                            }
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
                if (!foregroundPackageName.equals(currentHomePackage) && !foregroundPackageName.contains("seenot")
                        && !foregroundPackageName.equals("") && currentFilters.size() > 0) {
                    //&& hasRealActivity(cName) && isCapableClass(foregroundClassName)) {

                    handlerTime = SystemClock.uptimeMillis();
                    if (handlerTime - lastHandlerRunningTime >= 256) { // have a little pause
                        lastHandlerRunningTime = handlerTime;

                        for (int i = 0; i < currentFilters.size(); i++) {
                            tempFilter = currentFilters.get(i);
                            switch (tempFilter.getType()) {
                                case 0:
                                    performGlobalAction(GLOBAL_ACTION_HOME);
                                    Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                            , Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    if (foregroundClassName.equals(tempFilter.getParam1())) {
                                        if (performGlobalAction(GLOBAL_ACTION_BACK)) {
                                            Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()] + "：\"" + tempFilter.getParam1().replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                        } else {
                                            performGlobalAction(GLOBAL_ACTION_HOME);
                                            Toast.makeText(SeeNot.getAppContext(), resources.getString(R.string.back_last_failed) + "\"" + tempFilter.getParam1().replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                                default: // 2, 3, 4, 5
                                    if (!tempFilter.getParam1().equals("---")) {
                                        for (int j = 0; j < getWindows().size(); j++) {
                                            if (getWindows().get(j).isActive())
                                                wordFinder(getWindows().get(j).getRoot(), true, tempFilter.getParam1(), tempFilter.getType());
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getClassName() == null || event.getPackageName() == null) return;//fix crash
            if (isCapableClass(event.getClassName().toString())) {
                alertWindowStateChange(event.getPackageName().toString(), event.getClassName().toString(), event.getWindowId());
                foregroundPackageName = event.getPackageName().toString();
                foregroundClassName = event.getClassName().toString();
                foregroundWindowId = event.getWindowId();
            }
        }
    }

    private boolean isCapableClass(String className) {
        if (className == null) return false;
        if (className.equals(lastTimeClassName))
            return lastTimeClassCapable;
        else {
            if (className.contains("Activity"))
                return true;
            else
                return !className.startsWith("android.widget.") && !className.startsWith("android.view.")
                        && !className.startsWith("androidx.") && !className.startsWith("com.android.systemui")
                        && !className.startsWith("android.app")
                        && !className.startsWith("android.inputmethodservice");
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

    public void wordFinder(AccessibilityNodeInfo root, boolean isOnlyVisible, String word, int type) {
        if (!isServiceRunning || root == null) return;

        ArrayList<AccessibilityNodeInfo> queue = new ArrayList<>();
        ArrayList<Integer> gradientQueue = new ArrayList<>();
        queue.add(root);
        gradientQueue.add(0);

        String temp = null;
        Rect tempRect = new Rect();
        while (queue.size() > 0) {
            AccessibilityNodeInfo info = queue.remove(0);
            int gradient = gradientQueue.remove(0);
            if (info == null) continue;

            try {
                if ((!isOnlyVisible || info.isVisibleToUser()) || gradient > 0) {
                    if (info.getChildCount() != 0) {
                        for (int i = 0; i < info.getChildCount(); i++) {
                            if (info.getChild(i) != null) {
                                if (info.toString().contains("WebView") || gradient > 0)
                                    gradientQueue.add(gradient == 0 ? 5 : gradient - 1);
                                else
                                    gradientQueue.add(0);
                                queue.add(info.getChild(i));
                            }
                        }
                    }

                    switch (type) {
                        case 2: // text force-back
                            temp = null;
                            if (info.getText() != null) temp = info.getText().toString();
                            else if (info.getContentDescription() != null)
                                temp = info.getContentDescription().toString();
                            else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                temp = (String) info.getTooltipText();
                            }

                            if (temp != null) {
                                if (temp.equals(word)) {
                                    if (performGlobalAction(GLOBAL_ACTION_BACK)) {
                                        Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                                + "：\"" + word + "\"", Toast.LENGTH_SHORT).show();
                                    } else {
                                        performGlobalAction(GLOBAL_ACTION_HOME);
                                        Toast.makeText(SeeNot.getAppContext(), resources.getString(R.string.back_last_failed) + "\"" + word + "\"", Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }
                            }
                            break;
                        case 3: // id force-back
                            temp = info.getViewIdResourceName();
                            if (temp != null) {
                                if (temp.equals(word)) {
                                    if (performGlobalAction(GLOBAL_ACTION_BACK)) {
                                        Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                                + "：\"" + word.replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                    } else {
                                        performGlobalAction(GLOBAL_ACTION_HOME);
                                        Toast.makeText(SeeNot.getAppContext(), resources.getString(R.string.back_last_failed) + "\"" + word.replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }
                            }
                            break;
                        case 4: // id auto-click
                            temp = info.getViewIdResourceName();
                            if (temp != null) {
                                if (temp.equals(word)) {
                                    getClickable(info).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                            + "：\"" + word.replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            break;
                        case 5: // text auto-click
                            temp = null;
                            if (info.getText() != null) temp = info.getText().toString();
                            else if (info.getContentDescription() != null)
                                temp = info.getContentDescription().toString();
                            else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                temp = (String) info.getTooltipText();
                            }

                            if (temp != null) {
                                if (temp.equals(word)) {
                                    getClickable(info).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                            + "：\"" + word + "\"", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            break;
                        case 6: // text swipe
                            temp = null;
                            if (info.getText() != null) temp = info.getText().toString();
                            else if (info.getContentDescription() != null)
                                temp = info.getContentDescription().toString();
                            else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                temp = (String) info.getTooltipText();
                            }

                            if (temp != null) {
                                if (temp.equals(word)) {
                                    doGesture(0);
                                    Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                            + "：\"" + word + "\"", Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        case 7: // id swipe
                            temp = info.getViewIdResourceName();
                            if (temp != null) {
                                if (temp.equals(word)) {
                                    doGesture(0);
                                    Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                            + "：\"" + word.replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        case 8: // coordinate force-back
                            info.getBoundsInScreen(tempRect);
                            if (tempRect.toString().equals(word)) {
                                if (performGlobalAction(GLOBAL_ACTION_BACK)) {
                                    Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                            + "：\"" + word.replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                } else {
                                    performGlobalAction(GLOBAL_ACTION_HOME);
                                    Toast.makeText(SeeNot.getAppContext(), resources.getString(R.string.back_last_failed) + "\"" + word.replace(foregroundPackageName, "") + "\"", Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }
                            break;
                        case 9: // coordinate auto-click
                            info.getBoundsInScreen(tempRect);
                            if (tempRect.toString().equals(word)) {
                                getClickable(info).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Toast.makeText(SeeNot.getAppContext(), filterTypeNames[tempFilter.getType()]
                                        + "：\"" + word + "\"", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                le("ERR: " + e.getMessage());
            }
        }
    }

    private void doGesture(int mode) {
        Path path = new Path();
        if (mode == 0) {
            path.moveTo(1000, 1000);//滑动起点
            path.lineTo(1000, 2000);//滑动终点
        } else {
            path.moveTo(1000, 2000);//滑动起点
            path.lineTo(1500, 2000);//滑动终点
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 100L, 100L)).build();
        dispatchGesture(description, new GestureCallBack(), null);
    }

    private AccessibilityNodeInfo getClickable(AccessibilityNodeInfo info) {
        if (info.isClickable()) {
            return info;
        } else {
            if (info.getParent() == null) return info;
            else return getClickable(info.getParent());
        }
    }

    @Override
    public void onInterrupt() {
        le("Service interrupt");
        mService = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        le("Service unbind");
        mService = null;
        try {
            sendSimpleNotification(resources.getString(R.string.service_destroyed), resources.getString(R.string.go_accessib_for_reopen));
        } catch (Exception ignored) {
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        le("Service destroy");
        try {
            sendSimpleNotification(resources.getString(R.string.service_destroyed), resources.getString(R.string.go_accessib_for_reopen));
        } catch (Exception ignored) {
        }
        isFirstTimeInvokeService = true;
        if (isForegroundServiceRunning) {
            stopForeground(true);
            isForegroundServiceRunning = false;
        }
        mService = null;
        super.onDestroy();
    }

    private class GestureCallBack extends GestureResultCallback {
        public GestureCallBack() {
            super();
        }

        @Override
        public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);

        }

        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);

        }
    }
}

