package com.scrisstudio.seenot.ui.home;

import static com.scrisstudio.seenot.MainActivity.sharedPreferences;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.databinding.FragmentHomeBinding;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;
import com.scrisstudio.seenot.ui.rule.RuleInfoAdapter;
import com.scrisstudio.seenot.ui.rule.RuleInfoCardDecoration;
import com.sergivonavi.materialbanner.Banner;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final Gson gson = new Gson();
    private ArrayList<RuleInfo> rules = new ArrayList<>();
    private ArrayList<TimedInfo> timed = new ArrayList<>();
    private Banner banner;
    private String bannerMessage;
    private Resources resources;

    private final BroadcastReceiver bannerMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getStringExtra("banner_message").equals("dismiss")) {
                banner.setMessage(intent.getStringExtra("banner_message"));
                binding.banner.show();
                bannerMessage = intent.getStringExtra("banner_message");

                banner.setRightButton(getRightButtonText(), banner2 -> {
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
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                            navController.navigate(R.id.nav_permission);
                        } catch (Exception ignored) {
                        }
                        binding.fab.hide();
                        banner.dismiss();
                    }
                });
            } else {
                binding.banner.dismiss();
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());
        timed = gson.fromJson(sharedPreferences.getString("timed", "{}"), new TypeToken<ArrayList<TimedInfo>>() {
        }.getType());

        RecyclerView recyclerView = binding.ruleList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        RuleInfoAdapter adapter = new RuleInfoAdapter(getActivity(), getFragmentManager(), rules, timed, sharedPreferences);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RuleInfoCardDecoration());

        binding.fab.show();
        binding.fab.setOnClickListener(v -> {
            CharSequence[] addList = {"新建规则", "导入规则"};
            new MaterialAlertDialogBuilder(requireContext()).setTitle("选择模式")
                    .setItems(addList, (dialogInterface, i) -> {
                        if (i == 0) {
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            rules.add(new RuleInfo(sharedPreferences.getInt("rule-id-max", 0), true, "新建规则", "com.software.any", "未设置", new ArrayList<>(), 0, 0));
                            edit.putString("rules", gson.toJson(rules));
                            edit.putInt("rule-id-max", sharedPreferences.getInt("rule-id-max", 0) + 1);
                            edit.apply();
                            ExecutorService.setServiceBasicInfo(sharedPreferences, 0);
                            AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
                            MainActivity.setSharedPreferences(sharedPreferences);
                            adapter.dataChange(rules);
                        } else {
                            ImportDialogFragment.display(getFragmentManager(), sharedPreferences);
                            ImportDialogFragment.setOnSubmitListener((rules) -> {
                                SharedPreferences.Editor edit = sharedPreferences.edit();
                                this.rules = rules;
                                edit.putString("rules", gson.toJson(rules));
                                edit.putInt("rule-id-max", sharedPreferences.getInt("rule-id-max", 0) + 1);
                                edit.apply();
                                ExecutorService.setServiceBasicInfo(sharedPreferences, 0);
                                AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
                                MainActivity.setSharedPreferences(sharedPreferences);
                                adapter.dataChange(rules);
                            });
                        }
                    })
                    .create().show();
        });

        resources = MainActivity.resources;

        banner = binding.banner;
        banner.setLeftButtonListener(banner1 -> banner.dismiss());
        banner.setRightButtonListener(banner1 -> banner.dismiss());

        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(bannerMessageReceiver,
                new IntentFilter("banner_channel"));

        AssignerUtils.setOnQuitListener((position, list, mode) -> {
            rules = (ArrayList<RuleInfo>) list;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("rules", gson.toJson(rules));
            editor.apply();
            ExecutorService.setServiceBasicInfo(sharedPreferences, mode);
            adapter.dataChange(list);
        });
        RuleInfoAdapter.setOnEditListener((position, list, mode) -> {
            rules = (ArrayList<RuleInfo>) list;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("rules", gson.toJson(rules));
            editor.apply();
            ExecutorService.setServiceBasicInfo(sharedPreferences, mode);
            adapter.dataChange(list);
        });

        return root;
    }

    private int getRightButtonText() {
        if (bannerMessage.equals(resources.getString(R.string.function_closed)))
            return R.string.reopen_function;
        else
            return R.string.permission_grant;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(bannerMessageReceiver);
        } catch (Exception ignored) {
        }
        binding = null;
    }
}