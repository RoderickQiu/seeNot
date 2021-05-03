package com.scrisstudio.jianfou.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.mask.ActivitySeekerService;
import com.scrisstudio.jianfou.mask.MaskAssignerUtils;
import com.scrisstudio.jianfou.mask.RuleInfo;
import com.scrisstudio.jianfou.mask.WidgetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//https://github.com/Schalex1998/Android-FullScreen-Dialog
public class FullscreenDialogFragment extends DialogFragment {

	public static final String TAG = "Jianfou-Dialog";
	private static int position;
	private static SharedPreferences sharedPreferences;
	private static List<RuleInfo> list = new ArrayList<>();
	private static OnSubmitListener callBack;
	private static int ruleType = 0;
	private static boolean hasJustRevertedRuleType = false;
	private final Gson gson = new Gson();
	private View dialogView;
	private Toolbar toolbar;

	public static void display(FragmentManager fragmentManager, int pos, List<RuleInfo> l) {
		FullscreenDialogFragment dialog = new FullscreenDialogFragment();
		dialog.show(fragmentManager, TAG);
		position = pos;
		list = l;
	}

	public static void setOnSubmitListener(OnSubmitListener callback) {
		callBack = callback;
	}

	public void openRuleSetConfirmDialog(String type, String info, int formerChoice, int choice) {
		ConfirmDialogFragment.display(MainActivity.fragmentManager, type, info);
		ConfirmDialogFragment.setOnSubmitListener((callback -> {
			if (callback) {
				ruleType = choice;

				//delete the former settings
				RuleInfo rule = list.get(position);
				rule.setSkipText(null);
				rule.setAidText(null);
				rule.setFilter(new WidgetInfo());
				list.set(position, rule);
			} else {
				//revert the choice
				hasJustRevertedRuleType = true;
				try {
					if (formerChoice == 0) {
						if (choice == 1) {
							((RadioButton) dialogView.findViewById(R.id.radio_normal_mask)).toggle();
						}
					} else if (formerChoice == 1) {
						if (choice == 0) {
							((RadioButton) dialogView.findViewById(R.id.radio_simple_return)).toggle();
						}
					}
				} catch (Exception ignored) {
				}
			}
		}));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Jianfou);
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog != null) {
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setLayout(width, height);
			dialog.getWindow().setWindowAnimations(R.style.Theme_Jianfou_Slide);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.dialog_card_edit, container, false);
		toolbar = view.findViewById(R.id.toolbar);
		return view;
	}

	private void submitData(@NonNull View view) {
		RuleInfo rule = list.get(position);
		rule.setTitle(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_name)).getEditText()).getText()));
		rule.setVersion(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_version)).getEditText()).getText()));
		rule.setType(ruleType);
		rule.setFor(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_for)).getEditText()).getText()));
		rule.setForVersion(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_for_version)).getEditText()).getText()));
		list.set(position, rule);

		callBack.onSubmit(position, list);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		toolbar.setNavigationOnClickListener(v -> dismiss());
		toolbar.setTitle(R.string.edit_dialog_title);
		toolbar.inflateMenu(R.menu.menu_dialog_bar);
		toolbar.setOnMenuItemClickListener(item -> {
			submitData(view);
			dismiss();
			return true;
		});

		dialogView = view;
		view.findViewById(R.id.rule_extra_settings_tip).setOnClickListener(v -> {
			try {
				if (ActivitySeekerService.isServiceRunning) {
					submitData(view);
					MaskAssignerUtils.showActivityCustomizationDialog(position);
				} else {
					Toast.makeText(jianfou.getAppContext(), R.string.cannot_open_assigner, Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(jianfou.getAppContext(), R.string.cannot_open_assigner, Toast.LENGTH_LONG).show();
				Log.e(TAG, e.toString());
			}
			dismiss();
		});

		ruleType = list.get(position).getType();
		if (ruleType == 0) ((RadioButton) view.findViewById(R.id.radio_normal_mask)).toggle();
		else if (ruleType == 1)
			((RadioButton) view.findViewById(R.id.radio_simple_return)).toggle();
		((RadioGroup) view.findViewById(R.id.radio_type_select)).setOnCheckedChangeListener((group, checkedId) -> {
			if (hasJustRevertedRuleType) hasJustRevertedRuleType = false;
			else if (checkedId == view.findViewById(R.id.radio_normal_mask).getId()) {
				openRuleSetConfirmDialog("radio-rule-type", "这将会删除原有的规则逻辑，请谨慎。", ruleType, 0);
			} else if (checkedId == view.findViewById(R.id.radio_simple_return).getId()) {
				openRuleSetConfirmDialog("radio-rule-type", "这将会删除原有的规则逻辑，请谨慎。", ruleType, 1);
			}
		});

		Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_name)).getEditText()).setText(list.get(position).getTitle());
		Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_version)).getEditText()).setText(list.get(position).getVersion());
		Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_for)).getEditText()).setText(list.get(position).getFor());
		Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_for_version)).getEditText()).setText(list.get(position).getForVersion());
	}

	// This interface can be implemented by the Activity, parent Fragment,
	// or a separate test implementation.
	public interface OnSubmitListener {
		void onSubmit(int position, List<RuleInfo> list);
	}
}
