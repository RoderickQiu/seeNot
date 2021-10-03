package com.scrisstudio.jianfou.mask;
import static android.content.Context.WINDOW_SERVICE;
import static com.scrisstudio.jianfou.activity.MainActivity.resources;
import static com.scrisstudio.jianfou.jianfou.voided;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.TAG;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.inflater;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.mService;
import static com.scrisstudio.jianfou.mask.MixedOperatorUtils.le;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.ui.SkipSetAdapter;
import com.scrisstudio.jianfou.ui.SubRuleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class MixedAssignerUtil {
	// show activity customization window
	private static WindowManager windowManager;
	private static SharedPreferences sharedPreferences;
	private static AtomicInteger mode = new AtomicInteger();
	private static OnQuitListener callBack;

	public static void setOnQuitListener(OnQuitListener callback) {
		callBack = callback;
	}

	@SuppressLint("ClickableViewAccessibility")
	public static void showActivityCustomizationDialog(int modeId, int position, int current, int subCurrent) {
		windowManager = (WindowManager) mService.getSystemService(WINDOW_SERVICE);
		sharedPreferences = MainActivity.sharedPreferences;
		mode.set(modeId);
		DisplayMetrics metrics = new DisplayMetrics();
		final Gson gson = new Gson();
		final AtomicBoolean isSettingCondition = new AtomicBoolean();
		final AtomicInteger currentMaskWidgetId = new AtomicInteger();
		final AtomicReference<ArrayList<Integer>> resultWidget = new AtomicReference<>(new ArrayList<>());
		final AtomicReference<String> resultText = new AtomicReference<>("");
		final AtomicReference<Integer> resultFilterLength = new AtomicReference<>(0);
		MixedExecutorService.isServiceRunning = false;

		windowManager.getDefaultDisplay().getRealMetrics(metrics);
		boolean b = metrics.heightPixels > metrics.widthPixels;
		final int width = b ? metrics.widthPixels : metrics.heightPixels;
		final int height = b ? metrics.heightPixels : metrics.widthPixels;

		final WindowManager.LayoutParams customizationParams, outlineParams, lastTimeFrameParams, toastParams;
		customizationParams = new WindowManager.LayoutParams();
		customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		customizationParams.format = PixelFormat.TRANSPARENT;
		customizationParams.gravity = Gravity.START | Gravity.TOP;
		customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		customizationParams.width = width;
		customizationParams.height = (int) (height / 2.97);
		customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
		customizationParams.y = 0;
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

		lastTimeFrameParams = new WindowManager.LayoutParams();
		lastTimeFrameParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
		lastTimeFrameParams.format = PixelFormat.TRANSPARENT;
		lastTimeFrameParams.gravity = Gravity.START | Gravity.TOP;
		lastTimeFrameParams.width = metrics.widthPixels;
		lastTimeFrameParams.height = metrics.heightPixels;
		lastTimeFrameParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		lastTimeFrameParams.alpha = 0.8f;

		toastParams = MixedExecutorService.layoutParams;
		toastParams.alpha = 0f;

		View viewCustomization = MainActivity.viewCustomization.get(),
				viewTarget = MainActivity.viewTarget.get(),
				viewLastTimeChoice = MainActivity.viewLastTimeChoice.get(),
				viewToast = inflater.inflate(R.layout.layout_toast_view, null);

		final FrameLayout layoutOverlayOutline = viewTarget.findViewById(R.id.frame),
				layoutLastTimeChoiceOutline = viewLastTimeChoice.findViewById(R.id.last_time_choice_frame);

		ImageView lastChoice = new ImageView(mService);

		if (viewCustomization == null) {
			Log.e(TAG, "View customization does not exist.");
			return;
		} else Log.i(TAG, "Assigner running.");

		Consumer<Object> toastSender = (input) -> {
			AtomicInteger contentWidth = new AtomicInteger();
			TextView content = viewToast.findViewById(R.id.tv_toast_content);
			content.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
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
			}, 200);

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
			}, 3000);
		};

		if (mode.get() == -1) {
			try {
				windowManager.removeViewImmediate(viewCustomization);
			} catch (Exception ignored) {
			}
		} else {
			List<MixedRuleInfo> mix = gson.fromJson(sharedPreferences.getString("mixed", "{}"), new TypeToken<List<MixedRuleInfo>>() {
			}.getType());

			BiConsumer<ArrayList<SubRuleInfo>, Boolean> subRuleCommitter = (subArray, shouldAddLength) -> {
				SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
				mix.get(position).setSubRules(subArray);
				if (shouldAddLength)
					mix.get(position).setSubRuleLength(mix.get(position).getSubRuleLength() + 1);
				ruleInitEditor.putString("mixed", gson.toJson(mix));
				ruleInitEditor.apply();
			};

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

			Function<ArrayList<Integer>, Rect> nodeSearcher = indices -> {
				try {
					AccessibilityNodeInfo node = mService.getRootInActiveWindow();
					for (int i = 0; i < mService.getWindows().size(); i++) {
						if (mService.getWindows().get(i).getType() == AccessibilityWindowInfo.TYPE_APPLICATION)
							node = mService.getWindows().get(i).getRoot();
					}

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
					Rect temRect = nodeSearcher.apply(mix.get(position).getSubRules().get(current).getFilter().get(id));
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(temRect.width(), temRect.height());
					params.leftMargin = temRect.left;
					params.topMargin = temRect.top;
					layoutLastTimeChoiceOutline.addView(lastChoice, params);
				} catch (Exception ignored) {
				}
			};

			TextView tvPackageName = viewCustomization.findViewById(R.id.condition_in_app),
					tvActivityName = viewCustomization.findViewById(R.id.condition_in_activity),
					tvCurrentMaskWidgetNum = viewCustomization.findViewById(R.id.current_mask_widget_num), tvAllMaskWidgetNum = viewCustomization.findViewById(R.id.all_mask_widget_num),
					tvConditionSkipText = viewCustomization.findViewById(R.id.subrule_skip_text);
			Button btShowConditionOutline = viewCustomization.findViewById(R.id.condition_text_show_outline),
					btConfirmConditionText = viewCustomization.findViewById(R.id.condition_text_confirm),
					btShowMaskOutline = viewCustomization.findViewById(R.id.condition_mask_show_outline),
					btConfirmMask = viewCustomization.findViewById(R.id.condition_mask_confirm);
			ImageButton btSelectPreviousMask = viewCustomization.findViewById(R.id.previous_mask_widget),
					btSelectNextMask = viewCustomization.findViewById(R.id.next_mask_widget);

			try {
				if (mix.get(position).getSubRules().get(current).getFilterLength() != 0) {
					tvCurrentMaskWidgetNum.setText("1");
				} else {
					currentMaskWidgetId.set(-1);
					tvCurrentMaskWidgetNum.setText("0");
				}
			} catch (Exception e) {
				currentMaskWidgetId.set(-1);
				tvCurrentMaskWidgetNum.setText("0");
			}

			Consumer<Void> showOutlineOperator = (v) -> {
				if (outlineParams.alpha == 0) {
					if (!MixedExecutorService.foregroundPackageName.equals(MixedExecutorService.currentHomePackage) && !MixedExecutorService.foregroundPackageName.equals("com.scrisstudio.jianfou")) {
						if (currentMaskWidgetId.get() > 4) {
							toastSender.accept("只允许同时存在5个遮罩");
							return;
						}
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
									resultText.set((String) tempText);
									tvConditionSkipText.setText(tempText.equals("") ? "无法获取当前文字" : (String) tempText);
									resultWidget.set(indicesList);

									btConfirmConditionText.setVisibility(View.VISIBLE);
									btConfirmMask.setVisibility(View.VISIBLE);

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

						btShowConditionOutline.setText("隐藏布局");
						btShowMaskOutline.setText("隐藏布局");
					} else
						toastSender.accept("不允许在此处设置规则");
				} else {
					outlineParams.alpha = 0f;
					outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
					windowManager.updateViewLayout(viewTarget, outlineParams);

					btConfirmConditionText.setVisibility(View.GONE);
					btShowConditionOutline.setText("显示布局");
					btShowMaskOutline.setText("显示布局");
				}
			};

			@SuppressLint("SetTextI18n") Consumer<String> modeChanger = (param) -> {
				if (mode.get() == 0) {//homepage
					viewCustomization.findViewById(R.id.mixed_home).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.mixed_back).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_subrule_set).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_condition_set).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.new_mask_subrule).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.set_mask_info).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.button_save_condition).setVisibility(View.GONE);

					if (mix.get(position).getFor().equals("未设置")) {
						viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.GONE);
					} else {
						viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.VISIBLE);
						((TextView) viewCustomization.findViewById(R.id.rule_for_text)).setText(mix.get(position).getFor());
					}

					RecyclerView recyclerView = viewCustomization.findViewById(R.id.subrule_list);
					LinearLayoutManager linearLayoutManager = new LinearLayoutManager(jianfou.getAppContext());
					if (recyclerView.getLayoutManager() == null)
						recyclerView.setLayoutManager(linearLayoutManager);
					linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

					SubRuleAdapter adapter = new SubRuleAdapter(inflater, mix, position, sharedPreferences);
					recyclerView.setAdapter(adapter);

					final Button btAdd = viewCustomization.findViewById(R.id.new_mask_subrule);
					btAdd.setOnClickListener(v -> {
						AtomicInteger type = new AtomicInteger();
						String packageName = MixedExecutorService.foregroundPackageName;

						Consumer<Void> popupOpener = (v1) -> {
							PopupMenu popup = new PopupMenu(jianfou.getAppContext(), v);
							MenuInflater menuInflater = new MenuInflater(jianfou.getAppContext());
							menuInflater.inflate(R.menu.menu_sub_types, popup.getMenu());
							popup.setOnMenuItemClickListener(item -> {
								switch (item.getItemId()) {
									case R.id.type_0:
										type.set(0);
										break;
									case R.id.type_1:
										type.set(1);
										break;
									case R.id.type_2:
										type.set(2);
										break;
									case R.id.type_3:
										type.set(3);
										break;
								}

								SubRuleInfo sub = new SubRuleInfo(type.get(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, null, new ArrayList<>(), 0);
								ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();
								subArray.add(sub);
								subRuleCommitter.accept(subArray, true);
								adapter.dataChange(subArray);

								return false;
							});
							popup.show();
						};

						if (mix.get(position).getFor().equals("未设置")) {
							le(packageName);
							if (!packageName.equals("") && !packageName.equals("com.scrisstudio.jianfou") && !packageName.equals(MixedExecutorService.currentHomePackage)) {
								mix.get(position).setFor(getApplicationNameFunction.apply(packageName));
								mix.get(position).setForVersion(getApplicationVersionFunction.apply(packageName));
								mix.get(position).setForPackageName(packageName);

								viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.VISIBLE);
								((TextView) viewCustomization.findViewById(R.id.rule_for_text)).setText(mix.get(position).getFor());
								popupOpener.accept(voided);
							} else
								toastSender.accept("不能给见否本身和系统桌面添加规则，请重试");
						} else {
							if (mix.get(position).getForPackageName().equals(packageName))
								popupOpener.accept(voided);
							else
								toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");
						}
					});
				} else if (mode.get() == 1) {
					viewCustomization.findViewById(R.id.mixed_home).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_subrule_set).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_back).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.mixed_condition_set).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.set_mask_info).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.new_mask_subrule).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.button_save_condition).setVisibility(View.GONE);

					isSettingCondition.set(false);

					SubRuleInfo currentSub = mix.get(position).getSubRules().get(current);
					currentMaskWidgetId.set(0);
					tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidgetId.get() + 1));
					tvAllMaskWidgetNum.setText(Integer.toString(currentSub.getFilterLength()));

					// condition set
					int type = currentSub.getType();
					((TextView) viewCustomization.findViewById(R.id.subrule_set_title)).setText(jianfou.getRuleTypeRealName(type));
					final TextView conditionMsg = viewCustomization.findViewById(R.id.subrule_set_condition_msg),
							conditionValue = viewCustomization.findViewById(R.id.condition_value);
					switch (type) {
						case 0:
							conditionMsg.setText("当条件满足时，将寻找此元素进行遮罩");
							break;
						case 1:
							conditionMsg.setText("当条件满足时，就将执行强制返回操作");
							break;
						case 2:
							conditionMsg.setText("当条件满足时，才会查找文字来进行遮罩");
							break;
						case 3:
							conditionMsg.setText("当条件满足时，就将绘制所要求的比例遮罩");
							break;
					}

					@SuppressLint("CutPasteId") final Button conditionSet = viewCustomization.findViewById(R.id.condition_set);
					final Spinner spinner = viewCustomization.findViewById(R.id.condition_spinner);

					Consumer<String> spinnerConditionController = (item) -> {
						if (item == null) {
							conditionSet.setVisibility(View.GONE);
							conditionValue.setVisibility(View.GONE);
							spinner.setSelection(0);
						} else {
							conditionSet.setVisibility(View.VISIBLE);
							conditionValue.setVisibility(View.VISIBLE);
							spinner.setSelection(1);
							if (item.equals("")) conditionValue.setText("---");
							else {
								conditionValue.setText(item);
							}
						}
					};
					spinnerConditionController.accept(currentSub.getConditionActivity());
					spinner.postDelayed(() ->
							spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
								@Override
								public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
									spinnerConditionController.accept(pos == 0 ? null : "");
									ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();
									subArray.get(current).setConditionActivity(pos == 0 ? null : "");
									subRuleCommitter.accept(subArray, false);
								}

								@Override
								public void onNothingSelected(AdapterView<?> parent) {
								}
							}), 500);

					// skip set
					final RecyclerView recyclerView = viewCustomization.findViewById(R.id.skip_list);
					LinearLayoutManager linearLayoutManager = new LinearLayoutManager(jianfou.getAppContext());
					if (recyclerView.getLayoutManager() == null)
						recyclerView.setLayoutManager(linearLayoutManager);
					linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

					SkipSetAdapter adapter = new SkipSetAdapter(inflater, mix, position, current, sharedPreferences);
					recyclerView.setAdapter(adapter);

					final Button btAddSkip = viewCustomization.findViewById(R.id.button_add_skip),
							btCopySkip = viewCustomization.findViewById(R.id.button_copy_skip);
					btAddSkip.setOnClickListener(v -> {
						ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();
						SkipInfo skipInfo = new SkipInfo(0, "");
						ArrayList<SkipInfo> skipArray = subArray.get(current).getSkip();
						skipArray.add(skipInfo);
						subArray.get(current).setSkip(skipArray);
						subArray.get(current).setSkipLength(subArray.get(current).getSkipLength() + 1);
						subRuleCommitter.accept(subArray, false);
						adapter.dataChange(skipArray);
					});
					btCopySkip.setOnClickListener(v -> {
						ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();
						SubRuleInfo newSub = subArray.get(current);
						newSub.setFilter(new ArrayList<>());
						newSub.setFilterLength(0);
						newSub.setDynamicParentLevel(new ArrayList<>());
						newSub.setText(new ArrayList<>());
						subArray.add(newSub);
						subRuleCommitter.accept(subArray, true);
						toastSender.accept("规则复制成功");
					});

					// mask assign set
					Button btDeleteThisMask = viewCustomization.findViewById(R.id.condition_mask_delete_this);
					if (mix.get(position).getSubRules().get(current).getFilterLength() > 0) {
						btDeleteThisMask.setVisibility(View.VISIBLE);
					}
					btShowMaskOutline.setOnClickListener(v -> {
						if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName)) {
							showOutlineOperator.accept(voided);
						} else {
							toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");
						}
					});
					btConfirmMask.setOnClickListener(v -> {
						showOutlineOperator.accept(voided);

						ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();
						ArrayList<ArrayList<Integer>> filters = subArray.get(current).getFilter();

						if (currentMaskWidgetId.get() >= subArray.get(current).getFilterLength() - 1 || currentMaskWidgetId.get() == -1) {
							filters.add(resultWidget.get());
							subArray.get(current).setFilter(filters);
							subArray.get(current).setFilterLength(subArray.get(current).getFilterLength() + 1);
							currentMaskWidgetId.set(subArray.get(current).getFilterLength() - 1);
							tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidgetId.get() + 1));
							tvAllMaskWidgetNum.setText(Integer.toString(currentMaskWidgetId.get() + 1));
						} else {
							filters.set(currentMaskWidgetId.get(), resultWidget.get());
							subArray.get(current).setFilter(filters);
						}

						subRuleCommitter.accept(subArray, false);

						btDeleteThisMask.setVisibility(View.VISIBLE);
					});
					btSelectPreviousMask.setOnClickListener(v -> {
						if (currentMaskWidgetId.get() >= 1) {
							tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidgetId.decrementAndGet() + 1));
							lastChoiceSelecter.accept(currentMaskWidgetId.get());
						}
					});
					btSelectNextMask.setOnClickListener(v -> {
						if (currentMaskWidgetId.get() < mix.get(position).getSubRules().get(current).getFilterLength() - 1) {
							tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidgetId.incrementAndGet() + 1));
							lastChoiceSelecter.accept(currentMaskWidgetId.get());
						}
					});
					btDeleteThisMask.setOnClickListener(v -> {
						ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();
						if (subArray.get(current).getFilterLength() >= currentMaskWidgetId.get()) {
							subArray.get(current).getFilter().remove(currentMaskWidgetId.get());
							subArray.get(current).setFilterLength(subArray.get(current).getFilterLength() - 1);
							if (currentMaskWidgetId.get() > 0) currentMaskWidgetId.addAndGet(-1);
							tvCurrentMaskWidgetNum.setText(Integer.toString(currentMaskWidgetId.get() + 1));
							tvAllMaskWidgetNum.setText(Integer.toString(subArray.get(current).getFilterLength()));
							tvPackageName.setText(null);
							tvActivityName.setText(null);
							if (subArray.get(current).getFilterLength() > 0) {
								lastChoiceSelecter.accept(currentMaskWidgetId.get());
							}

							subRuleCommitter.accept(subArray, false);
						}
					});
				} else if (mode.get() == 2) {//mask more set
					viewCustomization.findViewById(R.id.mixed_home).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_subrule_set).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_back).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.mixed_condition_set).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.button_save_condition).setVisibility(View.VISIBLE);

					final TextView tvConditionTitle = viewCustomization.findViewById(R.id.condition_assign_title);
					final Button btRefresh = viewCustomization.findViewById(R.id.condition_refresh),
							btSave = viewCustomization.findViewById(R.id.button_save_condition);
					ArrayList<SubRuleInfo> subArray = mix.get(position).getSubRules();

					Consumer<Void> getPackageAndClassName = (v) -> {
						try {
							tvPackageName.setText(MixedExecutorService.foregroundPackageName);
							tvActivityName.setText(MixedExecutorService.foregroundClassName.replace(MixedExecutorService.foregroundPackageName, ""));
						} catch (Exception ignored) {
						}
					};
					if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName))
						getPackageAndClassName.accept(voided);
					else {
						tvPackageName.setText("---");
						tvActivityName.setText("---");
					}

					btRefresh.setOnClickListener(v -> {
						if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName))
							getPackageAndClassName.accept(voided);
						else {
							tvPackageName.setText("---");
							tvActivityName.setText("---");
							toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");

						}
					});

					if (isSettingCondition.get()) {//condition
						tvConditionTitle.setText(resources.getStringArray(R.array.condition_spinner)[subArray.get(current).getConditionActivity() == null ? 0 : 1]);
						btSave.setOnClickListener(v -> {
							if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName)) {
								subArray.get(current).setConditionActivity(MixedExecutorService.foregroundClassName);
								subRuleCommitter.accept(subArray, false);
							} else
								toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");
						});
					} else {//skip activity
						ArrayList<SkipInfo> skipArray = subArray.get(current).getSkip();

						tvConditionTitle.setText(resources.getStringArray(R.array.skip_spinner)[skipArray.get(subCurrent).getType()]);

						if (skipArray.get(subCurrent).getType() == 0) {
							btSave.setOnClickListener(v -> {
								if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName)) {
									skipArray.get(subCurrent).setParam(MixedExecutorService.foregroundClassName);
									subArray.get(current).setSkip(skipArray);
									subRuleCommitter.accept(subArray, false);
								} else
									toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");

							});
							viewCustomization.findViewById(R.id.condition_skip).setVisibility(View.GONE);
						} else {//skip text
							btShowConditionOutline.setVisibility(View.VISIBLE);
							tvConditionSkipText.setText(skipArray.get(subCurrent).getParam());

							btShowConditionOutline.setOnClickListener(v -> {
								if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName)) {
									getPackageAndClassName.accept(voided);
									showOutlineOperator.accept(voided);
								} else {
									tvPackageName.setText("---");
									tvActivityName.setText("---");
									toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");
								}
							});
							btConfirmConditionText.setOnClickListener(v -> {
								showOutlineOperator.accept(voided);
								btConfirmConditionText.setVisibility(View.GONE);
								skipArray.get(subCurrent).setParam(resultText.get());
								subArray.get(current).setSkip(skipArray);
							});
							btSave.setOnClickListener(v -> {
								if (mix.get(position).getForPackageName().equals(MixedExecutorService.foregroundPackageName)) {
									subRuleCommitter.accept(subArray, false);
								} else
									toastSender.accept("这条规则是关于" + mix.get(position).getFor() + "的，只能在那个程序打开时设置");
							});
							viewCustomization.findViewById(R.id.condition_skip).setVisibility(View.VISIBLE);
						}
					}
				} else if (mode.get() == 3) {//rule basis set
					try {
						customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
						windowManager.updateViewLayout(viewCustomization, customizationParams);
					} catch (Exception e) {
						le(e.getLocalizedMessage());
					}
					viewCustomization.findViewById(R.id.mixed_home).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_subrule_set).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_rule_for).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.mixed_back).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.mixed_condition_set).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.button_save_condition).setVisibility(View.VISIBLE);
					viewCustomization.findViewById(R.id.set_mask_info).setVisibility(View.GONE);
					viewCustomization.findViewById(R.id.new_mask_subrule).setVisibility(View.GONE);

					TextInputLayout tvRuleName = viewCustomization.findViewById(R.id.rule_name),
							tvRuleVersion = viewCustomization.findViewById(R.id.rule_version),
							tvRuleForVersion = viewCustomization.findViewById(R.id.rule_for_version);
					Button btSave = viewCustomization.findViewById(R.id.button_save_condition);
					Objects.requireNonNull(tvRuleName.getEditText()).setText(mix.get(position).getTitle());
					Objects.requireNonNull(tvRuleVersion.getEditText()).setText(mix.get(position).getVersion());
					Objects.requireNonNull(tvRuleForVersion.getEditText()).setText(mix.get(position).getForVersion());
					btSave.setOnClickListener(v -> {
						mix.get(position).setTitle(tvRuleName.getEditText().getText().toString());
						mix.get(position).setVersion(tvRuleVersion.getEditText().getText().toString());
						mix.get(position).setForVersion(tvRuleForVersion.getEditText().getText().toString());

						SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
						ruleInitEditor.putString("mixed", gson.toJson(mix));
						ruleInitEditor.apply();
					});
				}
			};

			@SuppressLint("CutPasteId") final Button btConditionSet = viewCustomization.findViewById(R.id.condition_set);
			btConditionSet.setOnClickListener(v -> {
				mode.set(2);
				isSettingCondition.set(true);
				modeChanger.accept("1");
			});

			final Button btSetMaskInfo = viewCustomization.findViewById(R.id.set_mask_info);
			btSetMaskInfo.setOnClickListener(v -> {
				mode.set(3);
				isSettingCondition.set(false);
				modeChanger.accept("");
			});

			final ImageButton btBack = viewCustomization.findViewById(R.id.mixed_back);
			btBack.setOnClickListener(v -> {
				if (mode.get() != 3) mode.decrementAndGet();
				else {
					mode.set(0);
					try {
						customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
						windowManager.updateViewLayout(viewCustomization, customizationParams);
					} catch (Exception e) {
						le(e.getLocalizedMessage());
					}
				}
				modeChanger.accept("");
				layoutOverlayOutline.removeAllViews();
				layoutLastTimeChoiceOutline.removeView(lastChoice);
				btShowMaskOutline.setText("显示布局");
			});

			final TextView tvPressToMove = viewCustomization.findViewById(R.id.press_here_move);
			tvPressToMove.setOnTouchListener(new View.OnTouchListener() {
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

			final ImageButton btQuit = viewCustomization.findViewById(R.id.button_quit);
			btQuit.setOnClickListener(v -> {
				windowManager.removeViewImmediate(viewCustomization);
				windowManager.removeViewImmediate(viewTarget);
				windowManager.removeViewImmediate(viewLastTimeChoice);
				MixedExecutorService.isServiceRunning = MainActivity.sharedPreferences.getBoolean("master-switch", true);
				callBack.onQuit(position, mix);
			});

			try {
				windowManager.addView(viewTarget, outlineParams);
				windowManager.addView(viewLastTimeChoice, lastTimeFrameParams);
				windowManager.addView(viewCustomization, customizationParams);
			} catch (Exception e) {
				le(e.getLocalizedMessage());
			}

			modeChanger.accept("");
		}
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
		void onQuit(int position, List<MixedRuleInfo> list);
	}
}
