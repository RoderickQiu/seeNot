package com.scrisstudio.jianfou.mask;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.scrisstudio.jianfou.mask.ActivitySeekerService.TAG;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.mService;

public class MaskAssignerUtils {

	@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
	public static void showActivityCustomizationDialog(int position, int type) {
		// show activity customization window
		final WindowManager windowManager = (WindowManager) mService.getSystemService(AccessibilityService.WINDOW_SERVICE);
		final DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getRealMetrics(metrics);
		final SharedPreferences sharedPreferences = MainActivity.sharedPreferences;
		final Gson gson = new Gson();
		ActivitySeekerService.isServiceRunning = false;
		Log.i(TAG, "Assigner running.");

		boolean b = metrics.heightPixels > metrics.widthPixels;
		final int width = b ? metrics.widthPixels : metrics.heightPixels;
		final int height = b ? metrics.heightPixels : metrics.widthPixels;

		final WidgetInfo widgetDescription = new WidgetInfo();
		final PositionInfo positionDescription = new PositionInfo("", "", 0, 0, 500, 500, 6);

		final LayoutInflater inflater = LayoutInflater.from(mService);
		// activity customization view
		final View viewCustomization = inflater.inflate(R.layout.layout_mask_assigner, null);
		final TextView tvPackageName = viewCustomization.findViewById(R.id.tv_package_name);
		final TextView tvActivityName = viewCustomization.findViewById(R.id.tv_activity_name);
		final TextView tvWidgetInfo = viewCustomization.findViewById(R.id.tv_widget_info);
		final TextView tvAidText = viewCustomization.findViewById(R.id.emergency_aid_text_info);
		final TextView tvSkipText = viewCustomization.findViewById(R.id.skip_text_info);
		final TextView tvPressToMove = viewCustomization.findViewById(R.id.press_here_move);
		//final TextView tvPositionInfo = viewCustomization.findViewById(R.id.tv_position_info);
		final ImageButton btQuit = viewCustomization.findViewById(R.id.button_quit);
		final Button btShowOutline = viewCustomization.findViewById(R.id.button_show_outline);
		final Button btShowOutline2 = viewCustomization.findViewById(R.id.button_show_outline_2);
		final Button btShowOutline3 = viewCustomization.findViewById(R.id.button_show_outline_3);
		final Button btAddWidget = viewCustomization.findViewById(R.id.button_add_widget);
		final Button btSelectAidText = viewCustomization.findViewById(R.id.button_set_emergency_aid);
		final Button btSelectSkipText = viewCustomization.findViewById(R.id.button_set_skip);
		final Button btDeleteWidget = viewCustomization.findViewById(R.id.button_delete_widget);
		final Button btDeleteSkipText = viewCustomization.findViewById(R.id.button_delete_skip);
		final Button btDeleteAidText = viewCustomization.findViewById(R.id.button_delete_emergency_aid);
		//Button btShowTarget = viewCustomization.findViewById(R.id.button_show_target);
		//Button btReGetTarget = viewCustomization.findViewById(R.id.button_reget_outline);
		//final Button btAddPosition = viewCustomization.findViewById(R.id.button_add_position);

		//if type is only skip, hide other things
		if (type == 1) {
			viewCustomization.findViewById(R.id.normal_mask_settings).setVisibility(View.GONE);
			viewCustomization.findViewById(R.id.skip_settings).setVisibility(View.GONE);
			((TextView) viewCustomization.findViewById(R.id.emergency_aid_settings_title)).setText(R.string.simple_return);
		}

		AtomicReference<List<RuleInfo>> tempList = new AtomicReference<>(gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
		}.getType()));
		RuleInfo tempRule = tempList.get().get(position);
		if (!tempRule.getFilter().equals(new WidgetInfo()))
			btDeleteWidget.setEnabled(true);
		if (tempRule.getAidText() != null) btDeleteAidText.setEnabled(true);
		if (tempRule.getSkipText() != null) btDeleteSkipText.setEnabled(true);

		@SuppressLint("InflateParams") final View viewTarget = inflater.inflate(R.layout.layout_accessibility_node_desc, null);
		final FrameLayout layoutOverlayOutline = viewTarget.findViewById(R.id.frame);

		final ImageView imageTarget = new ImageView(mService);
		imageTarget.setImageResource(R.drawable.target);

		// define view positions
		final WindowManager.LayoutParams customizationParams, outlineParams, targetParams;
		customizationParams = new WindowManager.LayoutParams();
		customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		customizationParams.format = PixelFormat.TRANSPARENT;
		customizationParams.gravity = Gravity.START | Gravity.TOP;
		customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		customizationParams.width = width;
		customizationParams.height = (int) (height / 4);
		customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
		customizationParams.y = metrics.heightPixels - customizationParams.height;
		customizationParams.alpha = 0.8f;
		customizationParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

		outlineParams = new WindowManager.LayoutParams();
		outlineParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		outlineParams.format = PixelFormat.TRANSPARENT;
		outlineParams.gravity = Gravity.START | Gravity.TOP;
		outlineParams.width = metrics.widthPixels;
		outlineParams.height = metrics.heightPixels;
		outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		outlineParams.alpha = 0f;

		targetParams = new WindowManager.LayoutParams();
		targetParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		targetParams.format = PixelFormat.TRANSPARENT;
		targetParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		targetParams.gravity = Gravity.START | Gravity.TOP;
		targetParams.width = targetParams.height = width / 4;
		targetParams.x = (metrics.widthPixels - targetParams.width) / 2;
		targetParams.y = (metrics.heightPixels - targetParams.height) / 2;
		targetParams.alpha = 0f;

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

		imageTarget.setOnTouchListener(new View.OnTouchListener() {
			final int width = targetParams.width / 2;
			final int height = targetParams.height / 2;
			int x = 0;
			int y = 0;

			@SuppressLint("SetTextI18n")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//btAddPosition.setEnabled(true);
						targetParams.alpha = 0.9f;
						windowManager.updateViewLayout(imageTarget, targetParams);
						x = Math.round(event.getRawX());
						y = Math.round(event.getRawY());
						break;
					case MotionEvent.ACTION_MOVE:
						targetParams.x = Math.round(targetParams.x + (event.getRawX() - x));
						targetParams.y = Math.round(targetParams.y + (event.getRawY() - y));
						x = Math.round(event.getRawX());
						y = Math.round(event.getRawY());
						windowManager.updateViewLayout(imageTarget, targetParams);
						positionDescription.packageName = ActivitySeekerService.foregroundPackageName;
						positionDescription.activityName = ActivitySeekerService.foregroundClassName;
						positionDescription.x = targetParams.x + width;
						positionDescription.y = targetParams.y + height;
						tvPackageName.setText(positionDescription.packageName);
						tvActivityName.setText(positionDescription.activityName);
						//tvPositionInfo.setText("X轴：" + positionDescription.x + "    " + "Y轴：" + positionDescription.y + "    " + "(其他参数默认)");
						break;
					case MotionEvent.ACTION_UP:
						targetParams.alpha = 0.5f;
						windowManager.updateViewLayout(imageTarget, targetParams);
						break;
				}
				return true;
			}
		});

		Consumer<String> btDeleteOperator = (mode) -> {
			List<RuleInfo> list = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
			}.getType());
			RuleInfo rule = list.get(position);
			switch (mode) {
				case "widget":
					rule.setFilter(new WidgetInfo());
					tvPackageName.setText(null);
				case "aid":
					rule.setAidText(null);
					tvAidText.setText(null);
					break;
				case "skip":
					rule.setSkipText(null);
					tvSkipText.setText(null);
					break;
			}
			list.set(position, rule);
			ActivitySeekerService.setRulesList(list);
			SharedPreferences.Editor ruleEditor = sharedPreferences.edit();
			ruleEditor.putString("rules", gson.toJson(list));
			ruleEditor.apply();
		};
		btDeleteWidget.setOnClickListener(v -> {
			btDeleteOperator.accept("widget");
			btDeleteWidget.setEnabled(false);
		});
		btDeleteAidText.setOnClickListener(v -> {
			btDeleteOperator.accept("aid");
			btDeleteAidText.setEnabled(false);
		});
		btDeleteSkipText.setOnClickListener(v -> {
			btDeleteOperator.accept("skip");
			btDeleteSkipText.setEnabled(false);
		});

		Consumer<String> btOperator = (mode) -> {
			WidgetInfo temWidget = new WidgetInfo(widgetDescription);
			List<RuleInfo> list = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
			}.getType());
			RuleInfo rule = list.get(position);
			switch (mode) {
				case "widget":
					rule.setFilter(temWidget);
					tvPackageName.setText(widgetDescription.packageName + " (控件数据已保存)");
				case "aid":
					rule.setAidText((String) tvAidText.getText());
					tvAidText.setText(tvAidText.getText() + " (控件数据已保存)");
					break;
				case "skip":
					rule.setSkipText((String) tvSkipText.getText());
					tvSkipText.setText(tvSkipText.getText() + " (控件数据已保存)");
					break;
			}
			list.set(position, rule);
			ActivitySeekerService.setRulesList(list);
			SharedPreferences.Editor ruleEditor = sharedPreferences.edit();
			ruleEditor.putString("rules", gson.toJson(list));
			ruleEditor.apply();

			btAddWidget.setEnabled(false);
			btSelectAidText.setEnabled(false);
			btSelectSkipText.setEnabled(false);
		};
		btAddWidget.setOnClickListener(v -> {
			btOperator.accept("widget");
		});
		btSelectAidText.setOnClickListener(v -> {
			btOperator.accept("aid");
		});
		btSelectSkipText.setOnClickListener(v -> {
			btOperator.accept("skip");
		});

		Consumer<Integer> showOutlineOperator = (num) -> {
			if (outlineParams.alpha == 0) {
				AccessibilityNodeInfo root = mService.getRootInActiveWindow();
				if (root == null) return;
				widgetDescription.packageName = ActivitySeekerService.foregroundPackageName;
				widgetDescription.activityName = ActivitySeekerService.foregroundClassName;
				layoutOverlayOutline.removeAllViews();
				ArrayList<AccessibilityNodeInfo> roots = new ArrayList<>();
				roots.add(root);
				ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
				findAllNode(roots, nodeList, "");
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
						if (hasFocus) {
							AccessibilityNodeInfo tempNode = e;
							ArrayList<Integer> indicesList = new ArrayList<Integer>();
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

							widgetDescription.position = temRect;
							widgetDescription.indices = indicesList;
							widgetDescription.clickable = e.isClickable();
							widgetDescription.className = e.getClassName().toString();
							CharSequence cId = e.getViewIdResourceName();
							widgetDescription.idName = cId == null ? "" : cId.toString();
							CharSequence cDesc = e.getContentDescription();
							widgetDescription.description = cDesc == null ? "" : cDesc.toString();
							CharSequence cText = e.getText();
							widgetDescription.text = cText == null ? "" : cText.toString();
							btAddWidget.setEnabled(true);
							btSelectAidText.setEnabled(true);
							btSelectSkipText.setEnabled(true);
							switch (num) {
								case 1:
									tvPackageName.setText(widgetDescription.packageName);
									tvActivityName.setText(widgetDescription.activityName);
									tvWidgetInfo.setText("bonus:" + temRect.toShortString() + " " + "id:" + (cId == null ? "null" : cId.toString().substring(cId.toString().indexOf("id/") + 3)) + " " + "desc:" + (cDesc == null ? "null" : cDesc.toString()) + " " + "text:" + (cText == null ? "null" : cText.toString()));
									btDeleteWidget.setEnabled(true);
									break;
								case 2:
									tvAidText.setText((e.getText() != null) ? e.getText() : e.getContentDescription());
									btDeleteAidText.setEnabled(true);
									break;
								case 3:
									tvSkipText.setText((e.getText() != null) ? e.getText() : e.getContentDescription());
									btDeleteSkipText.setEnabled(true);
									break;
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

				if (num == 1) {
					tvPackageName.setText(widgetDescription.packageName);
					tvActivityName.setText(widgetDescription.activityName + "（如不正确或为空，则是由于当前软件限制，请尝试重新获取）");
				}

				btShowOutline.setText("隐藏布局");
				btShowOutline2.setText("隐藏布局");
				btShowOutline3.setText("隐藏布局");
			} else {
				outlineParams.alpha = 0f;
				outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				windowManager.updateViewLayout(viewTarget, outlineParams);

				btAddWidget.setEnabled(false);
				btSelectAidText.setEnabled(false);
				btSelectSkipText.setEnabled(false);

				btShowOutline.setText("显示布局");
				btShowOutline2.setText("显示布局");
				btShowOutline3.setText("显示布局");
			}
		};
		btShowOutline.setOnClickListener(v -> {
			showOutlineOperator.accept(1);
		});
		btShowOutline2.setOnClickListener(v -> {
			showOutlineOperator.accept(2);
		});
		btShowOutline3.setOnClickListener(v -> {
			showOutlineOperator.accept(3);
		});

		btQuit.setOnClickListener(v -> {
			windowManager.removeViewImmediate(viewTarget);
			windowManager.removeViewImmediate(viewCustomization);
			windowManager.removeViewImmediate(imageTarget);
			ActivitySeekerService.isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
		});

		windowManager.addView(viewTarget, outlineParams);
		windowManager.addView(viewCustomization, customizationParams);
		windowManager.addView(imageTarget, targetParams);
	}

	private static void findAllNode(List<AccessibilityNodeInfo> roots, List<AccessibilityNodeInfo> list, String indent) {
		ArrayList<AccessibilityNodeInfo> childrenList = new ArrayList<>();
		for (AccessibilityNodeInfo e : roots) {
			if (e == null) continue;
			list.add(e);
			for (int n = 0; n < e.getChildCount(); n++) {
				childrenList.add(e.getChild(n));
			}
		}
		if (!childrenList.isEmpty()) {
			findAllNode(childrenList, list, indent + "  ");
		}
	}
}
