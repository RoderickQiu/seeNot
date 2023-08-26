package com.scrisstudio.seenot;

import static com.scrisstudio.seenot.MainActivity.sharedPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.databinding.ActivityTimedSetBinding;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.assigner.AssignerUtils;
import com.scrisstudio.seenot.ui.rule.RuleInfoCardDecoration;
import com.scrisstudio.seenot.ui.timed.EditDialogFragment;
import com.scrisstudio.seenot.ui.timed.RuleTimedAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RuleTimedActivity extends AppCompatActivity {

    private ActivityTimedSetBinding binding;
    private static final Gson gson = new Gson();
    private static ArrayList<TimedInfo> timed = null;
    private static ArrayList<RuleInfo> rules = null;
    private static FragmentManager fragmentManager;

    public static void openEditDialog(int position, SharedPreferences sharedPreferences) {
        EditDialogFragment.display(fragmentManager, position, sharedPreferences);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimedSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentManager = getSupportFragmentManager();

        binding.topAppBar.setNavigationOnClickListener(v -> {
            finish();
        });

        RecyclerView recyclerView = binding.timedSetList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        timed = gson.fromJson(sharedPreferences.getString("timed", "{}"), new TypeToken<ArrayList<TimedInfo>>() {
        }.getType());
        rules = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        RuleTimedAdapter adapter = new RuleTimedAdapter(this, timed, rules, sharedPreferences, getResources());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RuleInfoCardDecoration());

        RuleTimedAdapter.setOnEditListener((position, list, mode) -> {
            ArrayList<TimedInfo> rules = (ArrayList<TimedInfo>) list;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("timed", gson.toJson(rules));
            editor.apply();
            ExecutorService.setServiceBasicInfo(sharedPreferences, mode);
            adapter.dataChange(list);
        });

        binding.fab.show();
        binding.fab.setOnClickListener(v -> {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            timed.add(new TimedInfo(sharedPreferences.getInt("timed-id-max", 0), "未命名", true, true, -1, 0, new Date().getTime(), new Date().getTime(), new Date().getTime()));
            edit.putString("timed", gson.toJson(timed));
            edit.putInt("timed-id-max", sharedPreferences.getInt("timed-id-max", 0) + 1);
            edit.apply();
            ExecutorService.setServiceBasicInfo(sharedPreferences, 0);
            AssignerUtils.setAssignerSharedPreferences(sharedPreferences);
            MainActivity.setSharedPreferences(sharedPreferences);
            adapter.dataChange(timed);
        });
    }
}