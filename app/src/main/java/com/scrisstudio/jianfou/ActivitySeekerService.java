package com.scrisstudio.jianfou;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class ActivitySeekerService extends AccessibilityService {
	public static ActivitySeekerService mService;
	private final String TAG = "Jianfou-AccessibilityService";

	public static boolean isStart() {
		return mService != null;
	}

	//初始化
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Log.e(TAG, "Started");
		mService = this;
	}

	//实现辅助功能
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

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

