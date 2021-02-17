package com.scrisstudio.jianfou;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import java.util.List;

public class RuleInfoAdapter extends RecyclerView.Adapter<RuleInfoAdapter.MyViewHolder> {

	private static final String TAG = "Jianfou-RuleInfoAdapter";
	private final SharedPreferences sharedPreferences;
	private final Context context;
	private final Gson gson;
	private final List<RuleInfo> mList;

	public RuleInfoAdapter(Context context, List<RuleInfo> list, SharedPreferences sharedPreferences) {
		this.context = context;
		this.mList = list;
		this.sharedPreferences = sharedPreferences;
		gson = new Gson();
	}

	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.rule_card, parent, false);
		return new MyViewHolder(view);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		RuleInfo rule = mList.get(position);

		holder.ruleId.setContentDescription(String.valueOf(rule.getId()));
		holder.ruleTitle.setText(rule.getTitle());
		holder.ruleFor.setText(rule.getFor());
		holder.ruleType.setText(rule.getType());
		holder.ruleSwitch.setChecked(rule.getStatus());

		holder.ruleSwitch.setOnCheckedChangeListener((v, isChecked) -> {
			SharedPreferences.Editor edit = sharedPreferences.edit();
			RuleInfo newRule = mList.get(position);
			newRule.setStatus(isChecked);

			mList.set(position, newRule);
			edit.putString("rules", gson.toJson(mList));
			edit.apply();
		});

		holder.editButton.setOnClickListener(v -> {
			Toast.makeText(context, "还没有完成。", Toast.LENGTH_LONG).show();
		});

		holder.deleteButton.setOnClickListener(v -> {
			try {
				SharedPreferences.Editor edit = sharedPreferences.edit();
				mList.remove(position);
				notifyDataSetChanged();

				edit.putString("rules", gson.toJson(mList));
				edit.apply();
			} catch (Exception e) {
				Log.e(TAG, String.valueOf(e));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mList.size();
	}

	static class MyViewHolder extends RecyclerView.ViewHolder {
		TextView ruleTitle, ruleFor, ruleType, ruleId;
		Button editButton, deleteButton;
		SwitchMaterial ruleSwitch;

		public MyViewHolder(View view) {
			super(view);
			ruleId = view.findViewById(R.id.rule_id);
			ruleTitle = view.findViewById(R.id.rule_title);
			ruleFor = view.findViewById(R.id.rule_for);
			ruleType = view.findViewById(R.id.rule_type);
			editButton = view.findViewById(R.id.edit_button);
			deleteButton = view.findViewById(R.id.delete_button);
			ruleSwitch = view.findViewById(R.id.rule_switch);
		}
	}
}
