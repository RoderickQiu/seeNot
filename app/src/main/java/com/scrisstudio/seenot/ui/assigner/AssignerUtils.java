package com.scrisstudio.seenot.ui.assigner;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_RENDERING_INFO_KEY;
import static com.scrisstudio.seenot.SeeNot.getAppContext;
import static com.scrisstudio.seenot.SeeNot.getLocale;
import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.MODE_ASSIGNER;
import static com.scrisstudio.seenot.service.ExecutorService.MODE_EXECUTOR;
import static com.scrisstudio.seenot.service.ExecutorService.currentHomePackage;
import static com.scrisstudio.seenot.service.ExecutorService.foregroundPackageName;
import static com.scrisstudio.seenot.service.ExecutorService.inflater;
import static com.scrisstudio.seenot.service.ExecutorService.mService;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.FilterInfo;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.ui.rule.FilterInfoAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class AssignerUtils {

    private static final int LENGTH_LONG = 3000, LENGTH_SHORT = 1200;
    private static WindowManager windowManager;
    private static SharedPreferences sharedPreferences;
    private static OnQuitListener callBack;
    private static WindowManager.LayoutParams customizationParams, outlineParams, toastParams;
    private static ArrayList<RuleInfo> rules = new ArrayList<>();
    private static RuleInfo current = null;
    private static int position, filterId, triggerValueMaxWidth = 0;
    private static final Gson gson = new Gson();
    private static DisplayMetrics metrics = new DisplayMetrics();
    public static Resources resources = MainActivity.resources;

    public static void setOnQuitListener(OnQuitListener callback) {
        callBack = callback;
    }

    public static void setAssignerSharedPreferences(SharedPreferences sharedPreferences) {
        AssignerUtils.sharedPreferences = sharedPreferences;
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void initAssigner(int modeId, int position, int filterId) {
        if (mService == null) {
            le("mService null");
            Toast.makeText(getAppContext(), resources.getString(R.string.open_assigner_failed), Toast.LENGTH_SHORT).show();
        }
        mService.setTheme(R.style.Theme_SeeNot); // fix theme problem

        windowManager = (WindowManager) mService.getSystemService(WINDOW_SERVICE);
        sharedPreferences = ExecutorService.sharedPreferences;
        AssignerUtils.position = position;
        AssignerUtils.filterId = filterId;
        ExecutorService.isServiceRunning = false;
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        View viewCustomization = MainActivity.viewCustomization.get();
        View viewTarget = MainActivity.viewTarget.get();
        View viewToast = inflater.inflate(R.layout.layout_toast_view, null);
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
            try {
                windowManager.removeViewImmediate(viewTarget);
                windowManager.removeViewImmediate(viewCustomization);

                windowManager.addView(viewTarget, outlineParams);
                windowManager.addView(viewCustomization, customizationParams);
            } catch (Exception e1) {
                le("assigner open fail " + e1.getLocalizedMessage());
                Toast.makeText(getAppContext(), resources.getString(R.string.open_assigner_failed), Toast.LENGTH_SHORT).show();
            }
        }

        current = rules.get(position);
        setMode(viewCustomization, viewToast, viewTarget, modeId, layoutOverlayOutline);
    }

    private static void setMode(View viewCustomization, View viewToast, View viewTarget, int mode, FrameLayout layoutOverlayOutline) {
        initMode(viewCustomization, viewToast, viewTarget, mode, layoutOverlayOutline);

        viewCustomization.findViewById(R.id.assigner_content).setVisibility(View.VISIBLE);
        viewCustomization.findViewById(R.id.assigner_pre).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.assigner_home).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.assigner_set).setVisibility(mode == 2 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.assigner_webview).setVisibility(View.GONE);
        ((TextView) viewCustomization.findViewById(R.id.press_here_helper)).setText(R.string.press_here_helper);

        viewCustomization.findViewById(R.id.button_assigner_back).setVisibility(View.GONE);
        viewCustomization.findViewById(R.id.button_new_filter).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_save_pre).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        //viewCustomization.findViewById(R.id.button_set_timed).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_save_rule).setVisibility(mode == 2 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_set_rule).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_assigner_back).setVisibility(mode == 2 ? View.VISIBLE : View.GONE);

        if (mode == 0)
            try { //allow input
                customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                windowManager.updateViewLayout(viewCustomization, customizationParams);
            } catch (Exception e) {
                le("allow input " + e.getLocalizedMessage());
            }
        else
            try {
                customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                windowManager.updateViewLayout(viewCustomization, customizationParams);
            } catch (Exception e) {
                le("disable input " + e.getLocalizedMessage());
            }

        viewCustomization.findViewById(R.id.press_here_helper).setOnClickListener((v) -> {
            WebView webView = viewCustomization.findViewById(R.id.assigner_webview);
            ProgressBar progressBar = viewCustomization.findViewById(R.id.assigner_webview_progress);
            if (webView.getVisibility() == View.GONE) {
                ((TextView) viewCustomization.findViewById(R.id.press_here_helper)).setText(R.string.close_helper);
                viewCustomization.findViewById(R.id.assigner_content).setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("https://seenot.pages.dev/" + getLocale() + "/" + "Assigner" + mode + ".html");
                webView.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                viewCustomization.findViewById(R.id.assigner_content).setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                ((TextView) viewCustomization.findViewById(R.id.press_here_helper)).setText(R.string.press_here_helper);
            }
        });
    }

    private static void initMode(View viewCustomization, View viewToast, View viewTarget, int mode, FrameLayout layoutOverlayOutline) {
        switch (mode) {
            case 0:
                initPre(viewCustomization, viewToast, viewTarget, layoutOverlayOutline);
                break;
            case 1:
                initHome(viewCustomization, viewToast, viewTarget, layoutOverlayOutline);
                break;
            case 2:
                initSet(viewCustomization, viewToast, viewTarget, layoutOverlayOutline);
                break;
        }
    }

    private static void initSet(View viewCustomization, View viewToast, View viewTarget, FrameLayout layoutOverlayOutline) {
        FilterInfo filter = current.getFilter().get(filterId);
        ((TextView) viewCustomization.findViewById(R.id.filter_set_type)).setText(SeeNot.getFilterTypeName(filter.getType()));

        final MaterialSwitch filterSwitch = viewCustomization.findViewById(R.id.filter_status_switch);
        filterSwitch.setChecked(filter.getStatus());
        filterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filter.setStatus(isChecked);
        });

        final LinearLayout layoutTextFindForm = viewCustomization.findViewById(R.id.set_filter_text_find_form);
        final TextView triggerValue = viewCustomization.findViewById(R.id.set_filter_trigger_value);
        final Button btSave = viewCustomization.findViewById(R.id.button_save_rule),
                btTargetSelect = viewCustomization.findViewById(R.id.set_filter_target_select),
                btTargetSelectExit = viewCustomization.findViewById(R.id.set_filter_target_select_exit),
                btTargetDone = viewCustomization.findViewById(R.id.set_filter_target_done),
                btRefresh = viewCustomization.findViewById(R.id.set_filter_refresh),
                btBack = viewCustomization.findViewById(R.id.button_assigner_back);

        btTargetSelect.setVisibility(View.VISIBLE);
        btTargetSelectExit.setVisibility(View.GONE);
        btTargetDone.setVisibility(View.GONE);
        btRefresh.setVisibility(filter.getType() == 1 ? View.VISIBLE : View.GONE);
        layoutTextFindForm.setVisibility((filter.getType() >= 2 && filter.getType() <= 9)
                ? View.VISIBLE : View.GONE);

        triggerValue.setVisibility(View.VISIBLE);
        if (triggerValueMaxWidth == 0) triggerValueMaxWidth = triggerValue.getMaxWidth();
        triggerValue.setMaxWidth((filter.getType() == 2 || filter.getType() == 5 || filter.getType() == 6) ? (int) (triggerValueMaxWidth * 0.6) : triggerValueMaxWidth);

        btBack.setOnClickListener(v -> setMode(viewCustomization, viewToast, viewTarget, 1, layoutOverlayOutline));

        btSave.setOnClickListener(v -> {
            if (!foregroundPackageName.equals(current.getFor()) && !foregroundPackageName.equals("com.scrisstudio.seenot")) {
                sendToast(viewToast, resources.getString(R.string.rule_for_other, getAppRealName(current.getFor())), LENGTH_LONG);
                return;
            }
            String param = triggerValue.getText().toString();
            if ((param.equals("") || param.equals("---") ||
                    param.equals(resources.getString(R.string.cannot_get_current_text)) ||
                    param.equals(resources.getString(R.string.cannot_get_current_id)) ||
                    param.equals(resources.getString(R.string.click_right_to_set))
            ) && filter.getType() != 0) {
                sendToast(viewToast, resources.getString(R.string.fill_the_blanks), LENGTH_LONG);
            } else {
                filter.setParam1(param);
                if (outlineParams.alpha != 0)
                    toggleOutline(layoutOverlayOutline, viewCustomization, viewTarget, viewToast);

                ArrayList<FilterInfo> filters = current.getFilter();
                filters.set(filterId, filter);
                current.setFilter(filters);

                ruleSave(MODE_ASSIGNER);
                setMode(viewCustomization, viewToast, viewTarget, 1, layoutOverlayOutline);

                sendToast(viewToast, resources.getString(R.string.save_succeed), LENGTH_SHORT);
            }
        });
        btTargetSelect.setOnClickListener(v -> toggleOutline(layoutOverlayOutline, viewCustomization, viewTarget, viewToast));
        btTargetSelectExit.setOnClickListener(v -> toggleOutline(layoutOverlayOutline, viewCustomization, viewTarget, viewToast));
        btTargetDone.setOnClickListener(v -> {
            filter.setParam1(triggerValue.getText().toString());
            if (outlineParams.alpha != 0)
                toggleOutline(layoutOverlayOutline, viewCustomization, viewTarget, viewToast);
            sendToast(viewToast, resources.getString(R.string.save_succeed), LENGTH_SHORT);
        });

        refreshSet(filter, viewCustomization, viewToast, 0);
        btRefresh.setOnClickListener(v -> refreshSet(filter, viewCustomization, viewToast, 1));
    }

    private static void refreshSet(FilterInfo filter, View viewCustomization, View viewToast, int refreshMode) {
        if (!foregroundPackageName.equals(current.getFor()) && refreshMode == 1 && !foregroundPackageName.equals("com.scrisstudio.seenot")) {
            sendToast(viewToast, resources.getString(R.string.rule_for_other, getAppRealName(current.getFor())), LENGTH_LONG);
            return;
        }

        String triggerValue = "", triggerLabel = resources.getString(R.string.filter_trigger), tip = "";
        viewCustomization.findViewById(R.id.filter_set).setVisibility(View.VISIBLE);
        viewCustomization.findViewById(R.id.set_filter_trigger_value).setVisibility(View.VISIBLE);
        viewCustomization.findViewById(R.id.set_filter_trigger_label).setVisibility(View.VISIBLE);
        switch (filter.getType()) {
            case 0:
                viewCustomization.findViewById(R.id.filter_set).setVisibility(View.GONE);
                tip = resources.getString(R.string.filter_ban_app_tip);
                break;
            case 1:
                triggerValue = ExecutorService.foregroundClassName;
                tip = resources.getString(R.string.filter_ban_activity_tip);
                break;
            case 2:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_ban_text_tip);
                break;
            case 3:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_ban_id_tip);
                break;
            case 4:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_auto_click_id_tip);
                break;
            case 5:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_auto_click_text_tip);
                break;
            case 6:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_swipe_text_tip);
                break;
            case 7:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_swipe_id_tip);
                break;
            case 8:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_ban_coor_tip);
                break;
            case 9:
                triggerValue = filter.getParam1();
                tip = resources.getString(R.string.filter_auto_click_coor_tip);
                break;
        }
        final String finalTriggerValue = triggerValue.equals("---") ? resources.getString(R.string.click_right_to_set) : triggerValue;
        final String finalTip = tip;
        ((TextView) viewCustomization.findViewById(R.id.set_filter_trigger_value)).setText(finalTriggerValue);
        ((TextView) viewCustomization.findViewById(R.id.set_filter_trigger_label)).setText(triggerLabel);
        ((TextView) viewCustomization.findViewById(R.id.assigner_set_tip)).setText(finalTip);
    }

    private static void initHome(View viewCustomization, View viewToast, View viewTarget, FrameLayout layoutOverlayOutline) {
        viewCustomization.findViewById(R.id.button_set_rule).setOnClickListener(v ->
                setMode(viewCustomization, viewToast, viewTarget, 0, layoutOverlayOutline));
        ((TextView) viewCustomization.findViewById(R.id.home_rule_for)).setText(current.getForName());

        RecyclerView recyclerView = viewCustomization.findViewById(R.id.filters_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SeeNot.getAppContext());
        if (recyclerView.getLayoutManager() == null)
            recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        FilterInfoAdapter adapter = new FilterInfoAdapter(inflater, rules, position, sharedPreferences);
        recyclerView.setAdapter(adapter);

        final Button btAdd = viewCustomization.findViewById(R.id.button_new_filter);
        btAdd.setOnClickListener(v -> {
            if (hasQuitAppFilter(current.getFilter())) { // ban other request after filter_ban_app was set
                sendToast(viewToast, resources.getString(R.string.alreay_quit_app_filter), LENGTH_LONG);
                return;
            }

            final AlertDialog alertDialogTypeSelect;
            final String[] items = {
                    SeeNot.getFilterTypeName(0),
                    SeeNot.getFilterTypeName(1),
                    SeeNot.getFilterTypeName(2),
                    SeeNot.getFilterTypeName(3),
                    SeeNot.getFilterTypeName(8),
                    SeeNot.getFilterTypeName(5),
                    SeeNot.getFilterTypeName(4),
                    SeeNot.getFilterTypeName(9),
                    SeeNot.getFilterTypeName(6),
                    SeeNot.getFilterTypeName(7)
            };
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(mService);
            alertBuilder.setTitle("选择规则类型");
            alertBuilder.setItems(items, (dialogInterface, i) -> {
                int type = 0;
                for (int j = 0; j <= SeeNot.typesCnt; j++) {
                    if (SeeNot.getFilterTypeName(j).equals(items[i])) {
                        type = j;
                        break;
                    }
                }

                ArrayList<FilterInfo> filterInfos = current.getFilter();

                FilterInfo filter = new FilterInfo(true, type, current.getId(), "---", "---");
                filterInfos.add(filter);
                current.setFilter(filterInfos);
                current.setFilterLength(current.getFilterLength() + 1);
                ruleSave(MODE_ASSIGNER);

                adapter.dataChange(filterInfos);

                dialogInterface.dismiss();
            });
            alertDialogTypeSelect = alertBuilder.create();
            Objects.requireNonNull(alertDialogTypeSelect.getWindow()).
                    setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);

            if (current.getFor().equals(foregroundPackageName))
                alertDialogTypeSelect.show();
            else {
                sendToast(viewToast, resources.getString(R.string.rule_for_other,
                        getAppRealName(current.getFor())), LENGTH_LONG);
            }
        });

        FilterInfoAdapter.setSaveListener((rule) -> {
            current = rule;
            adapter.dataChange(current.getFilter());
        });
    }

    private static boolean hasQuitAppFilter(ArrayList<FilterInfo> array) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getType() == 0) return true;
        }
        return false;
    }

    private static void initPre(View viewCustomization, View viewToast, View viewTarget, FrameLayout layoutOverlayOutline) {
        ((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).setText(current.getTitle());
        ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText(resources.getString(R.string.click_right_to_set));
        if (current.getFor().equals("com.software.any")) {
            viewCustomization.findViewById(R.id.rule_for_refresh).setVisibility(View.VISIBLE);
            viewCustomization.findViewById(R.id.rule_for_refresh).setOnClickListener(v -> ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText(getAppRealName(ExecutorService.foregroundPackageName)));
            ((TextView) viewCustomization.findViewById(R.id.assigner_pre_tip)).setText(R.string.assigner_pre_tip);
            viewCustomization.findViewById(R.id.button_save_pre).setOnClickListener(v -> {
                if (Objects.requireNonNull(((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).getText()).toString().equals(resources.getString(R.string.click_right_to_set)) || Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString().equals("")) {
                    sendToast(viewToast, resources.getString(R.string.fill_the_blanks), LENGTH_LONG);
                    return;
                }
                if (ExecutorService.foregroundPackageName.contains("seenot") || ExecutorService.foregroundPackageName.equals(currentHomePackage)) {
                    sendToast(viewToast, resources.getString(R.string.no_seenot_set), LENGTH_LONG);
                    return;
                }
                current.setTitle(Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString());
                current.setFor(ExecutorService.foregroundPackageName);
                current.setForName(getAppRealName(ExecutorService.foregroundPackageName));
                ruleSave(MODE_ASSIGNER);
                sendToast(viewToast, resources.getString(R.string.save_succeed), LENGTH_SHORT);
                setMode(viewCustomization, viewToast, viewTarget, 1, layoutOverlayOutline);
            });
        } else {
            ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText(current.getForName());
            viewCustomization.findViewById(R.id.rule_for_refresh).setVisibility(View.GONE);
            ((TextView) viewCustomization.findViewById(R.id.assigner_pre_tip)).setText(R.string.cannot_modify_after_pre);
            viewCustomization.findViewById(R.id.button_save_pre).setOnClickListener(v -> {
                if (Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString().equals("")) {
                    sendToast(viewToast, resources.getString(R.string.fill_the_blanks), LENGTH_LONG);
                    return;
                }
                current.setTitle(Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString());
                ruleSave(MODE_ASSIGNER);
                sendToast(viewToast, resources.getString(R.string.save_succeed), LENGTH_SHORT);
                setMode(viewCustomization, viewToast, viewTarget, 1, layoutOverlayOutline);
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

    public static void sendToast(View viewToast, String input, int length) {
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
        content.setText(input == null ? resources.getString(R.string.error_come) : input);

        MainActivity.runOnUI(() -> {
            try {
                windowManager.addView(viewToast, toastParams);
            } catch (Exception e) {
                le("send toast run on ui " + e.getLocalizedMessage());
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
                        le("send toast run on ui2 " + e.getLocalizedMessage());
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
                        le("send toast run on ui3 " + e.getLocalizedMessage());
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
        customizationParams.width = width - 60;
        customizationParams.height = 800;
        customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
        customizationParams.y = height - customizationParams.height - 500;
        customizationParams.alpha = 0.84f;
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
        final TextView btQuitText = viewCustomization.findViewById(R.id.button_quit_text);
        final ImageButton btQuitImg = viewCustomization.findViewById(R.id.button_quit_img);
        btQuitText.setOnClickListener(v -> {
            windowManager.removeViewImmediate(viewCustomization);
            windowManager.removeViewImmediate(viewTarget);
            ruleSave(MODE_EXECUTOR);
        });
        btQuitImg.setOnClickListener(v -> {
            windowManager.removeViewImmediate(viewCustomization);
            windowManager.removeViewImmediate(viewTarget);
            ruleSave(MODE_EXECUTOR);
        });
    }

    private static void ruleSave(int mode) {
        ExecutorService.isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
        ExecutorService.setServiceBasicInfo(sharedPreferences, mode);
        rules.set(position, current);
        callBack.onQuit(position, rules, mode);
    }

    @SuppressLint("ClickableViewAccessibility")
    private static void initMover(View viewCustomization) {
        final ScrollView tvPressToMove = viewCustomization.findViewById(R.id.assigner_content);
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

    private static void toggleOutline(FrameLayout layoutOverlayOutline, View viewCustomization, View viewTarget, View viewToast) {
        if (!foregroundPackageName.equals(current.getFor())) {
            sendToast(viewToast, resources.getString(R.string.rule_for_other,
                    getAppRealName(current.getFor())), LENGTH_LONG);
            return;
        }

        FilterInfo filter = current.getFilter().get(filterId);
        int type = filter.getType();
        final TextView triggerValue = viewCustomization.findViewById(R.id.set_filter_trigger_value);
        final Button btTargetSelect = viewCustomization.findViewById(R.id.set_filter_target_select),
                btTargetSelectExit = viewCustomization.findViewById(R.id.set_filter_target_select_exit),
                btTargetDone = viewCustomization.findViewById(R.id.set_filter_target_done);

        triggerValue.setVisibility(View.VISIBLE);

        if (outlineParams.alpha == 0) {
            if (!ExecutorService.foregroundPackageName.equals(ExecutorService.currentHomePackage) &&
                    !ExecutorService.foregroundPackageName.equals("com.scrisstudio.seenot")) {
                btTargetSelect.setVisibility(View.GONE);
                btTargetSelectExit.setVisibility(View.VISIBLE);
                btTargetDone.setVisibility(View.GONE);
                AccessibilityNodeInfo root = null;
                for (AccessibilityWindowInfo windowInfo : mService.getWindows()) {
                    if (windowInfo.getType() == AccessibilityWindowInfo.TYPE_APPLICATION)
                        root = windowInfo.getRoot();
                }
                if (root == null) {
                    if (mService.getRootInActiveWindow() == null) return;
                    else root = mService.getRootInActiveWindow();
                }
                layoutOverlayOutline.removeAllViews();
                ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
                HashMap<String, Integer> idMap = new HashMap<>();
                findAllNode(root, nodeList);
                nodeList.sort((a, b1) -> {
                    Rect rectA = new Rect();
                    Rect rectB = new Rect();
                    a.getBoundsInScreen(rectA);
                    b1.getBoundsInScreen(rectB);
                    return rectB.width() * rectB.height() - rectA.width() * rectA.height();
                });
                for (final AccessibilityNodeInfo e : nodeList) {
                    // if cannot get, don't even allow click it
                    String tempId = e.getViewIdResourceName();
                    if ((type == 3 || type == 4 || type == 7) && tempId == null) continue;
                    if (tempId == null) tempId = "---";
                    else if ((type == 3 || type == 4 || type == 7) && !tempId.contains(foregroundPackageName))
                        continue; // fix fking bug
                    CharSequence tempDescription = e.getContentDescription();
                    CharSequence tempText = (e.getText() != null) ? ("" + e.getText()) : tempDescription;
                    tempText = (tempText == null) ? "" : tempText;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        tempText = (tempText.equals("")) ? (e.getTooltipText() != null ? e.getTooltipText() : "") : tempText;
                    }
                    if ((type == 2 || type == 5 || type == 6) && (tempText.equals(""))) continue;
                    if ((type == 4 || type == 5 || type == 9) && !isParentClickable(e)) continue;

                    if (idMap.containsKey(tempId)) {
                        Integer it = idMap.getOrDefault(tempId, 0);
                        if (it != null)
                            idMap.put(tempId, it + 1);
                        else idMap.put(tempId, 1);
                    } else idMap.put(tempId, 1);

                    final Rect temRect = new Rect();
                    e.getBoundsInScreen(temRect);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(temRect.width(), temRect.height());
                    params.leftMargin = temRect.left;
                    params.topMargin = temRect.top;
                    final ImageView img = new ImageView(mService);
                    img.setBackgroundResource(R.drawable.node);
                    img.setFocusableInTouchMode(true);
                    img.setOnClickListener(View::requestFocus);
                    CharSequence finalTempText = tempText;
                    String finalTempId = tempId;
                    img.setOnFocusChangeListener((v1, hasFocus) -> {
                        btTargetDone.setVisibility(View.VISIBLE);
                        if (hasFocus) {
                            if (type == 2 || type == 5 || type == 6) {
                                triggerValue.setText(finalTempText);
                            } else if (type == 3 || type == 4 || type == 7) {
                                triggerValue.setText(finalTempId);
                                Integer itCnt = idMap.getOrDefault(finalTempId, 0);
                                if (itCnt != null)
                                    if (type == 3) {
                                        if (itCnt > 2)
                                            sendToast(viewToast, resources.getString(R.string.multi_id_type3), LENGTH_LONG);
                                    } else if (type == 4) { // type 4
                                        if (itCnt > 1)
                                            sendToast(viewToast, resources.getString(R.string.multi_id_type4), LENGTH_LONG);
                                    } else {
                                        if (itCnt > 2)
                                            sendToast(viewToast, resources.getString(R.string.multi_id_other), LENGTH_LONG);
                                    }
                                else
                                    triggerValue.setText(resources.getString(R.string.strange_error));
                            } else if (type == 8 || type == 9) {
                                triggerValue.setText(temRect.toString());
                            } else {
                                triggerValue.setText(resources.getString(R.string.strange_error));
                            }

                            v1.setBackgroundResource(R.drawable.node_focus);
                        } else {
                            v1.setBackgroundResource(R.drawable.node);
                        }
                    });
                    layoutOverlayOutline.addView(img, params);
                }
                outlineParams.alpha = 0.5f;
                outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(viewTarget, outlineParams);
            } else
                sendToast(viewToast, resources.getString(R.string.cannot_set_here), LENGTH_SHORT);
        } else {
            outlineParams.alpha = 0f;
            outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(viewTarget, outlineParams);

            btTargetSelect.setVisibility(View.VISIBLE);
            btTargetSelectExit.setVisibility(View.GONE);
            btTargetDone.setVisibility(View.GONE);
        }
    }

    private static boolean isParentClickable(AccessibilityNodeInfo info) {
        if (info.isClickable()) {
            return true;
        } else {
            if (info.getParent() == null) return false;
            else return isParentClickable(info.getParent());
        }
    }

    private static void findAllNode(AccessibilityNodeInfo root, ArrayList<AccessibilityNodeInfo> list) {
        Queue<AccessibilityNodeInfo> queue = new LinkedList<>();
        //Queue<Integer> depth = new LinkedList<>();
        queue.add(root);
        //depth.add(0);
        while (!queue.isEmpty()) {
            AccessibilityNodeInfo info = queue.poll();
            //Integer dp = depth.poll();
            //if (dp == null) dp = 0;
            if (info == null) continue;
            //le(dp.toString() + info.getClassName());
            list.add(info);
            for (int k = 0; k < info.getChildCount(); k++) {
                //depth.add(dp + 1);
                queue.add(info.getChild(k));
            }
        }
    }

    public interface OnQuitListener {
        void onQuit(int position, List<RuleInfo> list, int mode);
    }
}
