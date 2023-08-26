package com.scrisstudio.seenot.ui.rule;

import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.MODE_EXECUTOR;
import static com.scrisstudio.seenot.ui.settings.SettingsFragment.password;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scottyab.aescrypt.AESCrypt;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.RuleTimedActivity;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;
import com.scrisstudio.seenot.ui.settings.SettingsFragment;

import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuleInfoAdapter extends RecyclerView.Adapter<RuleInfoAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_CARD = 1;
    private static List<RuleInfo> mList = null;
    private static List<TimedInfo> timedList = null;
    private final SharedPreferences sharedPreferences;
    private static FragmentManager fragmentManager;
    private static Resources resources;
    private final Context context;
    private final Gson gson;

    public RuleInfoAdapter(Context context, FragmentManager fragmentManager, List<RuleInfo> mList,
                           List<TimedInfo> timedList, SharedPreferences sharedPreferences, Resources resources) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        RuleInfoAdapter.fragmentManager = fragmentManager;
        RuleInfoAdapter.resources = resources;
        RuleInfoAdapter.timedList = timedList;
        RuleInfoAdapter.mList = mList;
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
                holder.ruleTimed.setText(resources.getString(R.string.have_timed_rule_on));
            } else
                holder.ruleTimed.setVisibility(View.GONE);

            AtomicBoolean hasOpenedDialog = new AtomicBoolean(false);
            if (!rule.getStatus() && (rule.getReopenTime() != 0 && rule.getReopenTime() < new Date().getTime()))
                rule.setStatus(true);
            holder.statusSwitch.setChecked(rule.getStatus());
            holder.statusSwitch.setOnClickListener((buttonView) -> {
                if (holder.statusSwitch.isChecked()) hasOpenedDialog.set(false);
                if (holder.statusSwitch.isChecked() || SettingsFragment.checkReservation(new Preference(context), context)) {
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
                } else holder.statusSwitch.setChecked(true);
            });

            holder.editButton.setOnClickListener(v -> {
                try {
                    AssignerUtils.initAssigner(rule.getFor().equals("com.software.any") ? 0 : 1, position, 0);
                } catch (Exception e) {
                    le(e.getLocalizedMessage());
                    Toast.makeText(context, R.string.open_assigner_failed, Toast.LENGTH_LONG).show();
                }
            });

            final AlertDialog alertDialogRuleMenu;
            final String[] items = {resources.getString(R.string.delete),
                    resources.getString(R.string.output), resources.getString(R.string.timed_settings)};
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(context);
            alertBuilder.setTitle(resources.getString(R.string.perform_action_to_rule));
            alertBuilder.setItems(items, (dialogInterface, i) -> {
                switch (i) {
                    case 0: //delete
                        new MaterialAlertDialogBuilder(context)
                                .setTitle(resources.getString(R.string.confirm_delete_with_content, rule.getTitle()))
                                .setMessage(resources.getString(R.string.action_not_invertible))
                                .setNegativeButton(resources.getString(R.string.cancel), null)
                                .setPositiveButton(resources.getString(R.string.done), (dialogInterface1, i1) -> {
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
                                }).show();
                        break;
                    case 1: //export-single
                        ArrayList<RuleInfo> rulesList = gson.fromJson(MainActivity.sharedPreferences.getString("rules", "{}"), new TypeToken<ArrayList<RuleInfo>>() {
                        }.getType());
                        boolean hasDone = false;
                        for (RuleInfo r : rulesList) {
                            if (rule.getId() == r.getId()) {
                                String output = gson.toJson(rulesList.get(i)), encryptedMsg = "";
                                try {
                                    encryptedMsg = AESCrypt.encrypt(password, output);
                                    // copy to clipboard
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("copied", encryptedMsg);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_LONG).show();
                                    hasDone = true;
                                } catch (GeneralSecurityException e) {
                                    hasDone = false;
                                    le(e.getLocalizedMessage());
                                }
                                le("Output: " + encryptedMsg + ", origin:" + output);
                            }
                        }
                        if (!hasDone) {
                            Toast.makeText(context, R.string.strange_error, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2: // rule-timed
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClass(context, RuleTimedActivity.class);
                        SeeNot.getAppContext().startActivity(intent);
                        break;
                }
            });
            alertDialogRuleMenu = alertBuilder.create();
            holder.menuButton.setOnClickListener(v -> {
                alertDialogRuleMenu.show();
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
        ImageButton editButton, menuButton;
        MaterialSwitch statusSwitch;

        public MyViewHolder(View view) {
            super(view);
            ruleTitle = view.findViewById(R.id.rule_title);
            ruleId = view.findViewById(R.id.rule_id);
            ruleFor = view.findViewById(R.id.rule_for);
            ruleTimed = view.findViewById(R.id.rule_timed);
            editButton = view.findViewById(R.id.edit_button);
            menuButton = view.findViewById(R.id.rule_menu_button);
            statusSwitch = view.findViewById(R.id.rule_status_switch);
        }
    }
}

