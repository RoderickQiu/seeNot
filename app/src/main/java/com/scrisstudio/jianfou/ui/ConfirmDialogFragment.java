package com.scrisstudio.jianfou.ui;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.scrisstudio.jianfou.R;

public class ConfirmDialogFragment extends DialogFragment {
	public static final String TAG = "Jianfou-Dialog";
	private static String type, info;
	private static OnSubmitListener callBack;
	private Button okButton;
	private Button regretButton;
	private TextView textView;

	public static void display(FragmentManager fragmentManager, String t, String i) {
		ConfirmDialogFragment dialog = new ConfirmDialogFragment();
		dialog.show(fragmentManager, TAG);
		type = t;
		info = i;
	}

	public static void setOnSubmitListener(OnSubmitListener callback) {
		callBack = callback;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog != null) {
			dialog.getWindow().setWindowAnimations(R.style.Theme_Jianfou_Slide);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = null;
		view = inflater.inflate(R.layout.confirm_dialog, container, false);
		if (view != null) {
			okButton = view.findViewById(R.id.ok_button);
			regretButton = view.findViewById(R.id.regret_button);
			textView = view.findViewById(R.id.info_box);
		}
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		textView.setText(info);

		okButton.setOnClickListener((v) -> {
			callBack.onSubmit(true);
			dismiss();
		});

		regretButton.setOnClickListener((v) -> {
			callBack.onSubmit(false);
			dismiss();
		});
	}

	// This interface can be implemented by the Activity, parent Fragment,
	// or a separate test implementation.
	public interface OnSubmitListener {
		void onSubmit(boolean callback);
	}
}

