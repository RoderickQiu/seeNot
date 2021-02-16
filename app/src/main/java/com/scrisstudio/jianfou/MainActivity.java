package com.scrisstudio.jianfou;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "Jianfou-MainActivity";
	ActivityMainBinding binding;
	private List<RuleInfo> list = new ArrayList<>();

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		//SharedPreferences.Editor editor = sharedPreferences.edit();
		Gson gson = new Gson();
		list = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {}.getType());

		RecyclerView recyclerView = binding.ruleList;
		LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		RuleInfoAdapter adapter = new RuleInfoAdapter(getBaseContext(), list, (v, name) -> {
			int id = Integer.parseInt((String) v.findViewById(R.id.rule_id).getContentDescription());

			SharedPreferences.Editor edit = sharedPreferences.edit();
			RuleInfo rule = list.get(id);

			if (name == "rule_switch") {
				rule.setStatus(((SwitchMaterial) v.findViewById(R.id.rule_switch)).isChecked());
			}

			list.set(id, rule);
			edit.putString("rules", gson.toJson(list));
			edit.apply();
		});
		recyclerView.setAdapter(adapter);

		binding.topAppBar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.help:
					Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
					return true;
				default:
					return false;
			}
		});

		binding.topAppBar.setNavigationOnClickListener(v -> binding.drawerLayout.openDrawer(binding.navigation));

		binding.navigation.setNavigationItemSelectedListener(menuItem -> {
			binding.drawerLayout.closeDrawer(binding.navigation);

			switch (menuItem.getItemId()) {
				case R.id.item_settings:
					Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
					startActivity(intent);
					return false;
				default:
					Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
					return false;
			}
		});

		binding.floatingActionButton.setOnClickListener(v -> {
			Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
		});

	}
}
