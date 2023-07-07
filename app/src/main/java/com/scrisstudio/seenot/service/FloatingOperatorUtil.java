package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.inflater;
import static com.scrisstudio.seenot.service.ExecutorService.layoutParams;
import static com.scrisstudio.seenot.service.ExecutorService.mFloatingViews;
import static com.scrisstudio.seenot.service.ExecutorService.mService;
import static com.scrisstudio.seenot.service.ExecutorService.mWindowManager;
import static com.scrisstudio.seenot.service.ExecutorService.sendSimpleNotification;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;

public class FloatingOperatorUtil {
    public static void addView(int x, int y, int width, int height, int maskId) {
        removeViews();

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
                view.getRootView().setOnTouchListener(new View.OnTouchListener() {
                    int x = 0, y = 0;

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                x = Math.round(event.getRawX());
                                y = Math.round(event.getRawY());
                                break;
                            case MotionEvent.ACTION_MOVE:
                                layoutParams.x = Math.round(layoutParams.x + (event.getRawX() - x));
                                layoutParams.y = Math.round(layoutParams.y + (event.getRawY() - y));
                                x = Math.round(event.getRawX());
                                y = Math.round(event.getRawY());
                                mWindowManager.updateViewLayout(v, layoutParams);
                                break;
                        }
                        return true;
                    }
                });
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

    public static void updateText(String str, int maskId) {
        if (mFloatingViews.size() >= maskId) {
            ((TextView) mFloatingViews.get(maskId).findViewById(R.id.floating_text)).setText(str);
            mWindowManager.updateViewLayout(mFloatingViews.get(maskId), layoutParams);
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
            Toast.makeText(SeeNot.getAppContext(), "消除遮罩失败", Toast.LENGTH_LONG).show();
            sendSimpleNotification("见否可能出了个错？", "消除遮罩失败");
        }
    }

    public static void changeForDarkMode(int maskId) {
        LinearLayout floatingLinear = mFloatingViews.get(maskId).findViewById(R.id.floating);
        if (ExecutorService.isNightMode(SeeNot.getAppContext())) {
            floatingLinear.findViewById(R.id.floating).setBackgroundColor(Color.parseColor("#1B1B1B"));
            ((TextView) floatingLinear.findViewById(R.id.floating_text)).setTextColor(Color.parseColor("#E8D21B"));
        } else {
            floatingLinear.findViewById(R.id.floating).setBackgroundColor(Color.parseColor("#F7F7F7"));
            ((TextView) floatingLinear.findViewById(R.id.floating_text)).setTextColor(Color.parseColor("#DF850D"));
        }
    }
}
