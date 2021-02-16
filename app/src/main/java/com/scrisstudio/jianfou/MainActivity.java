package com.scrisstudio.jianfou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scrisstudio.jianfou.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "Jianfou-MainActivity";
	ActivityMainBinding binding;
	private Context context;
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
		context = this;

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		list.add(new RuleInfo(0, "小猿搜题遮罩1", "小猿搜题", "界面级别遮罩"));
		list.add(new RuleInfo(1, "小猿搜题遮罩2", "小猿搜题", "界面级别遮罩"));
		list.add(new RuleInfo(2, "小猿搜题遮罩3", "小猿搜题", "界面级别遮罩"));

		RecyclerView recyclerView = binding.ruleList;
		LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		RuleInfoAdapter adapter = new RuleInfoAdapter(getBaseContext(), list);
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
