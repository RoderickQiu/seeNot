package com.scrisstudio.jianfou.mask;
import static com.scrisstudio.jianfou.mask.MaskOperatorUtil.removeViews;
import static com.scrisstudio.jianfou.mask.MixedOperatorUtils.isCapableClass;
import static com.scrisstudio.jianfou.mask.MixedOperatorUtils.l;
import static com.scrisstudio.jianfou.mask.MixedOperatorUtils.le;
import static com.scrisstudio.jianfou.mask.MixedOperatorUtils.sendSimpleNotification;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.info.MixedRuleInfo;
import com.scrisstudio.jianfou.info.RuleInfo;
import com.scrisstudio.jianfou.jianfou;

import java.util.ArrayList;
import java.util.List;

public class MixedExecutorService extends AccessibilityService {
	public static final String TAG = "Jianfou-AccessibilityService";
	public static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
	public static final int KEEPER_NOTIFICATION_ID = 9221, NORMAL_NOTIFICATION_ID = 9311;
	private static final int NORMAL_MASK_CONST = 4;
	public static MixedExecutorService mService;
	public static List<MixedRuleInfo> rulesList;
	public static boolean isServiceRunning = true, isFirstTimeInvokeService = true,
			isReceiverRegistered = false, isForegroundServiceRunning = false,
			isSoftInputPanelOn = false, hasSoftInputPanelJustFound = false, isSkipping = false,
			isMaskOn = false, isSplitScreenAcceptable = false, isDarkModeOn = false;
	public static int foregroundWindowId = 0, foregroundWindowLayer = 1;
	public static String foregroundClassName = "", foregroundPackageName = "com.scrisstudio.jianfou", currentHomePackage = "";
	public static LayoutInflater inflater;
	public static WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
	public static ArrayList<View> mFloatingViews = new ArrayList<>();
	public static WindowManager mWindowManager;
	public static NotificationManager normalNotificationManager;
	private static String windowOrientation = "portrait";
	private static int windowTrueWidth, windowTrueHeight;
	private static RuleInfo currentRule;
	private static ArrayList<Boolean> maskList = null;
	private static ArrayList<Integer> cntList = null;
	private final Handler mHandler = new Handler();
	public NotificationChannel normalNotificationChannel;
	private BroadcastReceiver mScreenOReceiver;
	private SharedPreferences sharedPreferences;
	private long lastContentChangedTime = 0, lastHandlerRunningTime = 0, contentChangeTime = 0, handlerTime = 0;
	private Rect contentRect = new Rect(), nodeSearcherRect = new Rect(), dynamicRect = new Rect();
	private int x = -1, y = -1, width = -1, height = -1, currentRuleId = -1, maskCnt = 0;

	public static boolean isStart() {
		return mService != null;
	}

	public static void setRulesList(List<MixedRuleInfo> l) {
		rulesList = l;
	}

	public static void setServiceBasicInfo(String rules, Boolean masterSwitch, Boolean split) {
		Gson gson = new Gson();
		rulesList = gson.fromJson(rules, new TypeToken<List<MixedRuleInfo>>() {
		}.getType());
		isServiceRunning = masterSwitch;
		isSplitScreenAcceptable = !split;
	}

	public void setForegroundService() {
		RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.layout_foreground_notification);
		String channelName = getString(R.string.channel_name);
		int importance = NotificationManager.IMPORTANCE_LOW;
		NotificationChannel channel = new NotificationChannel(CHANNEL_SERVICE_KEEPER_ID, channelName, importance);
		channel.setDescription(getString(R.string.channel_description));
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_SERVICE_KEEPER_ID);
		builder.setSmallIcon(R.drawable.jianfou_no_bg)
				.setCustomContentView(notificationLayout)
				.setOngoing(true);
		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		builder.setContentIntent(pendingIntent);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.createNotificationChannel(channel);
		startForeground(KEEPER_NOTIFICATION_ID, builder.build());
		isForegroundServiceRunning = true;
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		l("Service starting...");

		windowTrueWidth = jianfou.getAppContext().getResources().getDisplayMetrics().widthPixels;
		windowTrueHeight = jianfou.getAppContext().getResources().getDisplayMetrics().heightPixels;

		Intent homePkgIntent = new Intent(Intent.ACTION_MAIN);
		homePkgIntent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(homePkgIntent, PackageManager.MATCH_DEFAULT_ONLY);
		currentHomePackage = resolveInfo.activityInfo.packageName;

		createNotificationChannel();

		//TODO setMaskCntListEmpty();

		Gson gson = new Gson();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(jianfou.getAppContext());
		if (sharedPreferences != null) {
			rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
			}.getType());
		}

		mHandler.postDelayed(() -> {
			try {
				if (!isStart()) {
					Toast.makeText(jianfou.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
					le("Service invoking didn't respond, a manual start might be needed.");

					if (!jianfou.isDebugApp())
						sendSimpleNotification("见否服务未开启", "见否服务没有成功开启，请前往系统无障碍设置手动开启", mService);
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


		if (isFirstTimeInvokeService) {
			isFirstTimeInvokeService = false;

			l("Service invoking...");

			if (sharedPreferences != null) {
				isServiceRunning = sharedPreferences.getBoolean("master-switch", true);
			}

			mScreenOReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (action.equals("android.intent.action.SCREEN_OFF")) {
						le("Screen Off");
						try {
							removeViews();
						} catch (Exception e) {
							le("Failed to remove mask");
						}
					}
				}

			};

			try {
				IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
				this.registerReceiver(mScreenOReceiver, mScreenOffFilter);
				isReceiverRegistered = true;
			} catch (Exception e) {
				le("Monitoring locker failed, err message: " + e.toString());
			}

			try {
				setForegroundService();
			} catch (Exception e) {
				Toast.makeText(jianfou.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
				le("Starting foreground service failed, err message: " + e.toString());
			}
		} else le("Service already invoked");
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO regain activitySeekerService functions
		// TODO make it compatible with new mixedRuleInfo system
		// TODO make it works without the concept of currentRule
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			try {
				foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
			} catch (Exception e) {
				foregroundPackageName = event.getPackageName().toString();
			}
			ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);
		} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			if (isCapableClass(event.getClassName().toString()) && !event.getPackageName().equals(currentHomePackage)) {
				foregroundClassName = event.getClassName().toString();
				foregroundWindowId = event.getWindowId();
				l(event.getClassName().toString() + event.getWindowId());
			}
		}
	}

	private AccessibilityNodeInfo getRightWindowNode() {
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

	private ActivityInfo tryGetActivity(ComponentName componentName) {
		try {
			return getPackageManager().getActivityInfo(componentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
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
		if (isReceiverRegistered) {
			this.unregisterReceiver(mScreenOReceiver);
			isReceiverRegistered = false;
		}
		if (isForegroundServiceRunning) {
			stopForeground(true);
			isForegroundServiceRunning = false;
		}
		mService = null;
		super.onDestroy();
	}
}
