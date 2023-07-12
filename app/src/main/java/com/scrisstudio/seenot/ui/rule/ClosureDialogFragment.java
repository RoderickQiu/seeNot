package com.scrisstudio.seenot.ui.rule;

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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ClosureDialogFragment extends DialogFragment {
    public static final String TAG = "SeeNot-Dialog";
    private static int position;
    private static int spinnerSelect = -1;
    private static OnSubmitListener callBack;
    private static SharedPreferences sharedPreferences;
    private static ArrayList<RuleInfo> rules;
    private static ArrayList<TimedInfo> timed;
    private static final Gson gson = new Gson();
    private static WeakReference<MaterialSwitch> statusSwitch;
    private Button okButton;
    private ChipGroup typeChips;
    private TextInputLayout reopenTime;
    private RelativeLayout closureView;

    public static void display(FragmentManager fragmentManager, int pos,
                               WeakReference<MaterialSwitch> status, SharedPreferences sp) {
        ClosureDialogFragment dialog = new ClosureDialogFragment();
        position = pos;
        sharedPreferences = sp;
        statusSwitch = status;
        timed = gson.fromJson(sharedPreferences.getString("timed", "{}"), new TypeToken<ArrayList<TimedInfo>>() {
        }.getType());
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());
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
        view = inflater.inflate(R.layout.dialog_closure, container, false);
        if (view != null) {
            okButton = view.findViewById(R.id.closure_ok_button);
            typeChips = view.findViewById(R.id.closure_type_sel);
            reopenTime = view.findViewById(R.id.set_reopen_time);
            closureView = view.findViewById(R.id.closure_view);
        }
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RuleInfo cur = rules.get(position);

        typeChips.getChildAt(0).setOnClickListener(v -> {
            ((Chip) typeChips.getChildAt(1)).setChecked(false);
            cur.setStatus(false);
            reopenTime.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = closureView.getLayoutParams();
            layoutParams.width = closureView.getWidth();
            layoutParams.height = MainActivity.dip2px(270);
            closureView.setLayoutParams(layoutParams);
        });
        typeChips.getChildAt(1).setOnClickListener(v -> {
            ((Chip) typeChips.getChildAt(0)).setChecked(false);
            cur.setStatus(false);
            cur.setReopenTime(0);
            reopenTime.setVisibility(View.GONE);
            ViewGroup.LayoutParams layoutParams = closureView.getLayoutParams();
            layoutParams.width = closureView.getWidth();
            layoutParams.height = MainActivity.dip2px(200);
            closureView.setLayoutParams(layoutParams);
        });

        Objects.requireNonNull(reopenTime.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable inputContent) {
                ((Chip) typeChips.getChildAt(0)).setChecked(true);
                if (inputContent != null) {
                    reopenTime.getEditText().removeTextChangedListener(this);
                    if (inputContent.toString().trim().equals("小时")) {
                        reopenTime.getEditText().setText("");
                        cur.setReopenTime(0);
                    } else {
                        String ss = inputContent.toString();
                        int len = ss.replace(" 小时", "").length();
                        ss = ss.replace(" 小时", "") + " 小时";
                        reopenTime.getEditText().setText(ss);
                        Selection.setSelection(reopenTime.getEditText().getText(), len);
                        if (!ss.endsWith(". 小时")) cur.setReopenTime(new Date().getTime() +
                                (long) (3600000 * Float.parseFloat(ss.replace(" 小时", ""))));
                    }
                    reopenTime.getEditText().addTextChangedListener(this);
                }
            }
        });

        okButton.setOnClickListener(v -> {
            if ((!((Chip) typeChips.getChildAt(0)).isChecked() && !((Chip) typeChips.getChildAt(1)).isChecked())
                    || ((Chip) typeChips.getChildAt(0)).isChecked() && reopenTime.getEditText().getText().length() == 0) {
                Toast.makeText(SeeNot.getAppContext(), "还没设置完呢，请重试", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor edit = sharedPreferences.edit();
            rules.set(position, cur);

            edit.putString("rules", gson.toJson(rules));
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

