package com.scrisstudio.jianfou.ui;
import static com.scrisstudio.jianfou.mask.ActivitySeekerService.l;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.MainActivity;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.mask.MixedAssignerUtil;
import com.scrisstudio.jianfou.mask.MixedRuleInfo;
import com.scrisstudio.jianfou.mask.SkipInfo;
import com.scrisstudio.jianfou.mask.SubRuleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;

public class SkipSetAdapter extends RecyclerView.Adapter<SkipSetAdapter.MyViewHolder> {

	private static final int VIEW_TYPE_EMPTY = 0;
	private static final int VIEW_TYPE_CARD = 1;
	private final SharedPreferences sharedPreferences;
	private final LayoutInflater inflater;
	private final Gson gson;
	private final List<MixedRuleInfo> mix;
	private final int current;
	private final int position;
	private SubRuleInfo sub;

	public SkipSetAdapter(LayoutInflater inflater, List<MixedRuleInfo> mix, int position, int current, SharedPreferences sharedPreferences) {
		this.inflater = inflater;
		this.sharedPreferences = sharedPreferences;
		this.mix = mix;
		this.position = position;
		this.current = current;
		this.sub = mix.get(position).getSubRules().get(current);
		gson = new Gson();
	}

	@Override
	public int getItemViewType(int position) {
		if (sub.getSkipLength() == 0) {
			return VIEW_TYPE_EMPTY;
		} else {
			return VIEW_TYPE_CARD;
		}
	}

	@NonNull
	@Override
	public SkipSetAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		if (viewType == VIEW_TYPE_CARD) {
			view = inflater.inflate(R.layout.layout_skip_set, parent, false);
		} else {
			view = inflater.inflate(R.layout.layout_empty_recyclerview, parent, false);
			((TextView) view.findViewById(R.id.empty_recycler_msg)).setVisibility(View.GONE);
		}
		return new SkipSetAdapter.MyViewHolder(view);
	}

	@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
	@Override
	public void onBindViewHolder(@NonNull SkipSetAdapter.MyViewHolder holder, int skipId) {
		int viewType = getItemViewType(skipId);
		if (viewType == VIEW_TYPE_CARD) {
			holder.setButton.setOnClickListener(v -> {
				MixedAssignerUtil.showActivityCustomizationDialog(2, position, current, skipId);
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
			BiConsumer<ArrayList<SkipInfo>, String> skipCommitter = (skipArray, m) -> {
				SharedPreferences.Editor edit = sharedPreferences.edit();
				sub.setSkip(skipArray);
				if (m.equals("-")) sub.setSkipLength(sub.getSkipLength() - 1);
				MixedRuleInfo rule = mix.get(position);
				ArrayList<SubRuleInfo> subArray = rule.getSubRules();
				subArray.set(current, sub);
				rule.setSubRules(subArray);
				mix.set(position, rule);
				notifyDataSetChanged();
				edit.putString("mixed", gson.toJson(mix));
				edit.apply();
			};
			holder.deleteRecheckButton.setOnClickListener(v -> {
				ArrayList<SkipInfo> skipArray = sub.getSkip();
				skipArray.remove(holder.getAdapterPosition());
				skipCommitter.accept(skipArray, "-");
			});
			BiConsumer<Integer, String> spinnerConditionController = (type, param) -> {
				if (type == 0) {
					holder.spinner.setSelection(0);
				} else {
					holder.spinner.setSelection(1);
				}
				if (param == null) holder.skipValue.setText("---");
				else {
					if (param.equals("")) holder.skipValue.setText("---");
					else {
						holder.skipValue.setVisibility(View.VISIBLE);
						holder.skipValue.setText(param);
					}
				}

				l(holder.getAdapterPosition() + Integer.toString(type) + param);
			};
			spinnerConditionController.accept(sub.getSkip().get(holder.getAdapterPosition()).getType(), sub.getSkip().get(holder.getAdapterPosition()).getParam());
			holder.spinner.postDelayed(() -> holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					try {
						spinnerConditionController.accept(pos, "---");
						ArrayList<SkipInfo> skipArray = sub.getSkip();
						SkipInfo skipInfo = skipArray.get(holder.getAdapterPosition());
						skipInfo.setType(pos);
						skipInfo.setParam("");
						skipArray.set(holder.getAdapterPosition(), skipInfo);
						skipCommitter.accept(skipArray, "*");
					} catch (Exception e) {
						if (holder.getAdapterPosition() != -1)
							Toast.makeText(jianfou.getAppContext(), "设置错误，请重试", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			}), 500);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void dataChange(ArrayList<SkipInfo> l) {
		sub.setSkip(l);
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		if (sub.getSkipLength() == 0) {
			return 1;
		} else {
			return sub.getSkipLength();
		}
	}

	static class MyViewHolder extends RecyclerView.ViewHolder {
		Button setButton, deleteButton, deleteRecheckButton;
		Spinner spinner;
		TextView skipValue;

		public MyViewHolder(View view) {
			super(view);
			setButton = view.findViewById(R.id.skip_set);
			deleteButton = view.findViewById(R.id.skip_delete);
			deleteRecheckButton = view.findViewById(R.id.skip_delete_check);
			spinner = view.findViewById(R.id.skip_spinner);
			skipValue = view.findViewById(R.id.skip_value);
		}
	}
}
