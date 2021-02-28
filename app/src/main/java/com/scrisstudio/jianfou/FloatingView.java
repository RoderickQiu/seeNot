package com.scrisstudio.jianfou;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingView extends LinearLayout {
	public static final String TAG = "FloatingView";
	private final Context mContext;
	private final WindowManager mWindowManager;

	public FloatingView(Context context) {
		super(context);
		this.mContext = context;
		this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		initView();
	}

	private void initView() {
		View view = inflate(this.mContext, R.layout.layout_floating, this);
		TypedValue colorOnSecondary = new TypedValue(), colorSecondary = new TypedValue();
		Resources.Theme theme = MainActivity.theme;
		try {
			theme.resolveAttribute(R.attr.colorOnSecondary, colorOnSecondary, true);
			theme.resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
			view.findViewById(R.id.floating).setBackgroundColor(getResources().getColor(colorOnSecondary.resourceId, theme));
			((TextView) view.findViewById(R.id.floating_text)).setTextColor(getResources().getColor(colorSecondary.resourceId, theme));
		} catch (Exception e) {}
	}

	/* access modifiers changed from: protected */
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	/* access modifiers changed from: protected */
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				Log.d(TAG, "Fuck");
				break;
		}
		return true;
	}
}
