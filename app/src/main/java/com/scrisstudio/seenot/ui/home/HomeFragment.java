package com.scrisstudio.seenot.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.databinding.FragmentHomeBinding;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.service.RuleInfo;
import com.scrisstudio.seenot.ui.rule.RuleInfoAdapter;
import com.scrisstudio.seenot.ui.rule.RuleInfoCardDecoration;
import com.sergivonavi.materialbanner.Banner;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final Gson gson = new Gson();
    private ArrayList<RuleInfo> rules = new ArrayList<>();
    private Banner banner;
    private String bannerMessage;

    private final BroadcastReceiver bannerMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getStringExtra("banner_message").equals("dismiss")) {
                banner.setMessage(intent.getStringExtra("banner_message"));
                binding.banner.show();
                bannerMessage = intent.getStringExtra("banner_message");
            } else {
                binding.banner.dismiss();
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        RecyclerView recyclerView = binding.ruleList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        RuleInfoAdapter adapter = new RuleInfoAdapter(getActivity(), rules, sharedPreferences);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RuleInfoCardDecoration());

        binding.fab.show();
        binding.fab.setOnClickListener(v -> {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            rules.add(new RuleInfo(true, sharedPreferences.getInt("rule-id-max", 0), "未设置", "com.software.any", "any", new ArrayList<>(), 0));
            edit.putString("rules", gson.toJson(rules));
            edit.putInt("rule-id-max", sharedPreferences.getInt("rule-id-max", 0) + 1);
            edit.apply();
            adapter.dataChange(rules);
        });

        Resources resources = getResources();

        banner = binding.banner;
        banner.setLeftButtonListener(banner1 -> banner.dismiss());
        banner.setRightButtonListener(banner2 -> {
            if (bannerMessage.equals(resources.getString(R.string.function_closed))) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("master-switch", true);
                ExecutorService.isServiceRunning = true;
                editor.apply();

                if (ExecutorService.isStart()) {
                    Toast.makeText(SeeNot.getAppContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
                }
                banner.dismiss();
            } else if (bannerMessage.equals(resources.getString(R.string.service_not_running))) {
                try {
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_permission);
                } catch (Exception ignored) {
                }
                binding.fab.hide();
                banner.dismiss();
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(bannerMessageReceiver,
                new IntentFilter("banner_channel"));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(bannerMessageReceiver);
        } catch (Exception ignored) {
        }
        binding = null;
    }
}