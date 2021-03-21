package com.scrisstudio.jianfou.mask;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.ui.RuleInfo;

import java.util.ArrayList;
import java.util.List;

import static com.scrisstudio.jianfou.mask.ActivitySeekerService.mService;

public class MaskAssignerUtils {

	@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
	public static void showActivityCustomizationDialog(int position) {
		// show activity customization window
		final WindowManager windowManager = (WindowManager) mService.getSystemService(AccessibilityService.WINDOW_SERVICE);
		final DisplayMetrics metrics = new DisplayMetrics();
		jianfou.getAppContext().getDisplay().getRealMetrics(metrics);
		final SharedPreferences sharedPreferences = MainActivity.sharedPreferences;
		final Gson gson = new Gson();
		ActivitySeekerService.isServiceRunning = false;

		boolean b = metrics.heightPixels > metrics.widthPixels;
		final int width = b ? metrics.widthPixels : metrics.heightPixels;
		final int height = b ? metrics.heightPixels : metrics.widthPixels;

		final PackageWidgetDescription widgetDescription = new PackageWidgetDescription();
		final PackagePositionDescription positionDescription = new PackagePositionDescription("", "", 0, 0, 500, 500, 6);

		final LayoutInflater inflater = LayoutInflater.from(mService);
		// activity customization view
		final View viewCustomization = inflater.inflate(R.layout.layout_activity_customization, null);
		final TextView tvPackageName = viewCustomization.findViewById(R.id.tv_package_name);
		final TextView tvActivityName = viewCustomization.findViewById(R.id.tv_activity_name);
		final TextView tvWidgetInfo = viewCustomization.findViewById(R.id.tv_widget_info);
		//final TextView tvPositionInfo = viewCustomization.findViewById(R.id.tv_position_info);
		Button btShowOutline = viewCustomization.findViewById(R.id.button_show_outline);
		final Button btAddWidget = viewCustomization.findViewById(R.id.button_add_widget);
		//Button btShowTarget = viewCustomization.findViewById(R.id.button_show_target);
		//Button btReGetTarget = viewCustomization.findViewById(R.id.button_reget_outline);
		//final Button btAddPosition = viewCustomization.findViewById(R.id.button_add_position);
		Button btQuit = viewCustomization.findViewById(R.id.button_quit);

		@SuppressLint("InflateParams") final View viewTarget = inflater.inflate(R.layout.layout_accessibility_node_desc, null);
		final FrameLayout layoutOverlayOutline = viewTarget.findViewById(R.id.frame);

		final ImageView imageTarget = new ImageView(mService);
		imageTarget.setImageResource(R.drawable.ic_target);

		// define view positions
		final WindowManager.LayoutParams customizationParams, outlineParams, targetParams;
		customizationParams = new WindowManager.LayoutParams();
		customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		customizationParams.format = PixelFormat.TRANSPARENT;
		customizationParams.gravity = Gravity.START | Gravity.TOP;
		customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		customizationParams.width = width;
		customizationParams.height = (int) (height / 4.8);
		customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
		customizationParams.y = metrics.heightPixels - customizationParams.height;
		customizationParams.alpha = 0.8f;

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

		viewCustomization.setOnTouchListener(new View.OnTouchListener() {
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
		btAddWidget.setOnClickListener(v -> {
			PackageWidgetDescription temWidget = new PackageWidgetDescription(widgetDescription);

			List<RuleInfo> list = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {}.getType());
			RuleInfo rule = list.get(position);
			rule.setFilter(temWidget);
			list.set(position, rule);
			ActivitySeekerService.setRulesList(list);
			SharedPreferences.Editor ruleEditor = sharedPreferences.edit();
			ruleEditor.putString("rules", gson.toJson(list));
			ruleEditor.apply();

			btAddWidget.setEnabled(false);
			tvPackageName.setText(widgetDescription.packageName + " (以下控件数据已保存)");
		});
		btShowOutline.setOnClickListener(v -> {
			Button button = (Button) v;
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
							widgetDescription.position = temRect;
							widgetDescription.clickable = e.isClickable();
							widgetDescription.className = e.getClassName().toString();
							CharSequence cId = e.getViewIdResourceName();
							widgetDescription.idName = cId == null ? "" : cId.toString();
							CharSequence cDesc = e.getContentDescription();
							widgetDescription.description = cDesc == null ? "" : cDesc.toString();
							CharSequence cText = e.getText();
							widgetDescription.text = cText == null ? "" : cText.toString();
							btAddWidget.setEnabled(true);
							tvPackageName.setText(widgetDescription.packageName);
							tvActivityName.setText(widgetDescription.activityName);
							tvWidgetInfo.setText("click:" + (e.isClickable() ? "true" : "false") + " " + "bonus:" + temRect.toShortString() + " " + "id:" + (cId == null ? "null" : cId.toString().substring(cId.toString().indexOf("id/") + 3)) + " " + "desc:" + (cDesc == null ? "null" : cDesc.toString()) + " " + "text:" + (cText == null ? "null" : cText.toString()));
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
				tvPackageName.setText(widgetDescription.packageName);
				tvActivityName.setText(widgetDescription.activityName + "（如不正确或为空，则是由于当前软件限制，请尝试重新获取）");
				button.setText("隐藏布局");
			} else {
				outlineParams.alpha = 0f;
				outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				windowManager.updateViewLayout(viewTarget, outlineParams);
				btAddWidget.setEnabled(false);
				button.setText("显示布局");
			}
		});
		/*btReGetTarget.setOnClickListener(v -> {
			positionDescription.packageName = ActivitySeekerService.foregroundPackageName;
			positionDescription.activityName = ActivitySeekerService.foregroundClassName;
			tvPackageName.setText(positionDescription.packageName);
			tvActivityName.setText(positionDescription.activityName);
		});*/
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
