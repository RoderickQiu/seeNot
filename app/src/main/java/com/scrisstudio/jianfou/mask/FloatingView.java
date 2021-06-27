package com.scrisstudio.jianfou.mask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scrisstudio.jianfou.R;

public class FloatingView extends LinearLayout {
	private final Context mContext;

	public FloatingView(Context context) {
		super(context);
		this.mContext = context;
		initView();
	}

	private void initView() {
		View view = inflate(this.mContext, R.layout.layout_floating, this);
		view.findViewById(R.id.floating).setBackgroundColor(Color.parseColor("#F7F7F7"));
		((TextView) view.findViewById(R.id.floating_text)).setTextColor(Color.parseColor("#DF850D"));
	}

	/* access modifiers changed from: protected */
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	/* access modifiers changed from: protected */
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				break;
		}
		return true;
	}
}
