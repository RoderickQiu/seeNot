package com.scrisstudio.jianfou.maskcreate;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ActivitySeekerService extends AccessibilityService {
	public static ActivitySeekerService mService;
	private final String TAG = "Jianfou-AccessibilityService";
	FloatingWindowManager mWindowManager;
	private boolean isMaskOn = false, isWordFound = false;
	private String foregroundClassName = new String(), foregroundPackageName = new String();

	private int x = -1, y = -1, width = -1, height = -1;
	private int defaultX = 0, defaultY = 204 - 72, defaultWidth = 1080, defaultHeight = 1986;
	private int xBuffer = -20, yBuffer = -0, widthBuffer = -20, heightBuffer = -12 + 150;
	private int childLevel = 12 - 1, childSearchStructure[] = {/*0, */0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1};

	public static boolean isStart() {
		return mService != null;
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
	}

	//实现辅助功能
	/*
		只实现了首页，理论上应屏蔽热搜，但和其它项目是同类的
	 */
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			try {
				foregroundPackageName = getRootInActiveWindow().getPackageName().toString();
			} catch (Exception e) {
				foregroundPackageName = event.getPackageName().toString();
			}
			ComponentName cName = new ComponentName(foregroundPackageName, foregroundClassName);

			//Best solution til now
			if (foregroundPackageName.equals("com.zhihu.android")) {
				if (tryGetActivity(cName) != null) {
					if (isCapableClass(foregroundClassName)) {
						if (foregroundClassName.equals("com.zhihu.android.app.ui.activity.MainActivity")) {
							maskSet();
						} else {
							if (isMaskOn) maskCreator(false);
						}
					}
				}
			} else if (isMaskOn) maskCreator(false);
		} else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ComponentName cName = new ComponentName(foregroundPackageName, event.getClassName().toString());
			if (foregroundPackageName.equals("com.zhihu.android")) {
				if (tryGetActivity(cName) != null) {
					if (isCapableClass(event.getClassName().toString()))
						foregroundClassName = event.getClassName().toString();
					Log.e(TAG, foregroundClassName);
					if (foregroundClassName.equals("com.zhihu.android.app.ui.activity.MainActivity")) {
						maskSet();
					} else {
						if (isMaskOn) maskCreator(false);
					}
				}
			} else if (isMaskOn) maskCreator(false);
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
		else if (className.startsWith("android.widget.") || className.startsWith("android.view."))
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

	private void maskSet() {
		Rect rect = nodeSearcher();
		if (rect != null) {
			x = rect.centerX() - rect.width() / 2 - xBuffer;// buffer is for edge gesture
			y = rect.centerY() - rect.height() / 2 - getStatusBarHeight(getBaseContext()) - yBuffer;//fix status bar's effect
			width = rect.width() + widthBuffer + xBuffer;//buffer (2x) also for edge gesture
			height = rect.height() + heightBuffer + yBuffer;
		} else {
			x = defaultX - xBuffer;
			y = defaultY - yBuffer;
			width = defaultWidth + widthBuffer + xBuffer;
			height = defaultHeight + heightBuffer + yBuffer;
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

