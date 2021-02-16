package com.scrisstudio.jianfou;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class RuleInfoAdapter extends RecyclerView.Adapter<RuleInfoAdapter.MyViewHolder> {

	private static final String TAG = "Jianfou-RuleInfoAdapter";
	private static Context context;
	private static List<RuleInfo> mList;
	private static ruleListTriggerCallback callback;

	public RuleInfoAdapter(Context context, List<RuleInfo> list, ruleListTriggerCallback callback) {
		this.context = context;
		this.mList = list;
		this.callback = callback;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.rule_card, parent, false);
		MyViewHolder holder = new MyViewHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		RuleInfo rule = mList.get(position);
		holder.ruleId.setContentDescription(String.valueOf(rule.getId()));
		holder.ruleTitle.setText(rule.getTitle());
		holder.ruleFor.setText(rule.getFor());
		holder.ruleType.setText(rule.getType());
		holder.ruleSwitch.setChecked(rule.getStatus());
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

			ruleSwitch.setOnCheckedChangeListener((v, isChecked) -> {
				callback.callback(view, "rule_switch");
			});

			editButton.setOnClickListener(v -> {
				Toast.makeText(context, "还没有完成。", Toast.LENGTH_LONG).show();
			});

			deleteButton.setOnClickListener(v -> {
				Toast.makeText(context, "还没有完成。", Toast.LENGTH_LONG).show();
			});
		}

	}
}
