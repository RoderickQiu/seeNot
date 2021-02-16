package com.scrisstudio.jianfou;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class RuleInfoAdapter extends RecyclerView.Adapter<RuleInfoAdapter.MyViewHolder> {

	private static final String TAG = "Jianfou-RuleInfoAdapter";
	private static Context context;
	private List<RuleInfo> mList;

	public RuleInfoAdapter(Context context, List<RuleInfo> list) {
		this.context = context;
		this.mList = list;
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
		holder.ruleTitle.setText(rule.getTitle());
		holder.ruleFor.setText(rule.getFor());
		holder.ruleType.setText(rule.getType());
	}

	@Override
	public int getItemCount() {
		return mList.size();
	}

	static class MyViewHolder extends RecyclerView.ViewHolder {
		TextView ruleTitle, ruleFor, ruleType;
		Button editButton, deleteButton;

		public MyViewHolder(View view) {
			super(view);
			ruleTitle = view.findViewById(R.id.rule_title);
			ruleFor = view.findViewById(R.id.rule_for);
			ruleType = view.findViewById(R.id.rule_type);
			editButton = view.findViewById(R.id.edit_button);
			deleteButton = view.findViewById(R.id.delete_button);

			editButton.setOnClickListener(v -> {
				Toast.makeText(context, "还没有完成。", Toast.LENGTH_LONG).show();
			});

			deleteButton.setOnClickListener(v -> {
				Toast.makeText(context, "还没有完成。", Toast.LENGTH_LONG).show();
			});
		}

	}
}
