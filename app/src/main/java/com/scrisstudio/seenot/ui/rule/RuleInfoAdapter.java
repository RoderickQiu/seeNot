package com.scrisstudio.seenot.ui.rule;

import static com.scrisstudio.seenot.SeeNot.le;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.gson.Gson;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.service.RuleInfo;
import com.scrisstudio.seenot.service.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuleInfoAdapter extends RecyclerView.Adapter<RuleInfoAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_CARD = 1;
    private static List<RuleInfo> mList = null;
    private static List<TimedInfo> timedList = null;
    private final SharedPreferences sharedPreferences;
    private static FragmentManager fragmentManager;
    private final Context context;
    private final Gson gson;

    public RuleInfoAdapter(Context context, FragmentManager fragmentManager, List<RuleInfo> mList,
                           List<TimedInfo> timedList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.sharedPreferences = sharedPreferences;
        this.timedList = timedList;
        this.mList = mList;
        gson = new Gson();
    }

    private static OnEditListener callBack;

    public static void setOnEditListener(OnEditListener callback) {
        callBack = callback;
    }

    public interface OnEditListener {
        void onEdit(int position, List<RuleInfo> list, int mode);
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

    private boolean hasTimedInfo(int id) {
        for (TimedInfo timed : timedList) {
            if (timed.getIdFor() == id && timed.getStatus()) return true;
        }
        return false;
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

            if (hasTimedInfo(rule.getId())) {
                holder.ruleTimed.setText("有定时任务启用");
            } else
                holder.ruleTimed.setVisibility(View.GONE);

            AtomicBoolean hasOpenedDialog = new AtomicBoolean(false);
            if (!rule.getStatus() && (rule.getReopenTime() != 0 && rule.getReopenTime() < new Date().getTime()))
                rule.setStatus(true);
            holder.statusSwitch.setChecked(rule.getStatus());
            holder.statusSwitch.setOnClickListener((buttonView) -> {
                if (holder.statusSwitch.isChecked() || hasOpenedDialog.get()) {
                    SharedPreferences.Editor edit = sharedPreferences.edit();

                    rule.setStatus(holder.statusSwitch.isChecked());
                    rule.setReopenTime(0);
                    mList.set(position, rule);
                    edit.putString("rules", gson.toJson(mList));
                    edit.apply();
                    ExecutorService.setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
                    AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
                    MainActivity.setSharedPreferences(sharedPreferences);
                    callBack.onEdit(position, mList, MODE_EXECUTOR);
                } else {
                    hasOpenedDialog.set(true);
                    holder.statusSwitch.setChecked(true);
                    WeakReference<MaterialSwitch> status = new WeakReference<>(holder.statusSwitch);
                    ClosureDialogFragment.display(fragmentManager, position, status, sharedPreferences);
                    ClosureDialogFragment.setOnSubmitListener(() -> {
                        holder.statusSwitch.setChecked(false);
                    });
                }
            });

            holder.editButton.setOnClickListener(v -> {
                try {
                    AssignerUtils.initAssigner(rule.getFor().equals("com.software.any") ? 0 : 1, position, 0);
                } catch (Exception e) {
                    le(e.getLocalizedMessage());
                    Toast.makeText(context, R.string.open_assigner_failed, Toast.LENGTH_LONG).show();
                }
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

                edit.putString("rules", gson.toJson(mList));
                edit.apply();

                ExecutorService.setServiceBasicInfo(sharedPreferences, MODE_EXECUTOR);
                AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
                MainActivity.setSharedPreferences(sharedPreferences);
                callBack.onEdit(position, mList, MODE_EXECUTOR);

                Toast.makeText(context.getApplicationContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
            });
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void dataChange(List<RuleInfo> l) {
        mList = l;
        try {
            notifyDataSetChanged();
        } catch (Exception e) {
            le("ERR: " + e.getLocalizedMessage());
        }
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
        TextView ruleId, ruleTitle, ruleFor, ruleTimed;
        ImageButton editButton, deleteButton;
        Button deleteRecheckButton;
        MaterialSwitch statusSwitch;

        public MyViewHolder(View view) {
            super(view);
            ruleTitle = view.findViewById(R.id.rule_title);
            ruleId = view.findViewById(R.id.rule_id);
            ruleFor = view.findViewById(R.id.rule_for);
            ruleTimed = view.findViewById(R.id.rule_timed);
            editButton = view.findViewById(R.id.edit_button);
            deleteButton = view.findViewById(R.id.delete_button);
            deleteRecheckButton = view.findViewById(R.id.delete_button_recheck);
            statusSwitch = view.findViewById(R.id.rule_status_switch);
        }
    }
}

