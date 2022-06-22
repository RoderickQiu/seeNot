package com.scrisstudio.seenot.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.databinding.FragmentHomeBinding;
import com.scrisstudio.seenot.service.RuleInfo;
import com.scrisstudio.seenot.ui.rule.RuleInfoAdapter;
import com.scrisstudio.seenot.ui.rule.RuleInfoCardDecoration;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final Gson gson = new Gson();
    private ArrayList<RuleInfo> rules = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        RecyclerView recyclerView = binding.ruleList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        RuleInfoAdapter adapter = new RuleInfoAdapter(getActivity(), rules, sharedPreferences);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RuleInfoCardDecoration());

        binding.fab.setOnClickListener(v -> {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            rules.add(new RuleInfo(true, sharedPreferences.getInt("rule-id-max", 0), "未设置", "com.software.any", "any", new ArrayList<>(), 0));
            edit.putString("rules", gson.toJson(rules));
            edit.putInt("rule-id-max", sharedPreferences.getInt("rule-id-max", 0) + 1);
            edit.apply();
            adapter.dataChange(rules);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}