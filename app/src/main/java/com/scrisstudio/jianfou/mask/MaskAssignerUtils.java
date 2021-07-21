package com.scrisstudio.jianfou.mask;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.scrisstudio.jianfou.mask.ActivitySeekerService.TAG;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.mService;

public class MaskAssignerUtils {

	@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
	public static void showActivityCustomizationDialog(int position) {
		// show activity customization window
		final WindowManager windowManager = (WindowManager) mService.getSystemService(AccessibilityService.WINDOW_SERVICE);
		final DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getRealMetrics(metrics);
		final SharedPreferences sharedPreferences = MainActivity.sharedPreferences;
		final Gson gson = new Gson();
		ActivitySeekerService.isServiceRunning = false;
		Log.i(TAG, "Assigner running.");

		boolean b = metrics.heightPixels > metrics.widthPixels;
		AtomicInteger dynamicParentLevel = new AtomicInteger(), currentMaskWidget = new AtomicInteger();
		final int width = b ? metrics.widthPixels : metrics.heightPixels;
		final int height = b ? metrics.heightPixels : metrics.widthPixels;

		final WidgetInfo widgetDescription = new WidgetInfo();

		final LayoutInflater inflater = LayoutInflater.from(mService);
		// activity customization view
		@SuppressLint("InflateParams") final View viewCustomization = inflater.inflate(R.layout.layout_mask_assigner, null);
		final TextView tvPackageName = viewCustomization.findViewById(R.id.tv_package_name);
		final TextView tvActivityName = viewCustomization.findViewById(R.id.tv_activity_name);
		final TextView tvWidgetInfo = viewCustomization.findViewById(R.id.tv_widget_info);
		final TextView tvAidText = viewCustomization.findViewById(R.id.emergency_aid_text_info);
		final TextView tvSkipText = viewCustomization.findViewById(R.id.skip_text_info);
		final TextView tvDynamicText = viewCustomization.findViewById(R.id.dynamic_mask_text_info);
		final TextView tvPressToMove = viewCustomization.findViewById(R.id.press_here_move);
		final TextView tvCurrentMaskWidgetNum = viewCustomization.findViewById(R.id.current_mask_widget);
		final TextView tvAllMaskWidgetNum = viewCustomization.findViewById(R.id.all_mask_widget);
		final ImageButton btQuit = viewCustomization.findViewById(R.id.button_quit);
		final Button btShowOutline = viewCustomization.findViewById(R.id.button_show_outline);
		final Button btShowOutline2 = viewCustomization.findViewById(R.id.button_show_outline_2);
		final Button btShowOutline3 = viewCustomization.findViewById(R.id.button_show_outline_3);
		final Button btShowOutline4 = viewCustomization.findViewById(R.id.button_show_outline_4);
		final Button btAddWidget = viewCustomization.findViewById(R.id.button_add_widget);
		final Button btSelectAidText = viewCustomization.findViewById(R.id.button_set_emergency_aid);
		final Button btSelectSkipText = viewCustomization.findViewById(R.id.button_set_skip);
		final Button btSelectDynamicText = viewCustomization.findViewById(R.id.button_set_dynamic);
		final Button btDeleteThisWidget = viewCustomization.findViewById(R.id.button_delete_this_widget);
		final Button btDeleteWidget = viewCustomization.findViewById(R.id.button_delete_widget);
		final ImageButton btPreviousWidget = viewCustomization.findViewById(R.id.previous_mask_widget);
		final ImageButton btNextWidget = viewCustomization.findViewById(R.id.next_mask_widget);
		final Button btDeleteSkipText = viewCustomization.findViewById(R.id.button_delete_skip);
		final Button btDeleteAidText = viewCustomization.findViewById(R.id.button_delete_emergency_aid);
		final Button btDeleteDynamicText = viewCustomization.findViewById(R.id.button_delete_dynamic);
		ImageView lastChoice = new ImageView(mService);


		AtomicReference<List<RuleInfo>> tempList = new AtomicReference<>(gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
		}.getType()));
		RuleInfo tempRule = tempList.get().get(position);
		if (!tempRule.getFilter().equals(new ArrayList<>()))
			btDeleteWidget.setEnabled(true);
		if (tempRule.getAidText() != null) btDeleteAidText.setEnabled(true);
		if (tempRule.getSkipText() != null) btDeleteSkipText.setEnabled(true);
		if (tempRule.getFilterLength() != 0) {
			tvCurrentMaskWidgetNum.setText("1");
		} else {
			currentMaskWidget.set(-1);
			tvCurrentMaskWidgetNum.setText("0");
		}
		tvAllMaskWidgetNum.setText("" + tempRule.getFilterLength());

		//if type is only skip, hide other things
		if (tempRule.getType() == 1) {
			viewCustomization.findViewById(R.id.normal_mask_settings).setVisibility(View.GONE);
			viewCustomization.findViewById(R.id.skip_settings).setVisibility(View.GONE);
			viewCustomization.findViewById(R.id.dynamic_mask_settings).setVisibility(View.GONE);
			((TextView) viewCustomization.findViewById(R.id.emergency_aid_settings_title)).setText(R.string.simple_return);
		} else if (tempRule.getType() == 2) {
			viewCustomization.findViewById(R.id.skip_settings).setVisibility(View.GONE);
		} else {
			viewCustomization.findViewById(R.id.dynamic_mode_normal_settings_help).setVisibility(View.GONE);
			viewCustomization.findViewById(R.id.dynamic_mask_settings).setVisibility(View.GONE);
		}

		@SuppressLint("InflateParams") final View viewTarget = inflater.inflate(R.layout.layout_accessibility_node_desc, null);
		final FrameLayout layoutOverlayOutline = viewTarget.findViewById(R.id.frame);
		@SuppressLint("InflateParams") final View viewLastTimeChoice = inflater.inflate(R.layout.layout_last_choice_frame, null);
		final FrameLayout layoutLastTimeChoiceOutline = viewLastTimeChoice.findViewById(R.id.last_time_choice_frame);

		// define view positions
		final WindowManager.LayoutParams customizationParams, outlineParams, lastTimeFrameParams;
		customizationParams = new WindowManager.LayoutParams();
		customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		customizationParams.format = PixelFormat.TRANSPARENT;
		customizationParams.gravity = Gravity.START | Gravity.TOP;
		customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		customizationParams.width = width;
		customizationParams.height = (int) (height / 3.3);
		customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
		customizationParams.y = metrics.heightPixels - customizationParams.height;
		customizationParams.alpha = 0.8f;
		customizationParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

		lastTimeFrameParams = new WindowManager.LayoutParams();
		lastTimeFrameParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		lastTimeFrameParams.format = PixelFormat.TRANSPARENT;
		lastTimeFrameParams.gravity = Gravity.START | Gravity.TOP;
		lastTimeFrameParams.width = metrics.widthPixels;
		lastTimeFrameParams.height = metrics.heightPixels;
		lastTimeFrameParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		lastTimeFrameParams.alpha = 0.8f;
		windowManager.addView(viewLastTimeChoice, lastTimeFrameParams);

		outlineParams = new WindowManager.LayoutParams();
		outlineParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		outlineParams.format = PixelFormat.TRANSPARENT;
		outlineParams.gravity = Gravity.START | Gravity.TOP;
		outlineParams.width = metrics.widthPixels;
		outlineParams.height = metrics.heightPixels;
		outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		outlineParams.alpha = 0f;

		Function<ArrayList<Integer>, Rect> nodeSearcher = indices -> {
			try {
				AccessibilityNodeInfo node = mService.getRootInActiveWindow();
				for (int i = 0; i < indices.size(); i++) {
					node = node.getChild(indices.get(i));
				}

				Rect rect = new Rect();
				node.getBoundsInScreen(rect);
				return rect;
			} catch (Exception e) {
				return null;
			}
		};

		Consumer<Integer> lastChoiceSelecter = (id) -> {
			try {
				layoutLastTimeChoiceOutline.removeView(lastChoice);
			} catch (Exception ignored) {
			}
			try {
				lastChoice.setBackgroundResource(R.drawable.node_last_choice);
				Rect temRect = nodeSearcher.apply(tempRule.getFilter().get(id).indices);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(temRect.width(), temRect.height());
				params.leftMargin = temRect.left;
				params.topMargin = temRect.top;
				layoutLastTimeChoiceOutline.addView(lastChoice, params);
			} catch (Exception ignored) {
			}
		};

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

		Consumer<String> btDeleteOperator = (mode) -> {
			try {
				layoutLastTimeChoiceOutline.removeView(lastChoice);
			} catch (Exception ignored) {
			}
			switch (mode) {
				case "widget":
					tempRule.setFilter(new ArrayList<>());
					tempRule.setFilterLength(0);
					currentMaskWidget.set(0);
					tvCurrentMaskWidgetNum.setText("0");
					tvAllMaskWidgetNum.setText("0");
					tvPackageName.setText(null);
					tvActivityName.setText(null);
					tvWidgetInfo.setText(null);
				case "aid":
					tempRule.setAidText(null);
					if (tempRule.getType() == 1) tempRule.setFilter(new ArrayList<>());
					tvAidText.setText(null);
					break;
				case "skip":
					tempRule.setSkipText(null);
					tvSkipText.setText(null);
					break;
				case "dynamic":
					tempRule.setDynamicText(null);
					tempRule.setDynamicParentLevel(0);
					tvDynamicText.setText(null);
					break;
			}
			List<RuleInfo> newList = tempList.get();
			newList.set(position, tempRule);
			tempList.set(newList);
			ActivitySeekerService.setRulesList(newList);
			SharedPreferences.Editor ruleEditor = sharedPreferences.edit();
			ruleEditor.putString("rules", gson.toJson(newList));
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
		btDeleteDynamicText.setOnClickListener(v -> {
			btDeleteOperator.accept("dynamic");
			btDeleteDynamicText.setEnabled(false);
		});

		Function<String, String> getApplicationNameFunction = in -> {
			PackageManager pm = jianfou.getAppContext().getPackageManager();
			String name = "";
			try {
				name = pm.getApplicationLabel(pm.getApplicationInfo(in, PackageManager.GET_META_DATA)).toString();
			} catch (PackageManager.NameNotFoundException ignored) {
			}
			return name;
		};

		Function<String, String> getApplicationVersionFunction = in -> {
			PackageManager pm = jianfou.getAppContext().getPackageManager();
			String version = "";
			try {
				version = pm.getPackageInfo(in, PackageManager.GET_META_DATA).versionName;
			} catch (PackageManager.NameNotFoundException ignored) {
			}
			return version;
		};

		Consumer<String> btOperator = (mode) -> {
			try {
				layoutLastTimeChoiceOutline.removeView(lastChoice);
			} catch (Exception ignored) {
			}
			switch (mode) {
				case "widget":
					ArrayList<WidgetInfo> tempArray = tempRule.getFilter();
					WidgetInfo tempWidget = new WidgetInfo(widgetDescription);
					if (currentMaskWidget.get() >= tempRule.getFilterLength()) {
						tempArray.add(tempWidget);
						tempRule.setFilterLength(tempRule.getFilterLength() + 1);
						currentMaskWidget.addAndGet(1);
						tvCurrentMaskWidgetNum.setText(Integer.toString(tempRule.getFilterLength()));
						tvAllMaskWidgetNum.setText(Integer.toString(tempRule.getFilterLength()));
					} else {
						tempArray.set(currentMaskWidget.get(), tempWidget);
					}
					tempRule.setFilter(tempArray);

					tvPackageName.setText(widgetDescription.packageName + " (控件数据已保存)");
					if (!getApplicationNameFunction.apply(widgetDescription.packageName).equals(""))
						tempRule.setFor(getApplicationNameFunction.apply(widgetDescription.packageName));
					if (!getApplicationVersionFunction.apply(widgetDescription.packageName).equals(""))
						tempRule.setForVersion(getApplicationVersionFunction.apply(widgetDescription.packageName));
					break;
				case "aid":
					tempArray = new ArrayList<>();
					tempWidget = new WidgetInfo(widgetDescription);
					tempArray.add(tempWidget);
					tempRule.setAidText(tvAidText.getText().toString());
					tvAidText.setText(tvAidText.getText() + " (控件数据已保存)");
					if (tempRule.getType() == 1) {
						tempRule.setFilter(tempArray);
					}
					break;
				case "skip":
					tempRule.setSkipText((String) tvSkipText.getText());
					tvSkipText.setText(tvSkipText.getText() + " (控件数据已保存)");
					break;
				case "dynamic":
					tempRule.setDynamicText((String) tvDynamicText.getText());
					tempRule.setDynamicParentLevel(dynamicParentLevel.get());
					tvDynamicText.setText(tvDynamicText.getText() + " (控件数据已保存)");
					break;
			}
			List<RuleInfo> newList = tempList.get();
			newList.set(position, tempRule);
			tempList.set(newList);
			ActivitySeekerService.setRulesList(newList);
			SharedPreferences.Editor ruleEditor = sharedPreferences.edit();
			ruleEditor.putString("rules", gson.toJson(newList));
			ruleEditor.apply();

			//auto disable outline after settings saved
			outlineParams.alpha = 0f;
			outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
			windowManager.updateViewLayout(viewTarget, outlineParams);
			btAddWidget.setEnabled(false);
			btSelectAidText.setEnabled(false);
			btSelectSkipText.setEnabled(false);
			btSelectDynamicText.setEnabled(false);
			btShowOutline.setText("显示布局");
			btShowOutline2.setText("显示布局");
			btShowOutline3.setText("显示布局");
			btShowOutline4.setText("显示布局");
		};
		btAddWidget.setOnClickListener(v -> btOperator.accept("widget"));
		btSelectAidText.setOnClickListener(v -> btOperator.accept("aid"));
		btSelectSkipText.setOnClickListener(v -> btOperator.accept("skip"));
		btSelectDynamicText.setOnClickListener(v -> btOperator.accept("dynamic"));

		Consumer<Integer> showOutlineOperator = (num) -> {
			if (outlineParams.alpha == 0) {
				if (!ActivitySeekerService.foregroundPackageName.equals(ActivitySeekerService.currentHomePackage) && !ActivitySeekerService.foregroundPackageName.equals("com.scrisstudio.jianfou")) {
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

								widgetDescription.position = temRect;
								widgetDescription.indices = indicesList;
								widgetDescription.clickable = e.isClickable();
								widgetDescription.className = e.getClassName().toString();
								CharSequence cId = e.getViewIdResourceName();
								widgetDescription.idName = cId == null ? "" : cId.toString();
								CharSequence cDesc = e.getContentDescription();
								widgetDescription.description = cDesc == null ? "" : cDesc.toString();
								CharSequence cText = (e.getText() != null) ? ("" + e.getText()) : "";
								widgetDescription.text = cText.toString();
								btAddWidget.setEnabled(true);
								btSelectAidText.setEnabled(true);
								btSelectSkipText.setEnabled(true);
								btSelectDynamicText.setEnabled(true);
								switch (num) {
									case 1:
										tvPackageName.setText(widgetDescription.packageName);
										tvActivityName.setText(widgetDescription.activityName);
										tvWidgetInfo.setText("bonus:" + temRect.toShortString() + " " + "id:" + (cId == null ? "null" : cId.toString().substring(cId.toString().indexOf("id/") + 3)) + " " + "desc:" + (cDesc == null ? "null" : cDesc.toString()) + " " + "text:" + cText.toString());
										btDeleteWidget.setEnabled(true);
										break;
									case 2:
										//"" added to transform spannable to normal string
										tvAidText.setText((e.getText() != null) ? ("" + e.getText()) : e.getContentDescription());
										btDeleteAidText.setEnabled(true);
										break;
									case 3:
										tvSkipText.setText((e.getText() != null) ? ("" + e.getText()) : e.getContentDescription());
										btDeleteSkipText.setEnabled(true);
										break;
									case 4:
										// get dynamic parent level in the same time
										// abort when cannot get dynamic parent level
										try {
											dynamicParentLevel.set(0);

											WidgetInfo filterWidget = tempRule.getFilter().get(currentMaskWidget.get());
											AccessibilityNodeInfo node = root, textNode = e;
											int level = 0;
											for (int i = 0; i < filterWidget.indices.size(); i++) {
												node = node.getChild(filterWidget.indices.get(i));
											}
											while (!textNode.equals(root)) {
												if (textNode.equals(node)) {
													dynamicParentLevel.set(level);
													btDeleteDynamicText.setEnabled(true);
													tvDynamicText.setText((e.getText() != null) ? ("" + e.getText()) : e.getContentDescription());
													break;
												}
												textNode = textNode.getParent();
												level++;
											}

											if (dynamicParentLevel.get() == 0) {
												Toast.makeText(jianfou.getAppContext(), "添加失败，此文字与要屏蔽的元素无层级关系", Toast.LENGTH_LONG).show();
											}
										} catch (Exception exce) {
											Toast.makeText(jianfou.getAppContext(), "添加失败，此文字与要屏蔽的元素无层级关系 " + exce.getLocalizedMessage(), Toast.LENGTH_LONG).show();
										}
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
					btShowOutline4.setText("隐藏布局");
				} else
					Toast.makeText(jianfou.getAppContext(), "不允许在此处设置规则", Toast.LENGTH_SHORT).show();
			} else {
				outlineParams.alpha = 0f;
				outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				windowManager.updateViewLayout(viewTarget, outlineParams);

				btAddWidget.setEnabled(false);
				btSelectAidText.setEnabled(false);
				btSelectSkipText.setEnabled(false);
				btSelectDynamicText.setEnabled(false);

				btShowOutline.setText("显示布局");
				btShowOutline2.setText("显示布局");
				btShowOutline3.setText("显示布局");
				btShowOutline4.setText("显示布局");
			}
		};
		btShowOutline.setOnClickListener(v -> showOutlineOperator.accept(1));
		btShowOutline2.setOnClickListener(v -> showOutlineOperator.accept(2));
		btShowOutline3.setOnClickListener(v -> showOutlineOperator.accept(3));
		btShowOutline4.setOnClickListener(v -> {
			if (!tempRule.getFilter().equals(new ArrayList<>()))
				showOutlineOperator.accept(4);
			else
				Toast.makeText(jianfou.getAppContext(), "需要先定义要被屏蔽的元素", Toast.LENGTH_LONG).show();
		});

		btPreviousWidget.setOnClickListener(v -> {
			if (tempRule.getFilterLength() > 0 && currentMaskWidget.get() > 0) {
				lastChoiceSelecter.accept(currentMaskWidget.addAndGet(-1));
				tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidget.get() + 1));
				tvWidgetInfo.setText(null);
			}
		});

		btNextWidget.setOnClickListener(v -> {
			if (tempRule.getFilterLength() > 0 && currentMaskWidget.get() < tempRule.getFilterLength() - 1) {
				lastChoiceSelecter.accept(currentMaskWidget.addAndGet(1));
				tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidget.get() + 1));
				tvWidgetInfo.setText(null);
			}
		});

		btQuit.setOnClickListener(v -> {
			windowManager.removeViewImmediate(viewTarget);
			windowManager.removeViewImmediate(viewCustomization);
			ActivitySeekerService.isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
		});

		windowManager.addView(viewTarget, outlineParams);
		windowManager.addView(viewCustomization, customizationParams);
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
