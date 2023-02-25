package com.scrisstudio.seenot.ui.settings;

import static com.scrisstudio.seenot.service.ExecutorService.MODE_EXECUTOR;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;

import java.util.Objects;

public class RLockDialogFragment extends DialogFragment {
    public static final String TAG = "SeeNot-Dialog";
    private static OnSubmitListener callBack;
    private static SharedPreferences sharedPreferences;
    private static final Gson gson = new Gson();
    private Button okButton;
    private TextInputLayout timeTextInput;
    private RelativeLayout rLockView;

    public static void display(FragmentManager fragmentManager, SharedPreferences sp) {
        RLockDialogFragment dialog = new RLockDialogFragment();
        sharedPreferences = sp;
        dialog.show(fragmentManager, TAG);
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
            dialog.getWindow().setWindowAnimations(R.style.Theme_SeeNot_Slide);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = null;
        view = inflater.inflate(R.layout.dialog_rlock, container, false);
        if (view != null) {
            okButton = view.findViewById(R.id.rlock_ok_button);
            timeTextInput = view.findViewById(R.id.set_rlock_duration);
            rLockView = view.findViewById(R.id.rlock_view);
        }
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(timeTextInput.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable inputContent) {
                if (inputContent != null) {
                    timeTextInput.getEditText().removeTextChangedListener(this);
                    if (inputContent.toString().trim().equals("分钟")) {
                        timeTextInput.getEditText().setText("");
                        //cur.setReopenTime(0);
                    } else {
                        String ss = inputContent.toString();
                        int len = ss.replace(" 分钟", "").length();
                        ss = ss.replace(" 分钟", "") + " 分钟";
                        timeTextInput.getEditText().setText(ss);
                        Selection.setSelection(timeTextInput.getEditText().getText(), len);
                        //if (!ss.endsWith(". 分钟")) cur.setReopenTime(new Date().getTime() +
                        //        (long) (3600000 * Float.parseFloat(ss.replace(" 分钟", ""))));
                    }
                    timeTextInput.getEditText().addTextChangedListener(this);
                }
            }
        });

        Objects.requireNonNull(timeTextInput.getEditText())
                .setText(sharedPreferences.getString("reservation-lock", "0"));

        okButton.setOnClickListener(v -> {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("reservation-lock",
                    timeTextInput.getEditText().getText().toString().replace(" 分钟", ""));
            edit.apply();

            ExecutorService.setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
            AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
            MainActivity.setSharedPreferences(sharedPreferences);

            Toast.makeText(SeeNot.getAppContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
            callBack.onSubmit();
            dismiss();
        });
    }

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    public interface OnSubmitListener {
        void onSubmit();
    }
}

