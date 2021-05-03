package com.scrisstudio.jianfou.mask;

import android.accessibilityservice.AccessibilityService;
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
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;

import java.util.ArrayList;
import java.util.List;

public class ActivitySeekerService extends AccessibilityService {
	public static final String TAG = "Jianfou-AccessibilityService";
	private static final String CHANNEL_ID = "ServiceKeeper";
	private static final int NOTIFICATION_ID = 9221;
	public static ActivitySeekerService mService;
	public static List<RuleInfo> rulesList;
	public static boolean isServiceRunning = true, isFirstTimeInvokeService = true,
			isHandlerRunning = false, isReceiverRegistered = false, isForegroundServiceRunning = false,
			hasJustSkipped = false, isWordFinderRunning = false;
	public static String foregroundClassName = "", foregroundPackageName = "", currentHomePackage = "";
	private static String windowOrientation = "portrait";
	private static int windowTrueWidth, windowTrueHeight;
	private long lastContentChangedTime = 0;
	private FloatingViewManager mWindowManager;
	private boolean isMaskOn = false;
	private int x = -1, y = -1, width = -1, height = -1, currentRuleId = -1;
	private final BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals("android.intent.action.SCREEN_OFF")) {
				Log.e(TAG, "Screen Off");
				try {
					if (isMaskOn) maskCreator(false, -1, false);
				} catch (Exception e) {
					Log.i(TAG, "Failed to remove mask");
				}
			}
		}

	};

	public static boolean isStart() {
		return mService != null;
	}

	public static void setRulesList(List<RuleInfo> l) {
		rulesList = l;
	}

	public static void setServiceBasicInfo(String rules, Boolean masterSwitch) {
		Gson gson = new Gson();
		rulesList = gson.fromJson(rules, new TypeToken<List<RuleInfo>>() {
		}.getType());
		isServiceRunning = masterSwitch;
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "Service starting...");

		windowTrueWidth = jianfou.getAppContext().getResources().getDisplayMetrics().widthPixels;
		windowTrueHeight = jianfou.getAppContext().getResources().getDisplayMetrics().heightPixels;

		Intent homePkgIntent = new Intent(Intent.ACTION_MAIN);
		homePkgIntent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(homePkgIntent, PackageManager.MATCH_DEFAULT_ONLY);
		currentHomePackage = resolveInfo.activityInfo.packageName;

		try {
			Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.scrisstudio.jianfou/.mask.ActivitySeekerService");
			Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");
		} catch (Exception e) {
			//isSettingsModifierWorking = false;
			Toast.makeText(jianfou.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
			Log.e(TAG, "Service invoking failed, err message: " + e.toString());
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public void setForegroundService() {
		String channelName = getString(R.string.channel_name);
		int importance = NotificationManager.IMPORTANCE_LOW;
		NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
		channel.setDescription(getString(R.string.channel_description));
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setSmallIcon(R.drawable.jianfou_no_bg)
				.setContentTitle(getString(R.string.channel_notification_text))
				.setOngoing(true);
		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.createNotificationChannel(channel);
		startForeground(NOTIFICATION_ID, builder.build());
		isForegroundServiceRunning = true;
	}

	//初始化
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		mService = this;

		if (this.mWindowManager == null) {
			this.mWindowManager = new FloatingViewManager(this);
		}

		if (isFirstTimeInvokeService) {
			isFirstTimeInvokeService = false;

			Log.e(TAG, "Service invoking...");

			Gson gson = new Gson();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(jianfou.getAppContext());
			isServiceRunning = sharedPreferences.getBoolean("master-switch", true);
			rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
			}.getType());

			/* 注册机器锁屏时的广播 */
			try {
				IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
				this.registerReceiver(mScreenOReceiver, mScreenOffFilter);
				isReceiverRegistered = true;
			} catch (Exception ignored) {
			}

			try {
				setForegroundService();
			} catch (Exception ignored) {
			}
		}
	}

	//实现辅助功能
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
			if (currentRuleId == -1) wordFinder(getRootInActiveWindow(), true);
			else if (rulesList.get(currentRuleId).getType() == 0)
				wordFinder(getRootInActiveWindow(), true);
		} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			try {
				foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
			} catch (Exception e) {
				foregroundPackageName = event.getPackageName().toString();
			}
			ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);
			if (currentRuleId != -1) {
				if (foregroundPackageName.equals(currentHomePackage)) {
					if (isMaskOn) maskCreator(false, -1, false);
					currentRuleId = -1;
				} else if (foregroundPackageName.equals(rulesList.get(currentRuleId).getFilter().packageName) && !foregroundPackageName.equals("")) {
					if (tryGetActivity(cName) != null) {
						if (isCapableClass(foregroundClassName)) {
							if (!foregroundClassName.equals(rulesList.get(currentRuleId).getFilter().activityName)) {
								if (isMaskOn) maskCreator(false, -1, false);
								currentRuleId = -1;
							}
						}
					}
				}
			}

			//prevent a lot of events flood in to cause crash
			long time = SystemClock.uptimeMillis();
			if (time - lastContentChangedTime > 500) {
				lastContentChangedTime = time;
				try {
					Rect rect = new Rect();
					event.getSource().getBoundsInScreen(rect);
					if (currentRuleId != -1) {
						if (isMaskOn) {
							if (rect.width() == windowTrueWidth) {
								if (windowOrientation.equals("landscape")) {
									windowOrientation = "portrait";
									Log.e(TAG, "Change to Portrait");
									if (foregroundPackageName.equals(rulesList.get(currentRuleId).getFilter().packageName) && !foregroundPackageName.equals("")) {
										if (foregroundClassName.equals(rulesList.get(currentRuleId).getFilter().activityName)) {
											maskSet(rulesList.get(currentRuleId).getFilter(), currentRuleId);
										}
									}
								}
							} else if (rect.width() == windowTrueHeight) {
								if (windowOrientation.equals("portrait")) {
									windowOrientation = "landscape";
									Log.e(TAG, "Change to Landscape");
									if (foregroundPackageName.equals(rulesList.get(currentRuleId).getFilter().packageName) && !foregroundPackageName.equals("")) {
										if (foregroundClassName.equals(rulesList.get(currentRuleId).getFilter().activityName)) {
											maskSet(rulesList.get(currentRuleId).getFilter(), currentRuleId);
											Log.e(TAG, "Recover");
										}
									}
								}
							}
						} else if (rulesList.get(currentRuleId).getType() == 1) {
							wordFinder(getRootInActiveWindow(), true);
						}
					}
				} catch (Exception e) {
					Log.i(TAG, "Failed to get orientation, err message: " + e.toString());
				}

			}
		} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ComponentName cName = new ComponentName(foregroundPackageName, event.getClassName().toString());

			if (tryGetActivity(cName) != null)
				if (isCapableClass(event.getClassName().toString()) && !foregroundPackageName.equals(currentHomePackage)) {
					foregroundClassName = event.getClassName().toString();
				}
			Log.e(TAG, foregroundClassName);

			if (!isHandlerRunning && !isMaskOn) {
				if (currentRuleId != -1) {
					if (rulesList.get(currentRuleId).getType() == 0)
						if (foregroundPackageName.equals(rulesList.get(currentRuleId).getFilter().packageName) && !foregroundPackageName.equals("")) {
							if (foregroundClassName.equals(rulesList.get(currentRuleId).getFilter().activityName)) {
								maskSet(rulesList.get(currentRuleId).getFilter(), currentRuleId);
								Log.e(TAG, "Recover");
							}
						}
				} else {
					isHandlerRunning = true;
					new Handler().postDelayed(() -> {
						for (int i = 0; i < rulesList.size(); i++) {
							if (rulesList.get(i).getStatus()) {
								if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName) && !foregroundPackageName.equals("")) {
									if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
										currentRuleId = i;
										Log.e(TAG, "currentRuleId: " + currentRuleId);
										if (rulesList.get(i).getType() == 0)
											maskSet(rulesList.get(i).getFilter(), i);
										else if (rulesList.get(i).getType() == 1)
											wordFinder(getRootInActiveWindow(), true);
										break;
									}
								}
							}
						}
						isHandlerRunning = false;
					}, 250);
				}
			}
		}
	}

	private int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	private boolean isCapableClass(String className) {
		if (className == null) return false;
		else if (className.contains("Activity")) return true;
		else if (className.startsWith("android.widget.") || className.startsWith("android.view.") || className.startsWith("androidx."))
			return false;
		else return true;
	}

	private Rect nodeSearcher(ArrayList<Integer> indices) {
		try {
			AccessibilityNodeInfo node = getRootInActiveWindow();
			for (int i = 0; i < indices.size(); i++) {
				node = node.getChild(indices.get(i));
			}

			Rect rect = new Rect();
			node.getBoundsInScreen(rect);
			return rect;
		} catch (Exception e) {
			Log.e(TAG, String.valueOf(e));
		}

		return null;
	}

	private void aidTextTriggerExecutor() {
		performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
		Toast.makeText(jianfou.getAppContext(), "文字触发补救：强制返回", Toast.LENGTH_SHORT).show();
	}

	private void skipTextExecutor() {
		Log.e(TAG, "skip");
		if (isMaskOn) maskCreator(false, currentRuleId, true);
	}

	public void wordFinder(AccessibilityNodeInfo info, boolean isOnlyVisible) {
		if (isServiceRunning) {
			if (currentRuleId != -1 && info != null) {
				if (info.getChildCount() != 0) {
					for (int i = 0; i < info.getChildCount(); i++) {
						if (info.getChild(i) != null) {
							wordFinder(info.getChild(i), isOnlyVisible);
						}
					}
				} else if (!isOnlyVisible || info.isVisibleToUser()) {
					if (info.getText() != null) {
						if (info.getText().toString().equals(rulesList.get(currentRuleId).getAidText()))
							aidTextTriggerExecutor();
						else if (info.getText().toString().equals(rulesList.get(currentRuleId).getSkipText()))
							skipTextExecutor();
					} else if (info.getContentDescription() != null) {
						if (info.getContentDescription().equals(rulesList.get(currentRuleId).getAidText()))
							aidTextTriggerExecutor();
						else if (info.getContentDescription().equals(rulesList.get(currentRuleId).getSkipText()))
							skipTextExecutor();
					}
				}
			}
		}
	}

	private ActivityInfo tryGetActivity(ComponentName componentName) {
		try {
			return getPackageManager().getActivityInfo(componentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	private void maskSet(WidgetInfo p, int indice) {
		//Rect rect = nodeSearcher();
		if (isServiceRunning) {
			Rect rect = nodeSearcher(p.indices);
			if (rect != null) {
				x = rect.centerX() - rect.width() / 2;// buffer is for edge gesture
				try {
					y = rect.centerY() - rect.height() / 2 - getStatusBarHeight(getBaseContext());//fix status bar's effect
				} catch (Exception e) {
					Log.e(TAG, "Base context catching failed.");
					y = rect.centerY() - rect.height() / 2;
				}
				width = rect.width();//buffer (2x) also for edge gesture
				height = rect.height();
			}

			Log.e(TAG, x + " " + y + " " + width + " " + height + " ");

			if (!isMaskOn) maskCreator(true, indice, false);
			else maskPositionMover();
		}
	}

	private void maskPositionMover() {
		if (isServiceRunning) {
			Log.w(TAG, "Mask target moved.");

			this.mWindowManager.updateView(x, y, width, height);
		}
	}

	private void maskCreator(boolean shouldCreate, int indice, boolean isSkipping) {
		if (!hasJustSkipped)
			if (shouldCreate) {
				Log.w(TAG, "Find mask target.");
				isMaskOn = true;
				currentRuleId = indice;
				this.mWindowManager.addView(x, y, width, height);
			} else {
				Log.w(TAG, "Mask target was destroyed.");
				if (indice == -1) currentRuleId = -1;
				this.mWindowManager.removeView();
				if (isSkipping && isMaskOn) {
					hasJustSkipped = true;
					new Handler().postDelayed(() -> {
						hasJustSkipped = false;
					}, 800);
				}
				isMaskOn = false;
			}
	}

	@Override
	public void onInterrupt() {
		mService = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isReceiverRegistered) {
			this.unregisterReceiver(mScreenOReceiver);
			isReceiverRegistered = false;
		}
		if (isForegroundServiceRunning) {
			stopForeground(true);
			isForegroundServiceRunning = false;
		}
		mService = null;
	}
}

