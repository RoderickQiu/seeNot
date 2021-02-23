package com.scrisstudio.jianfou;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.jianfou.databinding.ActivityMainBinding;
import com.sergivonavi.materialbanner.Banner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "Jianfou-MainActivity";
	private static final Gson gson = new Gson();
	public static Resources resources;
	public static Handler UIHandler = new Handler(Looper.getMainLooper());
	private static FragmentManager fragmentManager;
	private static SharedPreferences sharedPreferences;
	private ActivityMainBinding binding;
	private List<RuleInfo> list = new ArrayList<>();

	public static int dip2px(float dipValue) {
		float m = jianfou.getAppContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	public static void openCardEditDialog(int position) {
		FullscreenDialogFragment.display(fragmentManager, position, gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {}.getType()));
	}

	public static void openSimpleDialog(String type, String info) {
		SimpleDialogFragment.display(fragmentManager, type, info);
	}

	public static void runOnUI(Runnable runnable) {
		UIHandler.post(runnable);
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

		resources = this.getResources();
		fragmentManager = getSupportFragmentManager();

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		/*SharedPreferences.Editor edit = sharedPreferences.edit();
		list.add(new RuleInfo(true, 0, "0", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 1, "1", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 2, "2", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 3, "3", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 4, "4", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 5, "5", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 6, "6", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 7, "7", "1.0", "software", "any", "general type"));
		list.add(new RuleInfo(true, 8, "8", "1.0", "software", "any", "general type"));
		edit.putString("rules", gson.toJson(list));
		edit.apply();*/
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

		FullscreenDialogFragment.setOnSubmitListener((pos, l) -> {
			SharedPreferences.Editor submitEditer = sharedPreferences.edit();
			submitEditer.putString("rules", gson.toJson(l));
			submitEditer.apply();

			adapter.dataChange(l);
		});

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

			if (menuItem.getItemId() == R.id.item_settings) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				return false;
			} else {
				return true;
			}
		});

		binding.floatingActionButton.setOnClickListener(v -> {
			final CharSequence[] choices = {resources.getString(R.string.add_rule_way_type_manual), resources.getString(R.string.add_rule_way_paste), resources.getString(R.string.add_rule_way_community)};

			AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).setTitle(R.string.add_rule).setItems(choices, (dialog, which) -> {
				Toast.makeText(jianfou.getAppContext(), "还没有完成。", Toast.LENGTH_LONG).show();
			}).create();
			alertDialog.show();

			try {
				Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
				mAlert.setAccessible(true);
				Object mAlertController = mAlert.get(alertDialog);
				if (mAlertController != null) {
					Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
					mTitle.setAccessible(true);
					TextView mTitleView = (TextView) mTitle.get(mAlertController);
					if (mTitleView != null)
						mTitleView.setTextAppearance(R.style.TextAppearance_MaterialComponents_Headline6);
				}
			} catch (IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		});

	}
}
