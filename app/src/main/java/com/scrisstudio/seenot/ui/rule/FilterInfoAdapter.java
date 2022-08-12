package com.scrisstudio.seenot.ui.rule;

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
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.FilterInfo;
import com.scrisstudio.seenot.service.RuleInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FilterInfoAdapter extends RecyclerView.Adapter<FilterInfoAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_CARD = 1;
    private final SharedPreferences sharedPreferences;
    private final LayoutInflater inflater;
    private final Gson gson;
    private ArrayList<RuleInfo> rules;
    private final RuleInfo current;
    private final int position;
    private static SaveListener callBack;

    public static void setSaveListener(SaveListener callback) {
        FilterInfoAdapter.callBack = callback;
    }

    public interface SaveListener {
        void save(RuleInfo current);
    }

    public FilterInfoAdapter(LayoutInflater inflater, ArrayList<RuleInfo> rules, int position, SharedPreferences sharedPreferences) {
        this.inflater = inflater;
        this.sharedPreferences = sharedPreferences;
        this.rules = rules;
        this.position = position;
        this.gson = new Gson();
        current = rules.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (current.getFilterLength() == 0) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_CARD;
        }
    }

    @NonNull
    @Override
    public FilterInfoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_CARD) {
            view = inflater.inflate(R.layout.layout_filter_card, parent, false);
        } else {
            view = inflater.inflate(R.layout.layout_empty_recyclerview, parent, false);
        }
        return new FilterInfoAdapter.MyViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull FilterInfoAdapter.MyViewHolder holder, int filterId) {
        int viewType = getItemViewType(filterId);
        if (viewType == VIEW_TYPE_CARD) {
            if (current.getFilter().get(filterId).getType() != 0)
                holder.filterValue.setText(current.getFilter().get(filterId).getParam1().replace(current.getFor(), ""));
            else holder.filterValue.setVisibility(View.GONE);
            holder.filterType.setText(AssignerUtils.resources.getString(SeeNot.getFilterTypeName(current.getFilter().get(filterId).getType())));
            holder.setButton.setOnClickListener(v -> AssignerUtils.initAssigner(2, position, filterId));
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
                ArrayList<FilterInfo> filters = current.getFilter();
                filters.remove(holder.getAdapterPosition());
                current.setFilterLength(current.getFilterLength() - 1);
                callBack.save(current);
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void dataChange(ArrayList<FilterInfo> l) {
        current.setFilter(l);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (current.getFilterLength() == 0) {
            return 1;
        } else {
            return current.getFilterLength();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        Button setButton, deleteButton, deleteRecheckButton;
        TextView filterValue, filterType;

        public MyViewHolder(View view) {
            super(view);
            setButton = view.findViewById(R.id.skip_set);
            deleteButton = view.findViewById(R.id.skip_delete);
            deleteRecheckButton = view.findViewById(R.id.skip_delete_check);
            filterValue = view.findViewById(R.id.filter_value);
            filterType = view.findViewById(R.id.filter_type);
        }
    }
}
