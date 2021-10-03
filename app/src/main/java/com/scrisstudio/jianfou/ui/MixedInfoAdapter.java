package com.scrisstudio.jianfou.ui;
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
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.mask.MixedAssignerUtil;
import com.scrisstudio.jianfou.mask.MixedExecutorService;
import com.scrisstudio.jianfou.mask.MixedRuleInfo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MixedInfoAdapter extends RecyclerView.Adapter<MixedInfoAdapter.MyViewHolder> {

	private static final int VIEW_TYPE_EMPTY = 0;
	private static final int VIEW_TYPE_CARD = 1;
	private static List<MixedRuleInfo> mList = null;
	private final SharedPreferences sharedPreferences;
	private final Context context;
	private final Gson gson;

	public MixedInfoAdapter(Context context, List<MixedRuleInfo> list, SharedPreferences sharedPreferences) {
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
			MixedRuleInfo rule = mList.get(position);

			holder.ruleId.setContentDescription(String.valueOf(rule.getId()));
			holder.ruleTitle.setText(rule.getTitle());
			holder.ruleVersion.setText(rule.getVersion());
			holder.ruleFor.setText(rule.getFor());
			holder.ruleForVersion.setText(" (" + rule.getForVersion() + ")");
			holder.ruleSwitch.setChecked(rule.getStatus());

			holder.ruleSwitch.setOnCheckedChangeListener((v, isChecked) -> {
				SharedPreferences.Editor edit = sharedPreferences.edit();
				MixedRuleInfo newRule = mList.get(position);
				newRule.setStatus(isChecked);

				mList.set(position, newRule);
				edit.putString("rules", gson.toJson(mList));
				edit.apply();

				MixedExecutorService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-swtich", true), sharedPreferences.getBoolean("split", true));
			});

			holder.moreButton.setOnClickListener(v -> MainActivity.openSimpleDialog("more-info", MainActivity.resources.getString(R.string.rule_version_info) + rule.getVersion()));

			holder.editButton.setOnClickListener(v -> {
				MixedAssignerUtil.showActivityCustomizationDialog(0, position, 0, 0);
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

				MixedExecutorService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-swtich", true), sharedPreferences.getBoolean("split", true));

				Toast.makeText(jianfou.getAppContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
			});
		}

	}

	@SuppressLint("NotifyDataSetChanged")
	public void dataChange(List<MixedRuleInfo> l) {
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
		TextView ruleTitle, ruleVersion, ruleFor, ruleForVersion, ruleId;
		ImageButton moreButton, editButton, deleteButton;
		Button deleteRecheckButton;
		SwitchMaterial ruleSwitch;

		public MyViewHolder(View view) {
			super(view);
			ruleId = view.findViewById(R.id.rule_id);
			ruleTitle = view.findViewById(R.id.rule_title);
			ruleVersion = view.findViewById(R.id.rule_version);
			ruleFor = view.findViewById(R.id.rule_for);
			ruleForVersion = view.findViewById(R.id.rule_for_version);
			moreButton = view.findViewById(R.id.more_button);
			editButton = view.findViewById(R.id.edit_button);
			deleteButton = view.findViewById(R.id.delete_button);
			deleteRecheckButton = view.findViewById(R.id.delete_button_recheck);
			ruleSwitch = view.findViewById(R.id.rule_switch);
		}
	}
}
