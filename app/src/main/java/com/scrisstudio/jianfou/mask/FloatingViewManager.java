package com.scrisstudio.jianfou.mask;

import static com.scrisstudio.jianfou.mask.ActivitySeekerService.le;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.jianfou;

import java.util.ArrayList;

public class FloatingViewManager {
	private static final WindowManager.LayoutParams layoutParams;

	static {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.x = 0;
		params.y = 0;
		params.width = -2;
		params.height = -2;
		params.gravity = 51;
		params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		params.format = 1;
		params.flags = 40;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
		}
		layoutParams = params;
	}

	private final Context mContext;
	private final WindowManager mWindowManager;
	private ArrayList<View> mFloatingViews = new ArrayList<>();

	public FloatingViewManager(Context context) {
		this.mContext = context;
		this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	public void addView(int x, int y, int width, int height, int maskId) {
		layoutParams.x = x;
		layoutParams.y = y;
		layoutParams.width = width;
		layoutParams.height = height;
		if (this.mFloatingViews.size() <= maskId) {
			try {
				FloatingView floatingView = new FloatingView(this.mContext);
				floatingView.setLayoutParams(layoutParams);
				this.mFloatingViews.add(floatingView);
				this.mWindowManager.addView(this.mFloatingViews.get(maskId), layoutParams);
			} catch (Exception e) {
				le("add failed" + e.getLocalizedMessage());
			}
		} else {
			try {
				FloatingView floatingView = (FloatingView) this.mFloatingViews.get(maskId);
				floatingView.setLayoutParams(layoutParams);
				this.mFloatingViews.set(maskId, floatingView);
				this.mWindowManager.addView(this.mFloatingViews.get(maskId), layoutParams);
			} catch (Exception e) {
				le("show failed" + e.getLocalizedMessage());
			}
		}
	}

	public void updateView(int x, int y, int width, int height, int maskId) {
		try {
			layoutParams.x = x;
			layoutParams.y = y;
			layoutParams.width = width;
			layoutParams.height = height;
			mFloatingViews.get(maskId).setLayoutParams(layoutParams);
			this.mWindowManager.updateViewLayout(mFloatingViews.get(maskId), layoutParams);
		} catch (Exception e) {
			le("update failed" + e.getLocalizedMessage());
		}
	}

	public void hideView(int maskId) {
		try {
			View view = this.mFloatingViews.get(maskId);
			if (view != null) this.mWindowManager.removeView(view);
		} catch (Exception e) {
			le("hide failed" + e.getLocalizedMessage());
		}
	}

	public void removeViews() {
		try {
			for (int i = 0; i < mFloatingViews.size(); i++) {
				View view = this.mFloatingViews.get(i);
				if (view != null) {
					this.mWindowManager.removeView(view);
				}
			}
			mFloatingViews.clear();
		} catch (Exception e) {
			le("remove failed" + e.getLocalizedMessage());
		}
	}

	public void changeForDarkMode(int maskId) {
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
