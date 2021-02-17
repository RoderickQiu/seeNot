package com.scrisstudio.jianfou;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.databinding.ActivityMainBinding;
import com.sergivonavi.materialbanner.Banner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "Jianfou-MainActivity";
	@SuppressLint("StaticFieldLeak")
	ActivityMainBinding binding;
	private List<RuleInfo> list = new ArrayList<>();

	public static int dip2px(float dipValue) {
		float m = jianfou.getAppContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

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
		/*SharedPreferences.Editor edit = sharedPreferences.edit();
		edit.putString("rules", "[{\"id\":0,\"ruleFor\":\"for\",\"ruleTitle\":\"0\",\"ruleType\":\"type\",\"status\":true},{\"id\":1,\"ruleFor\":\"for\",\"ruleTitle\":\"1\",\"ruleType\":\"type\",\"status\":true},{\"id\":2,\"ruleFor\":\"for\",\"ruleTitle\":\"2\",\"ruleType\":\"type\",\"status\":true},{\"id\":3,\"ruleFor\":\"for\",\"ruleTitle\":\"3\",\"ruleType\":\"type\",\"status\":true},{\"id\":4,\"ruleFor\":\"for\",\"ruleTitle\":\"4\",\"ruleType\":\"type\",\"status\":true},{\"id\":5,\"ruleFor\":\"for\",\"ruleTitle\":\"5\",\"ruleType\":\"type\",\"status\":true},{\"id\":6,\"ruleFor\":\"for\",\"ruleTitle\":\"6\",\"ruleType\":\"type\",\"status\":true},{\"id\":7,\"ruleFor\":\"for\",\"ruleTitle\":\"7\",\"ruleType\":\"type\",\"status\":true},{\"id\":8,\"ruleFor\":\"for\",\"ruleTitle\":\"8\",\"ruleType\":\"type\",\"status\":true}]");
		edit.apply();*/
		Gson gson = new Gson();
		list = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {}.getType());

		Banner banner = binding.banner;
		banner.setLeftButtonListener(banner1 -> {
			banner.dismiss();
		});
		banner.setRightButtonListener(banner2 -> {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("master-switch", true);
			editor.apply();

			Toast.makeText(this.getApplicationContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
			banner.dismiss();
		});
		if (!sharedPreferences.getBoolean("master-switch", true)) banner.show();

		RecyclerView recyclerView = binding.ruleList;
		LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		RuleInfoAdapter adapter = new RuleInfoAdapter(getBaseContext(), list, sharedPreferences);
		recyclerView.setAdapter(adapter);
		recyclerView.addItemDecoration(new CardDecoration());

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
