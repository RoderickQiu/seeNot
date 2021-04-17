package com.scrisstudio.jianfou.mask;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.ui.RuleInfo;

import java.util.ArrayList;
import java.util.List;

public class ActivitySeekerService extends AccessibilityService {
	public static final String TAG = "Jianfou-AccessibilityService";
	public static ActivitySeekerService mService;
	public static List<RuleInfo> rulesList;
	public static boolean isServiceRunning = false;
	public static String foregroundClassName = "", foregroundPackageName = "";
	public static boolean isFirstTimeInvokeService = true;
	private static String windowOrientation = "portrait";
	private FloatingWindowManager mWindowManager;
	private boolean isMaskOn = false;
	private int x = -1, y = -1, width = -1, height = -1, currentRuleId = -1;
	private final BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals("android.intent.action.SCREEN_OFF")) {
				Log.e(TAG, "Screen Off");
				try {
					if (isMaskOn) maskCreator(false, -1);
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

	//初始化
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Log.e(TAG, "Started");
		mService = this;

		if (this.mWindowManager == null) {
			this.mWindowManager = new FloatingWindowManager(this);
		}

		if (isFirstTimeInvokeService) {
			isFirstTimeInvokeService = false;
			rulesList = new ArrayList<>();
			isServiceRunning = false;

			/* 注册机器锁屏时的广播 */
			IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
			this.registerReceiver(mScreenOReceiver, mScreenOffFilter);
		}
	}

	//实现辅助功能
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			try {
				Rect rect = new Rect();
				event.getSource().getBoundsInScreen(rect);
				if (rect.width() == MainActivity.windowTrueWidth) {
					if (windowOrientation.equals("landscape")) {
						windowOrientation = "portrait";
						Log.e(TAG, "Change to Portrait");
						if (isMaskOn)
							for (int i = 0; i < rulesList.size(); i++) {
								if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName)) {
									if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
										maskSet(rulesList.get(i).getFilter(), i, true);
										break;
									}
								}
							}
					}
				} else if (rect.width() == MainActivity.windowTrueHeight) {
					if (windowOrientation.equals("portrait")) {
						windowOrientation = "landscape";
						Log.e(TAG, "Change to Landscape");
						if (isMaskOn)
							for (int i = 0; i < rulesList.size(); i++) {
								if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName)) {
									if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
										maskSet(rulesList.get(i).getFilter(), i, true);
										break;
									}
								}
							}
					}
				}
			} catch (Exception e) {
				Log.i(TAG, "Failed to get orientation");
			}
			try {
				foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
			} catch (Exception e) {
				foregroundPackageName = event.getPackageName().toString();
			}
			ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);
			//Best solution til now
			if (!foregroundPackageName.equals(MainActivity.currentHomePackage)) {
				wordFinder(getRootInActiveWindow(), true);

				for (int i = 0; i < rulesList.size(); i++) {
					if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName)) {
						if (tryGetActivity(cName) != null) {
							if (isCapableClass(foregroundClassName)) {
								if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
									//maskSet();
									break;
								} else {
									if (isMaskOn) maskCreator(false, -1);
									currentRuleId = -1;
								}
							}
						}
					}
				}
			} else if (isMaskOn) maskCreator(false, -1);
		} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ComponentName cName = new ComponentName(foregroundPackageName, event.getClassName().toString());

			if (tryGetActivity(cName) != null)
				if (isCapableClass(event.getClassName().toString()) && !foregroundPackageName.equals(MainActivity.currentHomePackage)) {
					foregroundClassName = event.getClassName().toString();
				}
			Log.e(TAG, foregroundClassName);
			for (int i = 0; i < rulesList.size(); i++) {
				if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName) && !foregroundPackageName.equals("")) {
					if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
						maskSet(rulesList.get(i).getFilter(), i, false);
						currentRuleId = i;
						break;
					}
				}
			}
			//}
			//if (foregroundPackageName.equals(MainActivity.currentHomePackage))
			//	if (isMaskOn) maskCreator(false);
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
						if (info.getText().equals(rulesList.get(currentRuleId).getAidText())) {
							performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
							Log.e(TAG, "Perform Back: " + info.getText());
							Toast.makeText(jianfou.getAppContext(), "文字触发补救：强制返回", Toast.LENGTH_LONG).show();
						}
					} else if (info.getContentDescription() != null) {
						Log.e(TAG, "Perform Back: " + info.getContentDescription());
						if (info.getContentDescription().equals(rulesList.get(currentRuleId).getAidText())) {
							performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
							Toast.makeText(jianfou.getAppContext(), "文字触发补救：强制返回", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
		}
	}

	private ActivityInfo tryGetActivity(ComponentName componentName) {
		try {
			Log.w(TAG, "Caught: " + componentName);
			return getPackageManager().getActivityInfo(componentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, "Problem catching: " + componentName);
			return null;
		}
	}

	private void maskSet(PackageWidgetDescription p, int indice, boolean shouldMove) {
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

			if (!isMaskOn) maskCreator(true, indice);
			else if (shouldMove) maskPositionMover();
		}
	}

	private void maskPositionMover() {
		if (isServiceRunning) {
			Log.w(TAG, "Mask target moved.");

			this.mWindowManager.updateView(x, y, width, height);
		}
	}

	private void maskCreator(boolean shouldCreate, int indice) {
		if (shouldCreate) {
			Log.w(TAG, "Find mask target.");
			isMaskOn = true;
			currentRuleId = indice;
			this.mWindowManager.addView(x, y, width, height);
		} else {
			Log.w(TAG, "Mask target was destroyed.");
			isMaskOn = false;
			currentRuleId = -1;
			this.mWindowManager.removeView();
		}
	}

	@Override
	public void onInterrupt() {
		mService = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mScreenOReceiver);
		mService = null;
	}
}

