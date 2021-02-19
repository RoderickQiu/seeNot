package com.scrisstudio.jianfou;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

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
	private final Gson gson = new Gson();
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
		View view = inflater.inflate(R.layout.card_edit_dialog, container, false);
		toolbar = view.findViewById(R.id.toolbar);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		toolbar.setNavigationOnClickListener(v -> dismiss());
		toolbar.setTitle(R.string.edit_dialog_title);
		toolbar.inflateMenu(R.menu.top_dialog_bar);
		toolbar.setOnMenuItemClickListener(item -> {
			RuleInfo rule = list.get(position);
			rule.setTitle(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_name)).getEditText()).getText()));
			rule.setVersion(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_version)).getEditText()).getText()));
			rule.setFor(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_for)).getEditText()).getText()));
			rule.setForVersion(String.valueOf(Objects.requireNonNull(((TextInputLayout) view.findViewById(R.id.rule_for_version)).getEditText()).getText()));
			list.set(position, rule);

			callBack.onSubmit(position, list);
			dismiss();
			return true;
		});

		view.findViewById(R.id.rule_extra_settings_tip).setOnClickListener(v -> {
			Toast.makeText(jianfou.getAppContext(), "还没有完成。", Toast.LENGTH_LONG).show();
			dismiss();
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
