package com.scrisstudio.jianfou;
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

public class SimpleDialogFragment extends DialogFragment {
	public static final String TAG = "Jianfou-Dialog";
	private static String type, info;
	private Button okButton;
	private TextView textView;

	public static void display(FragmentManager fragmentManager, String t, String i) {
		SimpleDialogFragment dialog = new SimpleDialogFragment();
		dialog.show(fragmentManager, TAG);
		type = t;
		info = i;
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
		if (type == "more-info") {
			view = inflater.inflate(R.layout.simple_dialog, container, false);
			okButton = view.findViewById(R.id.ok_button);
			textView = view.findViewById(R.id.info_box);
		}
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		textView.setText(info);

		okButton.setOnClickListener((v) -> {
			dismiss();
		});
	}
}

