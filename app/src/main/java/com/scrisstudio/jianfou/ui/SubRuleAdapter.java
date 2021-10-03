package com.scrisstudio.jianfou.ui;
import static com.scrisstudio.jianfou.jianfou.getRuleTypeRealName;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.info.MixedRuleInfo;
import com.scrisstudio.jianfou.info.SubRuleInfo;
import com.scrisstudio.jianfou.mask.MixedAssignerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SubRuleAdapter extends RecyclerView.Adapter<SubRuleAdapter.MyViewHolder> {

	private static final int VIEW_TYPE_EMPTY = 0;
	private static final int VIEW_TYPE_CARD = 1;
	private final SharedPreferences sharedPreferences;
	private final LayoutInflater inflater;
	private final Gson gson;
	private final MixedRuleInfo rule;
	private final List<MixedRuleInfo> mix;
	private final int ruleId;
	private ArrayList<SubRuleInfo> sub;

	public SubRuleAdapter(LayoutInflater inflater, List<MixedRuleInfo> mix, int ruleId, SharedPreferences sharedPreferences) {
		this.inflater = inflater;
		this.sharedPreferences = sharedPreferences;
		this.mix = mix;
		this.ruleId = ruleId;
		this.rule = mix.get(ruleId);
		gson = new Gson();
	}

	@Override
	public int getItemViewType(int position) {
		if (rule.getSubRuleLength() == 0) {
			return VIEW_TYPE_EMPTY;
		} else {
			return VIEW_TYPE_CARD;
		}
	}

	@NonNull
	@Override
	public SubRuleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		if (viewType == VIEW_TYPE_CARD) {
			view = inflater.inflate(R.layout.layout_sub_rule, parent, false);
		} else {
			view = inflater.inflate(R.layout.layout_empty_recyclerview, parent, false);
			((TextView) view.findViewById(R.id.empty_recycler_msg)).setText(R.string.empty_subrules);
		}
		return new SubRuleAdapter.MyViewHolder(view);
	}

	@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
	@Override
	public void onBindViewHolder(@NonNull SubRuleAdapter.MyViewHolder holder, int pos) {
		int viewType = getItemViewType(pos);
		if (viewType == VIEW_TYPE_CARD) {
			sub = rule.getSubRules();

			holder.titleText.setText(getRuleTypeRealName(sub.get(pos).getType()));
			holder.setButton.setOnClickListener(v -> {
				MixedAssignerUtil.showActivityCustomizationDialog(1, ruleId, pos, 0);
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
				sub.remove(pos);
				rule.setSubRules(sub);
				rule.setSubRuleLength(rule.getSubRuleLength() - 1);
				mix.set(ruleId, rule);
				notifyDataSetChanged();

				edit.putString("mixed", gson.toJson(mix));
				edit.apply();
			});
		}

	}

	@SuppressLint("NotifyDataSetChanged")
	public void dataChange(ArrayList<SubRuleInfo> l) {
		rule.setSubRules(l);
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		if (rule.getSubRuleLength() == 0) {
			return 1;
		} else {
			return rule.getSubRuleLength();
		}
	}

	static class MyViewHolder extends RecyclerView.ViewHolder {
		Button deleteRecheckButton, deleteButton, setButton;
		TextView titleText, caption1Text, caption2Text;

		public MyViewHolder(View view) {
			super(view);
			deleteButton = view.findViewById(R.id.subrule_delete);
			deleteRecheckButton = view.findViewById(R.id.subrule_delete_check);
			titleText = view.findViewById(R.id.subrule_type);
			caption1Text = view.findViewById(R.id.subrule_caption1);
			caption2Text = view.findViewById(R.id.subrule_caption2);
			setButton = view.findViewById(R.id.subrule_set);
		}
	}
}
