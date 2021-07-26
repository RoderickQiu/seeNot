package com.scrisstudio.jianfou.mask;

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
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
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
import com.scrisstudio.jianfou.activity.PIPShieldActivity;
import com.scrisstudio.jianfou.activity.PermissionGrantActivity;
import com.scrisstudio.jianfou.jianfou;

import java.util.ArrayList;
import java.util.List;

public class ActivitySeekerService extends AccessibilityService {
	public static final String TAG = "Jianfou-AccessibilityService";
	private static final String CHANNEL_SERVICE_KEEPER_ID = "ServiceKeeper", CHANNEL_NORMAL_NOTIFICATION_ID = "NormalNotification";
	private static final int KEEPER_NOTIFICATION_ID = 9221, NORMAL_NOTIFICATION_ID = 9311;
	public static ActivitySeekerService mService;
	public static List<RuleInfo> rulesList;
	public static boolean isServiceRunning = true, isFirstTimeInvokeService = true,
			isReceiverRegistered = false, isForegroundServiceRunning = false,
			isSoftInputPanelOn = false, hasSoftInputPanelJustFound = false, isSkipping = false,
			isMaskOn = false, isSplitScreenAcceptable = false, isDarkModeOn = false;
	public static int foregroundWindowId = 0, foregroundWindowLayer = 1;
	public static String foregroundClassName = "", foregroundPackageName = "com.scrisstudio.jianfou", currentHomePackage = "";
	private static String windowOrientation = "portrait";
	private static int windowTrueWidth, windowTrueHeight;
	private static RuleInfo currentRule;
	private final Handler mHandler = new Handler();
	public NotificationChannel normalNotificationChannel;
	public NotificationManager normalNotificationManager;
	private long lastContentChangedTime = 0, lastHandlerRunningTime = 0, contentChangeTime = 0, handlerTime = 0;
	private FloatingViewManager mWindowManager;
	private Rect contentRect = new Rect(), nodeSearcherRect = new Rect(), dynamicRect = new Rect();
	private int x = -1, y = -1, width = -1, height = -1, currentRuleId = -1;
	private final BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals("android.intent.action.SCREEN_OFF")) {
				le("Screen Off");
				try {
					if (isMaskOn) maskCreator(false, -1, 0);
				} catch (Exception e) {
					le("Failed to remove mask");
				}
			}
		}

	};

	public static boolean isStart() {
		return mService != null;
	}

	public static boolean isNightMode(Context context) {
		int currentNightMode = context.getResources().getConfiguration().uiMode &
				Configuration.UI_MODE_NIGHT_MASK;
		return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
	}

	public static void setRulesList(List<RuleInfo> l) {
		rulesList = l;
	}

	public static void setServiceBasicInfo(String rules, Boolean masterSwitch, Boolean split) {
		Gson gson = new Gson();
		rulesList = gson.fromJson(rules, new TypeToken<List<RuleInfo>>() {
		}.getType());
		isServiceRunning = masterSwitch;
		isSplitScreenAcceptable = !split;
	}

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

		mHandler.postDelayed(() -> {
			try {
				if (!isStart()) {
					Toast.makeText(jianfou.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
					le("Service invoking didn't respond, a manual start might be needed.");

					sendSimpleNotification("见否服务未开启", "见否服务没有成功开启，请前往系统无障碍设置手动开启");
				}
			} catch (Exception ignored) {
			}
		}, 2000);
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

	public void sendSimpleNotification(String title, String content) {
		Intent intent = new Intent(this, PermissionGrantActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_NORMAL_NOTIFICATION_ID)
				.setSmallIcon(R.drawable.jianfou_no_bg)
				.setContentTitle(title)
				.setContentText(content)
				.setContentIntent(pi)
				.setAutoCancel(true)
				.setShowWhen(true);
		normalNotificationManager.notify(NORMAL_NOTIFICATION_ID, nb.build());
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public void setForegroundService() {
		String channelName = getString(R.string.channel_name);
		int importance = NotificationManager.IMPORTANCE_LOW;
		NotificationChannel channel = new NotificationChannel(CHANNEL_SERVICE_KEEPER_ID, channelName, importance);
		channel.setDescription(getString(R.string.channel_description));
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_SERVICE_KEEPER_ID);
		builder.setSmallIcon(R.drawable.jianfou_no_bg)
				.setContentTitle(getString(R.string.channel_notification_text))
				.setContentText("为防止服务被杀，必须显示一个通知")
				.setOngoing(true);
		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.createNotificationChannel(channel);
		startForeground(KEEPER_NOTIFICATION_ID, builder.build());
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

			l("Service invoking...");

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
			} catch (Exception e) {
				le("Monitoring locker failed, err message: " + e.toString());
			}

			try {
				setForegroundService();
			} catch (Exception e) {
				Toast.makeText(jianfou.getAppContext(), R.string.service_start_failed, Toast.LENGTH_LONG).show();
				le("Starting foreground service failed, err message: " + e.toString());
			}
		}
	}

	//实现辅助功能
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			if (currentRuleId != -1 && isServiceRunning) {
				if (!hasSoftInputPanelJustFound && isSoftInputPanelOn) {
					isSoftInputPanelOn = false;
					l("Input method closed.");
					multiMaskSet();
				}
			}

			try {
				foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
			} catch (Exception e) {
				foregroundPackageName = event.getPackageName().toString();
			}
			ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);
			if (currentRuleId != -1) {
				if (foregroundPackageName.equals(currentHomePackage) || foregroundPackageName.equals("com.scrisstudio.jianfou")) {
					if (isMaskOn) maskCreator(false, -1, 0);
					currentRuleId = -1;
					currentRule = null;
					isSkipping = false;
					return;
				} else if (foregroundPackageName.equals(currentRule.getFilter().get(0).packageName) && !foregroundPackageName.equals("")) {
					if (tryGetActivity(cName) != null) {
						if (isCapableClass(foregroundClassName)) {
							if (!foregroundClassName.equals(currentRule.getFilter().get(0).activityName)) {
								if (isMaskOn) maskCreator(false, -1, 0);
								currentRuleId = -1;
								currentRule = null;
								isSkipping = false;
							} else {
								//prevent a lot of events flood in to cause crash
								contentChangeTime = SystemClock.uptimeMillis();
								if (contentChangeTime - lastContentChangedTime >= 128) {
									lastContentChangedTime = contentChangeTime;

									//can't find skip text, stop being skipping
									if (!wordFinder(getRootInActiveWindow(), true)) {
										isSkipping = false;
										if (currentRule.getType() == 2)
											maskCreator(false, currentRuleId, 0);

									}

									try {
										event.getSource().getBoundsInScreen(contentRect);
										if (isMaskOn && !isSkipping) {
											if (contentRect.width() == windowTrueWidth) {
												if (windowOrientation.equals("landscape")) {
													windowOrientation = "portrait";
													l("Change to Portrait");
													if (foregroundPackageName.equals(currentRule.getFilter().get(0).packageName) && !foregroundPackageName.equals("")) {
														if (foregroundClassName.equals(currentRule.getFilter().get(0).activityName)) {
															multiMaskSet();
															l("Recover");
														}
													}
												}
											} else if (contentRect.width() == windowTrueHeight) {
												if (windowOrientation.equals("portrait")) {
													windowOrientation = "landscape";
													l("Change to Landscape");
													if (foregroundPackageName.equals(currentRule.getFilter().get(0).packageName) && !foregroundPackageName.equals("")) {
														if (foregroundClassName.equals(currentRule.getFilter().get(0).activityName)) {
															multiMaskSet();
															l("Recover");
														}
													}
												}
											}
										}
									} catch (Exception e) {
										le("Failed to get orientation, err message: " + e.toString());
									}
								}
							}
						}
					}
				}
			}

			//移到此处以解决快速切换的问题
			if (!isMaskOn && !isSoftInputPanelOn && !isSkipping && isServiceRunning) {
				handlerTime = SystemClock.uptimeMillis();
				if (handlerTime - lastHandlerRunningTime >= 128) {
					lastHandlerRunningTime = handlerTime;
					if (currentRuleId == -1)
						for (int i = 0; i < rulesList.size(); i++) {
							try {
								if (rulesList.get(i).getStatus()) {
									if (foregroundPackageName.equals(rulesList.get(i).getFilter().get(0).packageName) && !foregroundPackageName.equals("")) {
										if (foregroundClassName.equals(rulesList.get(i).getFilter().get(0).activityName)) {
											currentRuleId = i;
											currentRule = rulesList.get(currentRuleId);
											l("currentRuleId: " + currentRuleId);
											if (rulesList.get(i).getType() == 0)
												multiMaskSet();
											else if (rulesList.get(i).getType() == 1 || rulesList.get(i).getType() == 2)
												wordFinder(getRootInActiveWindow(), true);
											break;
										}
									}
								}
							} catch (Exception e) {
								le("Bad rule " + e.getLocalizedMessage());
							}
						}
					else if (currentRule.getType() == 0)
						multiMaskSet();
				}
			}

			if (isServiceRunning) {
				hasSoftInputPanelJustFound = false;
				for (int i = 0; i < getWindows().size(); i++) {
					//通过查找window的layer属性发现是否处于分屏、小窗
					if (getWindows().get(i).getId() == foregroundWindowId && getWindows().get(i).isActive()) {
						foregroundWindowLayer = getWindows().get(i).getLayer();
						if (getWindows().get(i).isInPictureInPictureMode()) {//检测画中画并强退之
							startActivity(new Intent(this, PIPShieldActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
							Toast.makeText(jianfou.getAppContext(), "分屏屏蔽打开：当前禁止打开画中画窗口", Toast.LENGTH_SHORT).show();
						} else if (foregroundWindowLayer != 0 && getWindows().get(i).getType() == 1) {//只选择常规窗口，不选择输入法等特殊窗口
							performGlobalAction(GLOBAL_ACTION_BACK);
							Toast.makeText(jianfou.getAppContext(), "分屏屏蔽打开：禁止分屏和小窗", Toast.LENGTH_SHORT).show();
						}
					}
					if (currentRuleId != -1) {
						try {
							//查找输入法是否打开，打开则删除遮罩：TYPE_INPUT_METHOD=2
							if (getWindows().get(i).getType() == 2) {
								hasSoftInputPanelJustFound = true;
								if (!isSoftInputPanelOn) {
									isSoftInputPanelOn = true;
									l("Input method is enabled.");
									maskCreator(false, currentRuleId, 0);
								}
							}
						} catch (Exception ignored) {
						}
					}
				}
			}
		} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			if (isCapableClass(event.getClassName().toString()) && !event.getPackageName().equals(currentHomePackage)) {
				foregroundClassName = event.getClassName().toString();
				foregroundWindowId = event.getWindowId();
				l(event.getClassName().toString() + event.getWindowId());

				if (currentRule != null)
					if (!foregroundClassName.equals(currentRule.getFilter().get(0).activityName)) {
						if (isMaskOn) maskCreator(false, -1, 0);
						currentRuleId = -1;
						currentRule = null;
						isSkipping = false;
					}
			}

			if ((isNightMode(jianfou.getAppContext()) && !isDarkModeOn) || (!isNightMode(jianfou.getAppContext()) && isDarkModeOn)) {
				isDarkModeOn = !isDarkModeOn;
				if (isMaskOn) {
					if (currentRule.getType() == 2)
						maskThemeChanger(0);
					else {
						for (int i = 0; i < currentRule.getFilterLength(); i++) {
							maskThemeChanger(i);
						}
					}
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
		else
			return !className.startsWith("android.widget.") && !className.startsWith("android.view.") && !className.startsWith("androidx.");
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

	private void aidTextTriggerExecutor() {
		performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
		Toast.makeText(jianfou.getAppContext(), "文字触发补救：强制返回", Toast.LENGTH_SHORT).show();
	}

	private void skipTextExecutor() {
		if (isMaskOn) {
			//when skipping, cannot create masks
			isSkipping = true;

			l("Skipping now.");
			maskCreator(false, currentRuleId, 0);
		}
	}

	private void dynamicTextExecutor(AccessibilityNodeInfo info) {
		for (int i = 0; i < currentRule.getDynamicParentLevel().get(0); i++) {
			info = info.getParent();
		}
		info.getBoundsInScreen(dynamicRect);
		maskSet(dynamicRect, currentRuleId, 0);
		isMaskOn = true;
	}

	public boolean wordFinder(AccessibilityNodeInfo info, boolean isOnlyVisible) {
		if (isServiceRunning) {
			boolean hasExecutionSucceeded = false;
			if (currentRuleId != -1 && info != null) {
				if (info.getChildCount() != 0) {
					for (int i = 0; i < info.getChildCount(); i++) {
						if (info.getChild(i) != null) {
							if (wordFinder(info.getChild(i), isOnlyVisible)) {
								hasExecutionSucceeded = true;
							}
						}
					}
				} else if (!isOnlyVisible || info.isVisibleToUser()) {
					if (info.getText() != null) {
						if (info.getText().toString().equals(currentRule.getAidText())) {
							aidTextTriggerExecutor();
							hasExecutionSucceeded = true;
						} else if (info.getText().toString().equals(currentRule.getSkipText())) {
							skipTextExecutor();
							hasExecutionSucceeded = true;
						} else if (info.getText().toString().equals(currentRule.getDynamicText().get(0))) {
							dynamicTextExecutor(info);
							hasExecutionSucceeded = true;
						}
					} else if (info.getContentDescription() != null) {
						if (info.getContentDescription().equals(currentRule.getAidText())) {
							aidTextTriggerExecutor();
							hasExecutionSucceeded = true;
						} else if (info.getContentDescription().equals(currentRule.getSkipText())) {
							skipTextExecutor();
							hasExecutionSucceeded = true;
						} else if (info.getContentDescription().toString().equals(currentRule.getDynamicText().get(0))) {
							dynamicTextExecutor(info);
							hasExecutionSucceeded = true;
						}
					}
				}
			}
			return hasExecutionSucceeded;
		}
		return false;
	}

	private ActivityInfo tryGetActivity(ComponentName componentName) {
		try {
			return getPackageManager().getActivityInfo(componentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	private void multiMaskSet() {
		try {
			boolean flag = false;
			for (int i = 0; i < currentRule.getFilterLength(); i++) {
				if (maskSet(nodeSearcher(currentRule.getFilter().get(i).indices), currentRuleId, i))
					flag = true;
			}
			if (flag) isMaskOn = true;
			l("Multi multi");
		} catch (Exception e) {
			le(e.getMessage());
		}
	}

	private boolean maskSet(Rect rect, int indice, int maskId) {
		if (isServiceRunning && !isSkipping) {
			if (rect != null) {
				x = rect.centerX() - rect.width() / 2;
				try {
					y = rect.centerY() - rect.height() / 2 - getStatusBarHeight(getBaseContext());//fix status bar's effect
				} catch (Exception e) {
					le("Base context catching failed.");
					y = rect.centerY() - rect.height() / 2;
				}
				width = rect.width();
				height = rect.height();

				le(x + " " + y + " " + width + " " + height + " ");

				if (x == -1 || y == -1 || width == -1 || height == -1) {
					le("Item rect wrong.");
					return false;
				} else {
					if (!isMaskOn) maskCreator(true, indice, maskId);
					else maskPositionMover(maskId);
					return true;
				}
			} else {
				le("Item rect wrong.");
				return false;
				//maskCreator(false, -1);
			}
		}
		return false;
	}

	private void maskThemeChanger(int maskId) {
		try {
			if (isServiceRunning) {
				this.mWindowManager.changeForDarkMode(maskId);
			}
		} catch (Exception ignored) {
		}
	}

	private void maskPositionMover(int maskId) {
		try {
			if (isServiceRunning) {
				l("Mask target moved.");
				this.mWindowManager.updateView(x, y, width, height, maskId);
			}
		} catch (Exception ignored) {
		}
	}

	private void maskCreator(boolean shouldCreate, int indice, int maskId) {
		if (shouldCreate) {
			l("Find mask target.");
			this.mWindowManager.addView(x, y, width, height, maskId);
		} else if (isMaskOn) {
			l("Mask target was destroyed.");
			if (indice == -1) {
				currentRuleId = -1;
				currentRule = null;
			}
			this.mWindowManager.removeViews();
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

