package com.scrisstudio.seenot.ui.timed;

import static com.scrisstudio.seenot.service.ExecutorService.MODE_EXECUTOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.RuleTimedActivity;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.service.RuleInfo;
import com.scrisstudio.seenot.service.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RuleTimedAdapter extends RecyclerView.Adapter<RuleTimedAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_CARD = 1;
    private static List<TimedInfo> mList = null;
    private static List<RuleInfo> ruleList = null;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private final Gson gson;

    public RuleTimedAdapter(Context context, List<TimedInfo> timedList, List<RuleInfo> ruleInfoList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        mList = timedList;
        ruleList = ruleInfoList;
        this.gson = new Gson();
    }

    private static OnEditListener callBack;

    public static void setOnEditListener(OnEditListener callback) {
        callBack = callback;
    }

    public interface OnEditListener {
        void onEdit(int position, List<TimedInfo> list, int mode);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_CARD) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_timed_card, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.layout_empty_recyclerview, parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_CARD;
        }
    }

    private RuleInfo getForRule(int in) {
        for (RuleInfo i : ruleList) {
            if (i.getId() == in) return i;
        }
        return new RuleInfo(0, true, "新建规则", "com.software.any", "未设置", new ArrayList<>(), 0);
    }

    public static ArrayList<Integer> getRealScope(int scope) {
        ArrayList<Integer> result = new ArrayList<>();
        int[] binary = {1, 2, 4, 8, 16, 32, 64};
        for (int i = 0; i < 7; i++) {
            if ((binary[i] & scope) > 0) result.add(i + 1);
        }
        return result;
    }

    public static int getStockedScope(ArrayList<Integer> scopeList) {
        int result = 0;
        for (int i : scopeList) {
            result += Math.pow(2, i - 1);
        }
        return result;
    }

    private static String realScopeParser(ArrayList<Integer> list) {
        StringBuilder builder = new StringBuilder("");
        String[] week = {"一", "二", "三", "四", "五", "六", "日"};
        if (list.size() == 0) {
            builder.append("仅当天");
        } else if (list.size() == 7) {
            builder.append("每天");
        } else {
            builder.append("周");
            for (int i = 0; i < list.size(); i++) {
                builder.append(week[list.get(i) - 1]);
                if (i != list.size() - 1) builder.append("/");
            }
        }
        return builder.toString();
    }

    private static String timeParser(Long time) {
        Date date = new Date(time);
        return new SimpleDateFormat("HH:mm", Locale.CHINA).format(date);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_CARD) {
            TimedInfo rule = mList.get(position);
            RuleInfo corresp = getForRule(rule.getIdFor());

            holder.ruleId.setContentDescription(String.valueOf(rule.getId()));
            holder.ruleTitle.setText(rule.getName() + " (" + corresp.getForName() + ")");
            holder.ruleDescription.setText(realScopeParser(getRealScope(rule.getScope())) + " " +
                    timeParser(rule.getStartTime()) + "~" + timeParser(rule.getEndTime()));

            holder.statusSwitch.setChecked(rule.getStatus());
            holder.statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor edit = sharedPreferences.edit();

                rule.setStatus(isChecked);
                rule.setFirstLaunchTime(new Date().getTime());
                mList.set(position, rule);
                edit.putString("timed", gson.toJson(mList));
                edit.apply();

                ExecutorService.setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
                AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
                MainActivity.setSharedPreferences(sharedPreferences);
                callBack.onEdit(position, mList, MODE_EXECUTOR);
            });

            holder.deleteButton.setOnClickListener(v -> {
                holder.deleteButton.setVisibility(View.GONE);
                holder.deleteRecheckButton.setVisibility(View.VISIBLE);

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            MainActivity.runOnUI(() -> {
                                holder.deleteButton.setVisibility(View.VISIBLE);
                                holder.deleteRecheckButton.setVisibility(View.GONE);
                            });
                        } catch (Exception ignored) {
                        }
                    }
                }, 3000);
            });
            holder.deleteRecheckButton.setOnClickListener(v -> {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                mList.remove(position);
                notifyDataSetChanged();

                edit.putString("timed", gson.toJson(mList));
                edit.apply();

                ExecutorService.setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
                AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
                MainActivity.setSharedPreferences(sharedPreferences);
                callBack.onEdit(position, mList, MODE_EXECUTOR);

                Toast.makeText(context.getApplicationContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
            });

            holder.editButton.setOnClickListener(v -> {
                RuleTimedActivity.openEditDialog(position, sharedPreferences);
                EditDialogFragment.setOnSubmitListener(() -> {
                });
            });
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void dataChange(List<TimedInfo> l) {
        mList = l;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mList.size() == 0) {
            return 1;
        } else {
            return mList.size();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView ruleId, ruleTitle, ruleDescription;
        ImageButton editButton, deleteButton;
        Button deleteRecheckButton;
        SwitchMaterial statusSwitch;

        public MyViewHolder(View view) {
            super(view);
            ruleTitle = view.findViewById(R.id.timed_title);
            ruleId = view.findViewById(R.id.timed_id);
            ruleDescription = view.findViewById(R.id.timed_description);
            editButton = view.findViewById(R.id.edit_button);
            deleteButton = view.findViewById(R.id.delete_button);
            deleteRecheckButton = view.findViewById(R.id.delete_button_recheck);
            statusSwitch = view.findViewById(R.id.timed_status_switch);
        }
    }
}

