package com.scrisstudio.jianfou.mask;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.ui.RuleInfo;

import java.util.List;

public class ActivitySeekerService extends AccessibilityService {
	public static ActivitySeekerService mService;
	public static List<RuleInfo> rulesList;
	public static boolean isServiceRunning;
	public static String foregroundClassName = "", foregroundPackageName = "";
	private final String TAG = "Jianfou-AccessibilityService";
	private FloatingWindowManager mWindowManager;
	private boolean isMaskOn = false, isWordFound = false;
	private int x = -1, y = -1, width = -1, height = -1;
	private int xBuffer = 0, yBuffer = -0, widthBuffer = 0, heightBuffer = 0;
	private int childLevel = 12 - 1, childSearchStructure[] = {/*0, */0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1};

	public static boolean isStart() {
		return mService != null;
	}

	public static void setRulesList(List<RuleInfo> l) {
		rulesList = l;
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

		Gson gson = new Gson();
		rulesList = gson.fromJson(MainActivity.sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {}.getType());
		isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
	}

	//实现辅助功能
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (isServiceRunning) {
			if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
				try {
					foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
				} catch (Exception e) {
					foregroundPackageName = event.getPackageName().toString();
				}
				ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);
				//Best solution til now
				if (!foregroundPackageName.equals(MainActivity.currentHomePackage)) {
					for (int i = 0; i < rulesList.size(); i++) {
						if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName)) {
							if (tryGetActivity(cName) != null) {
								if (isCapableClass(foregroundClassName)) {
									if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
										//maskSet();
										break;
									} else {
										if (isMaskOn) maskCreator(false);
									}
								}
							}
						}
					}
				} else if (isMaskOn) maskCreator(false);
			} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
				ComponentName cName = new ComponentName(foregroundPackageName, event.getClassName().toString());

				if (tryGetActivity(cName) != null) {
					if (isCapableClass(event.getClassName().toString()))
						foregroundClassName = event.getClassName().toString();
					Log.e(TAG, foregroundClassName);
					for (int i = 0; i < rulesList.size(); i++) {
						if (foregroundPackageName.equals(rulesList.get(i).getFilter().packageName)) {
							if (foregroundClassName.equals(rulesList.get(i).getFilter().activityName)) {
								maskSet(rulesList.get(i).getFilter());
								break;
							} else {
								if (isMaskOn) maskCreator(false);
							}
						} else {
							if (isMaskOn) maskCreator(false);
						}
					}
				}
				if (foregroundPackageName.equals(MainActivity.currentHomePackage))
					if (isMaskOn) maskCreator(false);
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

	private Rect nodeSearcher() {
		try {
			AccessibilityNodeInfo node = getRootInActiveWindow();
			for (int i = 0; i < childLevel; i++) {
				node = node.getChild(childSearchStructure[i]);
			}

			Rect rect = new Rect();
			node.getBoundsInScreen(rect);
			return rect;
		} catch (Exception e) {
			Log.e(TAG, String.valueOf(e));
		}

		return null;
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

	private void maskSet(PackageWidgetDescription p) {
		//Rect rect = nodeSearcher();
		Rect rect = p.position;
		if (rect != null) {
			x = rect.centerX() - rect.width() / 2 - xBuffer;// buffer is for edge gesture
			y = rect.centerY() - rect.height() / 2 - getStatusBarHeight(getBaseContext()) - yBuffer;//fix status bar's effect
			width = rect.width() + widthBuffer + xBuffer;//buffer (2x) also for edge gesture
			height = rect.height() + heightBuffer + yBuffer;
		}

		Log.e(TAG, x + " " + y + " " + width + " " + height + " ");

		if (!isMaskOn) maskCreator(true);
		else maskPositionMover();
	}

	private void maskPositionMover() {
		Log.w(TAG, "Mask target moved.");

		this.mWindowManager.updateView(x, y, width, height);
	}

	private void maskCreator(boolean shouldCreate) {
		if (shouldCreate) {
			Log.w(TAG, "Find mask target.");
			isMaskOn = true;

			this.mWindowManager.addView(x, y, width, height);
		} else {
			Log.w(TAG, "Mask target was destroyed.");
			isMaskOn = false;

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
		mService = null;
	}
}
