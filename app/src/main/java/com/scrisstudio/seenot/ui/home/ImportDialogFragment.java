package com.scrisstudio.seenot.ui.home;

import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.ui.settings.SettingsFragment.password;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.google.gson.reflect.TypeToken;
import com.scottyab.aescrypt.AESCrypt;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.struct.RuleInfo;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;

public class ImportDialogFragment extends DialogFragment {
    public static final String TAG = "SeeNot-Dialog";
    private static OnSubmitListener callBack;
    private static ArrayList<RuleInfo> rules;
    private static SharedPreferences sharedPreferences;
    private static final Gson gson = new Gson();
    private Button okButton;
    private TextInputLayout timeTextInput;
    private RelativeLayout pwdView;

    public static void display(FragmentManager fragmentManager, SharedPreferences sp) {
        ImportDialogFragment dialog = new ImportDialogFragment();
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
        view = inflater.inflate(R.layout.dialog_import_rule, container, false);
        if (view != null) {
            okButton = view.findViewById(R.id.import_ok_button);
            timeTextInput = view.findViewById(R.id.set_single_rule);
            pwdView = view.findViewById(R.id.import_view);
        }
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<ArrayList<RuleInfo>>() {
        }.getType());
        okButton.setOnClickListener(v -> {
            try {
                try {
                    String messageAfterDecrypt = AESCrypt.decrypt(password,
                            Objects.requireNonNull(timeTextInput.getEditText()).getText().toString());
                    RuleInfo rule = gson.fromJson(messageAfterDecrypt, new TypeToken<RuleInfo>() {
                    }.getType());
                    rule.setId(sharedPreferences.getInt("rule-id-max", 0));
                    rules.add(rule);
                    l("Imported: " + rule);
                    Toast.makeText(SeeNot.getAppContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
                } catch (GeneralSecurityException e) {
                    le("INNER ERR: " + e.getLocalizedMessage());
                    Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                le("OUTER ERR: " + e.getLocalizedMessage());
                Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_SHORT).show();
            }

            callBack.onSubmit(rules);
            dismiss();
        });
    }

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    public interface OnSubmitListener {
        void onSubmit(ArrayList<RuleInfo> rules);
    }
}

