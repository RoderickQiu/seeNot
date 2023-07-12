package com.scrisstudio.seenot.ui.notification;

import static com.scrisstudio.seenot.SeeNot.le;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.struct.PushedInfo;
import com.scrisstudio.seenot.struct.RuleInfo;

import java.util.List;

public class PushedInfoAdapter extends RecyclerView.Adapter<PushedInfoAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_CARD = 1;
    private static List<PushedInfo> mList = null;
    private final SharedPreferences sharedPreferences;
    private static FragmentManager fragmentManager;
    private final Context context;
    private final Gson gson;

    public PushedInfoAdapter(Context context, FragmentManager fragmentManager, List<PushedInfo> mList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.sharedPreferences = sharedPreferences;
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
            view = LayoutInflater.from(context).inflate(R.layout.layout_pushed_info, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.layout_empty_recyclerview, parent, false);
            ((TextView) view.findViewById(R.id.empty_recycler_msg)).setText(R.string.no_notification_or_bad_net);
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
            holder.title.setText(mList.get(position).getTitle());
            holder.msg.setText(mList.get(position).getMsg());
            holder.date.setText(getStyledDate(mList.get(position).getId()));
        }
    }

    private String getStyledDate(int id) {
        String str = String.valueOf(id);
        return "20" + str.substring(0, 2) + "." + str.substring(2, 4) + "." + str.substring(4, 6);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void dataChange(List<PushedInfo> l) {
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
        TextView title, msg, date;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.pushed_title);
            msg = view.findViewById(R.id.pushed_msg);
            date = view.findViewById(R.id.pushed_date);
        }
    }
}

