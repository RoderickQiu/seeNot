package com.scrisstudio.seenot.ui.rule;

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
import com.scrisstudio.seenot.service.RuleInfo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RuleInfoAdapter extends RecyclerView.Adapter<RuleInfoAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_CARD = 1;
    private static List<RuleInfo> mList = null;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private final Gson gson;

    public RuleInfoAdapter(Context context, List<RuleInfo> list, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        mList = list;
        gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_CARD) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_rule_card, parent, false);
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

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_CARD) {
            RuleInfo rule = mList.get(position);

            holder.ruleId.setContentDescription(String.valueOf(rule.getId()));
            holder.ruleTitle.setText(rule.getTitle());
            holder.ruleFor.setText(rule.getForName());
            holder.ruleSwitch.setChecked(rule.getStatus());

            holder.ruleSwitch.setOnCheckedChangeListener((v, isChecked) -> {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                RuleInfo newRule = mList.get(position);
                newRule.setStatus(isChecked);

                mList.set(position, newRule);
                edit.putString("rules", gson.toJson(mList));
                edit.apply();

                //ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-swtich", true), sharedPreferences.getBoolean("split", true));
            });

            //holder.editButton.setOnClickListener(v -> MainActivity.openCardEditDialog(position));

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

                edit.putString("rules", gson.toJson(mList));
                edit.apply();

                //ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-swtich", true), sharedPreferences.getBoolean("split", true));

                Toast.makeText(context.getApplicationContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
            });
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void dataChange(List<RuleInfo> l) {
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
        TextView ruleTitle, ruleVersion, ruleFor, ruleId;
        ImageButton editButton, deleteButton;
        Button deleteRecheckButton;
        SwitchMaterial ruleSwitch;

        public MyViewHolder(View view) {
            super(view);
            ruleId = view.findViewById(R.id.rule_id);
            ruleTitle = view.findViewById(R.id.rule_title);
            ruleVersion = view.findViewById(R.id.rule_version);
            ruleFor = view.findViewById(R.id.rule_for);
            editButton = view.findViewById(R.id.edit_button);
            deleteButton = view.findViewById(R.id.delete_button);
            deleteRecheckButton = view.findViewById(R.id.delete_button_recheck);
            ruleSwitch = view.findViewById(R.id.rule_switch);
        }
    }
}

