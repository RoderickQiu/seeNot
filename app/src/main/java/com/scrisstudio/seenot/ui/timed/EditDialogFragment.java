package com.scrisstudio.seenot.ui.timed;

import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.MODE_EXECUTOR;
import static com.scrisstudio.seenot.ui.timed.RuleTimedAdapter.getRealScope;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.RuleTimedActivity;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EditDialogFragment extends DialogFragment {
    public static final String TAG = "SeeNot-Dialog";
    private static int position;
    private static int spinnerSelect = -1;
    private static OnSubmitListener callBack;
    private static SharedPreferences sharedPreferences;
    private static ArrayList<RuleInfo> rules;
    private static ArrayList<TimedInfo> timed;
    private static final Gson gson = new Gson();
    private Button okButton;
    private SmartMaterialSpinner<String> spinner;
    private ChipGroup weekChips, statusChips;
    private TimePicker pickerStart, pickerEnd;
    private TextInputLayout title;
    private Date startDate, endDate;

    public static void display(FragmentManager fragmentManager, int pos, SharedPreferences sp) {
        EditDialogFragment dialog = new EditDialogFragment();
        position = pos;
        sharedPreferences = sp;
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
        view = inflater.inflate(R.layout.dialog_timed_edit, container, false);
        if (view != null) {
            title = view.findViewById(R.id.set_timed_title);
            spinner = view.findViewById(R.id.rule_for_spinner);
            weekChips = view.findViewById(R.id.timed_week_sel);
            statusChips = view.findViewById(R.id.timed_mode_sel);
            pickerStart = view.findViewById(R.id.timed_picker_start);
            pickerEnd = view.findViewById(R.id.timed_picker_end);
            okButton = view.findViewById(R.id.timed_ok_button);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimedInfo cur = timed.get(position);

        Objects.requireNonNull(title.getEditText()).setText(cur.getName().equals(getResources().getString(R.string.untitled)) ? "" : cur.getName());

        ArrayList<String> rulesList = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            RuleInfo rule = rules.get(i);
            rulesList.add(rule.getTitle() + " (" + rule.getForName() + ")");
            if (rule.getId() == cur.getIdFor()) spinnerSelect = i;
        }
        spinner.setItem(rulesList);
        if (spinnerSelect != -1) spinner.setSelection(spinnerSelect);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerSelect = (int) id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnerSelect = -1;
            }
        });

        if (cur.getMode()) ((Chip) statusChips.getChildAt(0)).setChecked(true);
        else ((Chip) statusChips.getChildAt(1)).setChecked(true);
        statusChips.getChildAt(0).setOnClickListener(v -> {
            ((Chip) statusChips.getChildAt(1)).setChecked(false);
        });
        statusChips.getChildAt(1).setOnClickListener(v -> {
            ((Chip) statusChips.getChildAt(0)).setChecked(false);
        });

        ArrayList<Integer> realScope = getRealScope(cur.getScope());
        if (realScope.size() > 0)
            for (int day : realScope) {
                ((Chip) weekChips.getChildAt(day - 1)).setChecked(true);
            }
        else
            ((Chip) weekChips.getChildAt(7)).setChecked(true);
        for (int i = 0; i < 7; i++) {
            weekChips.getChildAt(i).setOnClickListener((v) -> {
                if (((Chip) weekChips.getChildAt(7)).isChecked())
                    ((Chip) weekChips.getChildAt(7)).setChecked(false);
            });
        }
        weekChips.getChildAt(7).setOnClickListener((v) -> {
            for (int i = 0; i < 7; i++) {
                if (((Chip) weekChips.getChildAt(i)).isChecked())
                    ((Chip) weekChips.getChildAt(i)).setChecked(false);
            }
        });

        pickerStart.setIs24HourView(true);
        pickerEnd.setIs24HourView(true);
        resizePicker(pickerStart);
        resizePicker(pickerEnd);
        startDate = new Date(cur.getStartTime());
        endDate = new Date(cur.getEndTime());
        pickerStart.setHour(Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(startDate)));
        pickerStart.setMinute(Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(startDate)));
        pickerEnd.setHour(Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(endDate)));
        pickerEnd.setMinute(Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(endDate)));

        okButton.setOnClickListener(v -> {
            cur.setName(String.valueOf(title.getEditText().getText()).equals("") ? "未命名"
                    : String.valueOf(title.getEditText().getText()));

            ArrayList<Integer> stocked = new ArrayList<>();
            if (!((Chip) weekChips.getChildAt(7)).isChecked())
                for (int i = 0; i < 7; i++) {
                    if (((Chip) weekChips.getChildAt(i)).isChecked())
                        stocked.add(i + 1);
                }
            cur.setScope(RuleTimedAdapter.getStockedScope(stocked));

            cur.setMode(((Chip) statusChips.getChildAt(0)).isChecked());

            if (spinnerSelect != -1) cur.setIdFor(rules.get(spinnerSelect).getId());

            startDate = new Date(startDate.getTime()
                    - 1000L * 60 * 60 * Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(startDate))
                    - 1000L * 60 * Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(startDate))
                    + pickerStart.getHour() * 60 * 60 * 1000L + pickerStart.getMinute() * 60 * 1000L);
            endDate = new Date(endDate.getTime()
                    - 1000L * 60 * 60 * Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(endDate))
                    - 1000L * 60 * Integer.parseInt(new SimpleDateFormat("mm", Locale.CHINA).format(endDate))
                    + pickerEnd.getHour() * 60 * 60 * 1000L + pickerEnd.getMinute() * 60 * 1000L);
            cur.setStartTime(startDate.getTime());
            cur.setEndTime(endDate.getTime());
            le("S " + new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(startDate));
            le("E " + new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(endDate));

            le(cur.toString());

            SharedPreferences.Editor edit = sharedPreferences.edit();
            timed.set(position, cur);

            edit.putString("timed", gson.toJson(timed));
            edit.apply();

            ExecutorService.setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
            AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
            MainActivity.setSharedPreferences(sharedPreferences);

            Toast.makeText(SeeNot.getAppContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();

            callBack.onSubmit();

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setClass(SeeNot.getAppContext(), RuleTimedActivity.class);
            SeeNot.getAppContext().startActivity(intent);

            dismiss();
        });
    }

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    public interface OnSubmitListener {
        void onSubmit();
    }

    private void resizePicker(FrameLayout tp) {
        List<NumberPicker> npList = findNumberPicker(tp);
        for (NumberPicker np : npList) {
            resizeNumberPicker(np);
        }
    }

    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;
        if (null != viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0) {
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    private void resizeNumberPicker(NumberPicker np) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(3, 3, 3, 3);
        np.setLayoutParams(params);
    }
}

