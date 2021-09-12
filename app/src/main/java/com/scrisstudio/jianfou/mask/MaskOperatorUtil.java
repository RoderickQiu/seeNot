package com.scrisstudio.jianfou.mask;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.inflater;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.layoutParams;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.le;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.mFloatingViews;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.mService;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.mWindowManager;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.sendSimpleNotification;

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.jianfou;

public class MaskOperatorUtil {
	public static void addView(int x, int y, int width, int height, int maskId) {
		layoutParams.x = x;
		layoutParams.y = y;
		layoutParams.width = width;
		layoutParams.height = height;
		if (mFloatingViews.size() <= maskId) {
			try {
				FrameLayout frameLayout = new FrameLayout(mService);
				View view = inflater.inflate(R.layout.layout_floating, frameLayout);
				view.findViewById(R.id.floating).setBackgroundColor(Color.parseColor("#F7F7F7"));
				((TextView) view.findViewById(R.id.floating_text)).setTextColor(Color.parseColor("#DF850D"));
				view.setLayoutParams(layoutParams);
				mFloatingViews.add(view);
				changeForDarkMode(mFloatingViews.size() - 1);
				mWindowManager.addView(mFloatingViews.get(maskId), layoutParams);
			} catch (Exception e) {
				le("add failed" + e.getLocalizedMessage());
			}
		} else {
			try {
				View view = mFloatingViews.get(maskId);
				view.setLayoutParams(layoutParams);
				mFloatingViews.set(maskId, view);
				mWindowManager.addView(mFloatingViews.get(maskId), layoutParams);
			} catch (Exception e) {
				le("show failed" + e.getLocalizedMessage());
			}
		}
	}

	public static void updateView(int x, int y, int width, int height, int maskId) {
		try {
			layoutParams.x = x;
			layoutParams.y = y;
			layoutParams.width = width;
			layoutParams.height = height;
			mFloatingViews.get(maskId).setLayoutParams(layoutParams);
			mWindowManager.updateViewLayout(mFloatingViews.get(maskId), layoutParams);
		} catch (Exception e) {
			le("update failed" + e.getLocalizedMessage());
		}
	}

	public static void hideView(int maskId) {
		try {
			View view = mFloatingViews.get(maskId);
			if (view != null) mWindowManager.removeView(view);
		} catch (Exception e) {
			le("hide failed" + e.getLocalizedMessage());
		}
	}

	public static void removeViews() {
		try {
			for (int i = 0; i < mFloatingViews.size(); i++) {
				View view = mFloatingViews.get(i);
				if (view != null) {
					mWindowManager.removeViewImmediate(view);
				}
			}
			mFloatingViews.clear();
		} catch (Exception e) {
			le("remove failed" + e.getLocalizedMessage());
			Toast.makeText(jianfou.getAppContext(), "消除遮罩失败，如果遮罩一直错误地存在，烦请手动前往系统无障碍设置重启见否服务", Toast.LENGTH_LONG).show();
			sendSimpleNotification("见否可能出了个错？", "消除遮罩失败，如果遮罩一直错误地存在，烦请手动前往系统无障碍设置重启见否服务");
		}
	}

	public static void changeForDarkMode(int maskId) {
		LinearLayout floatingLinear = mFloatingViews.get(maskId).findViewById(R.id.floating);
		if (ActivitySeekerService.isNightMode(jianfou.getAppContext())) {
			floatingLinear.findViewById(R.id.floating).setBackgroundColor(Color.parseColor("#1B1B1B"));
			((TextView) floatingLinear.findViewById(R.id.floating_text)).setTextColor(Color.parseColor("#E8D21B"));
		} else {
			floatingLinear.findViewById(R.id.floating).setBackgroundColor(Color.parseColor("#F7F7F7"));
			((TextView) floatingLinear.findViewById(R.id.floating_text)).setTextColor(Color.parseColor("#DF850D"));
		}
	}
}
