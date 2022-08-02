package com.scrisstudio.seenot.ui.assigner;

import static android.content.Context.WINDOW_SERVICE;
import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.MODE_ASSIGNER;
import static com.scrisstudio.seenot.service.ExecutorService.MODE_EXECUTOR;
import static com.scrisstudio.seenot.service.ExecutorService.currentHomePackage;
import static com.scrisstudio.seenot.service.ExecutorService.inflater;
import static com.scrisstudio.seenot.service.ExecutorService.mService;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.service.FilterInfo;
import com.scrisstudio.seenot.service.RuleInfo;
import com.scrisstudio.seenot.ui.rule.FilterInfoAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    @SuppressLint("ClickableViewAccessibility")
    public static void initAssigner(int modeId, int position, int filterId) {
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
            le(e.getLocalizedMessage());
        }

        current = rules.get(position);
        setMode(viewCustomization, viewToast, viewTarget, modeId, layoutOverlayOutline);
    }

    private static void setMode(View viewCustomization, View viewToast, View viewTarget, int mode, FrameLayout layoutOverlayOutline) {
        initMode(viewCustomization, viewToast, viewTarget, mode, layoutOverlayOutline);

        viewCustomization.findViewById(R.id.assigner_pre).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.assigner_home).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.assigner_set).setVisibility(mode == 2 ? View.VISIBLE : View.GONE);

        viewCustomization.findViewById(R.id.button_assigner_back).setVisibility(View.GONE);
        viewCustomization.findViewById(R.id.button_new_filter).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_save_pre).setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_save_rule).setVisibility(mode == 2 ? View.VISIBLE : View.GONE);
        viewCustomization.findViewById(R.id.button_set_rule).setVisibility(mode == 1 ? View.VISIBLE : View.GONE);

        if (mode == 0)
            try { //allow input
                customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                windowManager.updateViewLayout(viewCustomization, customizationParams);
            } catch (Exception e) {
                le(e.getLocalizedMessage());
            }
        else
            try {
                customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                windowManager.updateViewLayout(viewCustomization, customizationParams);
            } catch (Exception e) {
                le(e.getLocalizedMessage());
            }
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

        final SwitchMaterial filterSwitch = viewCustomization.findViewById(R.id.filter_status_switch);
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
                btRefresh = viewCustomization.findViewById(R.id.set_filter_refresh);

        btTargetSelectExit.setVisibility(View.GONE);
        btTargetDone.setVisibility(View.GONE);
        btRefresh.setVisibility(filter.getType() == 1 ? View.VISIBLE : View.GONE);
        layoutTextFindForm.setVisibility(filter.getType() == 2 ? View.VISIBLE : View.GONE);

        if (triggerValueMaxWidth == 0) triggerValueMaxWidth = triggerValue.getMaxWidth();
        triggerValue.setMaxWidth(filter.getType() == 2 ? (int) (triggerValueMaxWidth * 0.6) : triggerValueMaxWidth);

        btSave.setOnClickListener(v -> {
            String param = triggerValue.getText().toString();
            if (param.equals("") || param.equals("---") || param.equals("无法获取当前文字")) {
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

        refreshSet(filter, viewCustomization);
        viewCustomization.findViewById(R.id.set_filter_refresh).setOnClickListener(v -> refreshSet(filter, viewCustomization));
    }

    private static void refreshSet(FilterInfo filter, View viewCustomization) {
        String triggerValue = "", triggerLabel = resources.getString(R.string.filter_trigger), tip = "";
        viewCustomization.findViewById(R.id.filter_set).setVisibility(View.VISIBLE);
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
        }
        final String finalTriggerValue = triggerValue, finalTriggerLabel = triggerLabel, finalTip = tip;
        ((TextView) viewCustomization.findViewById(R.id.set_filter_trigger_value)).setText(finalTriggerValue);
        ((TextView) viewCustomization.findViewById(R.id.set_filter_trigger_label)).setText(finalTriggerLabel);
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

            AtomicInteger type = new AtomicInteger();
            String packageName = ExecutorService.foregroundPackageName;
            PopupMenu popup = new PopupMenu(SeeNot.getAppContext(), viewCustomization);
            MenuInflater menuInflater = new MenuInflater(SeeNot.getAppContext());
            menuInflater.inflate(R.menu.menu_filter_type, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.type_0) type.set(0);
                else if (item.getItemId() == R.id.type_1) type.set(1);
                else if (item.getItemId() == R.id.type_2) type.set(2);
                else if (item.getItemId() == R.id.type_3) type.set(3);

                ArrayList<FilterInfo> filterInfos = current.getFilter();

                FilterInfo filter = new FilterInfo(true, type.get(), current.getId(), "---", "---");
                filterInfos.add(filter);
                current.setFilter(filterInfos);
                current.setFilterLength(current.getFilterLength() + 1);
                ruleSave(MODE_ASSIGNER);

                adapter.dataChange(filterInfos);

                return false;
            });

            if (current.getFor().equals(packageName))
                popup.show();
            else
                sendToast(viewToast, "这条规则是关于" + getAppRealName(current.getFor())
                        + "的，只能在那个程序打开时编辑", LENGTH_LONG);
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
        ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText("---");
        if (current.getFor().equals("com.software.any")) {
            viewCustomization.findViewById(R.id.rule_for_refresh).setOnClickListener(v -> ((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).setText(getAppRealName(ExecutorService.foregroundPackageName)));
            viewCustomization.findViewById(R.id.button_save_pre).setOnClickListener(v -> {
                if (Objects.requireNonNull(((TextView) viewCustomization.findViewById(R.id.pre_rule_for)).getText()).toString().equals("---") || Objects.requireNonNull(((TextInputEditText) viewCustomization.findViewById(R.id.rule_name_textfield)).getText()).toString().equals("")) {
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

    private static void sendToast(View viewToast, String input, int length) {
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
            ruleSave(MODE_EXECUTOR);
        });
    }

    private static void ruleSave(int mode) {

        ExecutorService.isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
        rules.set(position, current);
        callBack.onQuit(position, rules, mode);
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

    private static void toggleOutline(FrameLayout layoutOverlayOutline, View viewCustomization, View viewTarget, View viewToast) {
        FilterInfo filter = current.getFilter().get(filterId);
        final TextView triggerValue = viewCustomization.findViewById(R.id.set_filter_trigger_value);
        final Button btTargetSelect = viewCustomization.findViewById(R.id.set_filter_target_select),
                btTargetSelectExit = viewCustomization.findViewById(R.id.set_filter_target_select_exit),
                btTargetDone = viewCustomization.findViewById(R.id.set_filter_target_done);

        if (outlineParams.alpha == 0) {
            if (!ExecutorService.foregroundPackageName.equals(ExecutorService.currentHomePackage) &&
                    !ExecutorService.foregroundPackageName.equals("com.scrisstudio.seenot")) {
                btTargetSelect.setVisibility(View.GONE);
                btTargetSelectExit.setVisibility(View.VISIBLE);
                btTargetDone.setVisibility(View.GONE);
                AccessibilityNodeInfo root = mService.getRootInActiveWindow();
                if (root == null) return;
                layoutOverlayOutline.removeAllViews();
                ArrayList<AccessibilityNodeInfo> roots = new ArrayList<>();
                roots.add(root);
                ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
                findAllNode(roots, nodeList);
                nodeList.sort((a, b1) -> {
                    Rect rectA = new Rect();
                    Rect rectB = new Rect();
                    a.getBoundsInScreen(rectA);
                    b1.getBoundsInScreen(rectB);
                    return rectB.width() * rectB.height() - rectA.width() * rectA.height();
                });
                for (final AccessibilityNodeInfo e : nodeList) {
                    final Rect temRect = new Rect();
                    e.getBoundsInScreen(temRect);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(temRect.width(), temRect.height());
                    params.leftMargin = temRect.left;
                    params.topMargin = temRect.top;
                    final ImageView img = new ImageView(mService);
                    img.setBackgroundResource(R.drawable.node);
                    img.setFocusableInTouchMode(true);
                    img.setOnClickListener(View::requestFocus);
                    img.setOnFocusChangeListener((v1, hasFocus) -> {
                        btTargetDone.setVisibility(View.VISIBLE);
                        if (hasFocus) {
                            AccessibilityNodeInfo tempNode = e;
                            ArrayList<Integer> indicesList = new ArrayList<>();
                            while (true) {
                                for (int i = 0; i < tempNode.getParent().getChildCount(); i++) {
                                    if (tempNode.getParent().getChild(i).equals(tempNode)) {
                                        indicesList.add(i);
                                        break;
                                    }
                                }
                                tempNode = tempNode.getParent();

                                if (tempNode.equals(root)) {
                                    Collections.reverse(indicesList);
                                    break;
                                }
                            }

                            CharSequence tempDescription = e.getContentDescription();
                            CharSequence tempText = (e.getText() != null) ? ("" + e.getText()) : tempDescription;
                            tempText = (tempText == null) ? "" : tempText;
                            triggerValue.setText(tempText.equals("") ? "无法获取当前文字" : tempText);

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
                sendToast(viewToast, "不允许在此处设置规则", LENGTH_SHORT);
        } else {
            outlineParams.alpha = 0f;
            outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(viewTarget, outlineParams);

            btTargetSelect.setVisibility(View.VISIBLE);
            btTargetSelectExit.setVisibility(View.GONE);
            btTargetDone.setVisibility(View.GONE);
        }
    }

    private static void findAllNode
            (ArrayList<AccessibilityNodeInfo> roots, ArrayList<AccessibilityNodeInfo> list) {
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
        void onQuit(int position, List<RuleInfo> list, int mode);
    }
}
