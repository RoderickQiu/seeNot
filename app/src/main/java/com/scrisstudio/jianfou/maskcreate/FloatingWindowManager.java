package com.scrisstudio.jianfou.maskcreate;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.scrisstudio.jianfou.R;

public class FloatingWindowManager {
	private static final WindowManager.LayoutParams layoutParams;

	static {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.x = 0;
		params.y = 0;
		params.width = -2;
		params.height = -2;
		params.gravity = 51;
		params.type = 2032;
		params.format = 1;
		params.flags = 40;
		params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
		layoutParams = params;
	}

	private final Context mContext;
	private final WindowManager mWindowManager;
	private View mFloatingView;

	public FloatingWindowManager(Context context) {
		this.mContext = context;
		this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	public void addView(int x, int y, int width, int height) {
		if (this.mFloatingView == null) {
			FloatingView floatingView = new FloatingView(this.mContext);
			this.mFloatingView = floatingView;
			layoutParams.x = x;
			layoutParams.y = y;
			floatingView.setLayoutParams(layoutParams);
			this.mWindowManager.addView(this.mFloatingView, layoutParams);

			LinearLayout floatingLinear = mFloatingView.findViewById(R.id.floating);
			ViewGroup.LayoutParams linearParams;
			linearParams = floatingLinear.getLayoutParams();
			linearParams.width = width;
			linearParams.height = height;
			floatingLinear.setLayoutParams(linearParams);
		}
	}

	public void updateView(int x, int y, int width, int height) {
		if (mFloatingView != null) {
			layoutParams.x = x;
			layoutParams.y = y;
			mFloatingView.setLayoutParams(layoutParams);
			this.mWindowManager.updateViewLayout(mFloatingView, layoutParams);

			LinearLayout floatingLinear = mFloatingView.findViewById(R.id.floating);
			ViewGroup.LayoutParams linearParams;
			linearParams = floatingLinear.getLayoutParams();
			linearParams.width = width;
			linearParams.height = height;
			floatingLinear.setLayoutParams(linearParams);
		}
	}

	public void removeView() {
		View view = this.mFloatingView;
		if (view != null) {
			this.mWindowManager.removeView(view);
			this.mFloatingView = null;
		}
	}
}
