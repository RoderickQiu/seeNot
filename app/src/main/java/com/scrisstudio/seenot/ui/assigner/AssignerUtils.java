package com.scrisstudio.seenot.ui.assigner;

import static android.content.Context.WINDOW_SERVICE;
import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.currentHomePackage;
import static com.scrisstudio.seenot.service.ExecutorService.mService;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.service.RuleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class AssignerUtils {

    private static final int LENGTH_LONG = 3000;
    private static int LENGTH_SHORT = 1200;
    private static WindowManager windowManager;
    private static SharedPreferences sharedPreferences;
    private static AtomicInteger mode = new AtomicInteger();
    private static OnQuitListener callBack;
    private static WindowManager.LayoutParams customizationParams, outlineParams, toastParams;
    private static ArrayList<RuleInfo> rules = new ArrayList<>();
    private static RuleInfo current = null;
    private static int position;
    private static final Gson gson = new Gson();
    private static DisplayMetrics metrics = new DisplayMetrics();
    private static Resources resources = MainActivity.resources;

    public static void setOnQuitListener(OnQuitListener callback) {
        callBack = callback;
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void initAssigner(int modeId, int position) {
        windowManager = (WindowManager) mService.getSystemService(WINDOW_SERVICE);
        sharedPreferences = ExecutorService.sharedPreferences;
        mode.set(modeId);
        AssignerUtils.position = position;
        ExecutorService.isServiceRunning = false;
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        View viewCustomization = MainActivity.viewCustomization.get();
        View viewTarget = MainActivity.viewTarget.get();
        View viewToast = ExecutorService.inflater.inflate(R.layout.layout_toast_view, null);
        FrameLayout layoutOverlayOutline = viewTarget.findViewById(R.id.frame);

        initParams();
        initQuit(viewCustomization, viewTarget);
        initMover(viewCustomization);

        if (viewCustomization == null) {
            le("View customization does not exist.");
            return;
        } else {
            l("Assigner running.");
        }

        try {
            windowManager.addView(viewTarget, outlineParams);
            windowManager.addView(viewCustomization, customizationParams);
        } catch (Exception e) {
            le(e.getLocalizedMessage());
        }

        current = rules.get(position);
        if (current.getFor().equals("com.software.any")) {
            setMode(viewCustomization, viewToast, 0);
        } else {
            setMode(viewCustomization, viewToast, 1);
        }
    }

    private static void setMode(View viewCustomization, View viewToast, int mode) {
        initMode(viewCustomization, viewToast, mode);

        viewCustomization.findViewById(R.id.assigner_pre).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.assigner_home).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);

        viewCustomization.findViewById(R.id.button_assigner_back).setVisibility(View.GONE);
        viewCustomization.findViewById(R.id.button_new_filter).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_save_pre).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_save_rule).setVisibility(View.GONE);
        viewCustomization.findViewById(R.id.button_set_rule).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
    }

    private static void initMode(View viewCustomization, View viewToast, int mode) {
        switch (mode) {
            case 0:
                initPre(viewCustomization, viewToast);
                break;
            case 1:
                initHome(viewCustomization, viewToast);
                break;
        }
    }

    private static void initHome(View viewCustomization, View viewToast) {
        viewCustomization.findViewById(R.id.button_set_rule).setOnClickListener(v -> setMode(viewCustomization, viewToast, 0));
        ((TextView) viewCustomization.findViewById(R.id.home_rule_for)).setText(current.getForName());
    }

    private static void initPre(View viewCustomization, View viewToast) {
        ((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).setText(current.getTitle());
        ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText("---");
        if (current.getFor().equals("com.software.any")) {
            viewCustomization.findViewById(R.id.rule_for_refresh).setOnClickListener(v -> ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText(getAppRealName(ExecutorService.foregroundPackageName)));
            viewCustomization.findViewById(R.id.button_save_pre).setOnClickListener(v -> {
                if (Objects.requireNonNull(((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).getText()).toString().equals("---") || Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString().equals("")) {
                    sendToast(viewToast, resources.getString(R.string.fill_the_blanks), LENGTH_SHORT);
                    return;
                }
                if (ExecutorService.foregroundPackageName.contains("seenot") || ExecutorService.foregroundPackageName.equals(currentHomePackage)) {
                    sendToast(viewToast, resources.getString(R.string.no_seenot_set), LENGTH_LONG);
                    return;
                }
                current.setTitle(Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString());
                current.setFor(ExecutorService.foregroundPackageName);
                current.setForName(getAppRealName(ExecutorService.foregroundPackageName));
                sendToast(viewToast, resources.getString(R.string.save_succeed), LENGTH_SHORT);
                setMode(viewCustomization, viewToast, 1);
            });
        } else {
            ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText(current.getForName());
            viewCustomization.findViewById(R.id.rule_for_refresh).setVisibility(View.GONE);
            ((TextView) viewCustomization.findViewById(R.id.assigner_pre_tip)).setText(R.string.cannot_modify_after_pre);
            viewCustomization.findViewById(R.id.button_save_pre).setOnClickListener(v -> {
                if (Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString().equals("")) {
                    sendToast(viewToast, resources.getString(R.string.fill_the_blanks), LENGTH_SHORT);
                    return;
                }
                current.setTitle(Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString());
                sendToast(viewToast, resources.getString(R.string.save_succeed), LENGTH_SHORT);
                setMode(viewCustomization, viewToast, 1);
            });
        }
    }

    private static String getAppRealName(String in) {
        PackageManager pm = SeeNot.getAppContext().getPackageManager();
        String name;
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(in, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            name = in;
        }
        return name;
    }

    private static void sendToast(View viewToast, Object input, int length) {
        AtomicInteger contentWidth = new AtomicInteger();
        TextView content = viewToast.findViewById(R.id.tv_toast_content);
        content.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                content.removeOnLayoutChangeListener(this);
                contentWidth.set(content.getWidth());
            }
        });
        content.setText(input == null ? "发生错误" : input.toString());

        MainActivity.runOnUI(() -> {
            try {
                windowManager.addView(viewToast, toastParams);
            } catch (Exception e) {
                le(e.getLocalizedMessage());
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toastParams.x = (metrics.widthPixels - contentWidth.get()) / 2 - 26;
                toastParams.y = metrics.heightPixels - 300;
                toastParams.alpha = 1f;

                MainActivity.runOnUI(() -> {
                    try {
                        windowManager.updateViewLayout(viewToast, toastParams);
                    } catch (Exception e) {
                        le(e.getLocalizedMessage());
                    }
                });
            }
        }, 150);


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.runOnUI(() -> {
                    try {
                        windowManager.removeViewImmediate(viewToast);
                    } catch (Exception e) {
                        le(e.getLocalizedMessage());
                    }
                });
            }
        }, length);
    }

    private static void initParams() {
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        boolean b = metrics.heightPixels > metrics.widthPixels;
        final int width = b ? metrics.widthPixels : metrics.heightPixels;
        final int height = b ? metrics.heightPixels : metrics.widthPixels;

        customizationParams = new WindowManager.LayoutParams();
        customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        customizationParams.format = PixelFormat.TRANSPARENT;
        customizationParams.gravity = Gravity.START | Gravity.TOP;
        customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        customizationParams.width = width;
        customizationParams.height = 800;
        customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
        customizationParams.y = height - customizationParams.height - 500;
        customizationParams.alpha = 0.75f;
        customizationParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;

        outlineParams = new WindowManager.LayoutParams();
        outlineParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        outlineParams.format = PixelFormat.TRANSPARENT;
        outlineParams.gravity = Gravity.START | Gravity.TOP;
        outlineParams.width = metrics.widthPixels;
        outlineParams.height = metrics.heightPixels;
        outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        outlineParams.alpha = 0f;

        toastParams = ExecutorService.layoutParams;
        toastParams.alpha = 0f;
        toastParams = new WindowManager.LayoutParams();
        toastParams.x = (metrics.widthPixels - 30) / 2 - 26;
        toastParams.y = metrics.heightPixels - 300;
        toastParams.width = -2;
        toastParams.height = -2;
        toastParams.gravity = Gravity.START | Gravity.TOP;
        toastParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        toastParams.format = 1;
        toastParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }

    private static void initQuit(View viewCustomization, View viewTarget) {
        final ImageButton btQuit = viewCustomization.findViewById(R.id.button_quit);
        btQuit.setOnClickListener(v -> {
            windowManager.removeViewImmediate(viewCustomization);
            windowManager.removeViewImmediate(viewTarget);
            ExecutorService.isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
            rules.set(position, current);
            callBack.onQuit(position, rules);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private static void initMover(View viewCustomization) {
        final TextView tvPressToMove = viewCustomization.findViewById(R.id.press_here_move);
        tvPressToMove.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        customizationParams.x = Math.round(customizationParams.x + (event.getRawX() - x));
                        customizationParams.y = Math.round(customizationParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(viewCustomization, customizationParams);
                        break;
                }
                return true;
            }
        });
    }

    private static void findAllNode(ArrayList<AccessibilityNodeInfo> roots, ArrayList<AccessibilityNodeInfo> list) {
        ArrayList<AccessibilityNodeInfo> childrenList = new ArrayList<>();
        for (AccessibilityNodeInfo e : roots) {
            if (e == null) continue;
            list.add(e);
            for (int n = 0; n < e.getChildCount(); n++) {
                childrenList.add(e.getChild(n));
            }
        }
        if (!childrenList.isEmpty()) {
            findAllNode(childrenList, list);
        }
    }

    public interface OnQuitListener {
        void onQuit(int position, List<RuleInfo> list);
    }
}
