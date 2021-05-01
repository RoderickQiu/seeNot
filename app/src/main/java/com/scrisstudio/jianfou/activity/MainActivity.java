package com.scrisstudio.jianfou.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.databinding.ActivityMainBinding;
import com.scrisstudio.jianfou.jianfou;
import com.scrisstudio.jianfou.mask.ActivitySeekerService;
import com.scrisstudio.jianfou.ui.CardDecoration;
import com.scrisstudio.jianfou.ui.FullscreenDialogFragment;
import com.scrisstudio.jianfou.ui.RuleInfo;
import com.scrisstudio.jianfou.ui.RuleInfoAdapter;
import com.scrisstudio.jianfou.ui.SimpleDialogFragment;
import com.sergivonavi.materialbanner.Banner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "Jianfou-MainActivity";
	private static final Gson gson = new Gson();
	public static Resources resources;
	public static Handler UIHandler = new Handler(Looper.getMainLooper());
	public static Resources.Theme theme;
	public static int windowTrueWidth, windowTrueHeight;
	public static SharedPreferences sharedPreferences;
	public static String currentHomePackage;
	private static FragmentManager fragmentManager;
	private ActivityMainBinding binding;
	private List<RuleInfo> list = new ArrayList<>();

	public static int dip2px(float dipValue) {
		float m = jianfou.getAppContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	public static void openCardEditDialog(int position) {
		FullscreenDialogFragment.display(fragmentManager, position, gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
		}.getType()));
	}

	public static void openSimpleDialog(String type, String info) {
		SimpleDialogFragment.display(fragmentManager, type, info);
		SimpleDialogFragment.setOnSubmitListener(() -> {
		});
	}

	public static void runOnUI(Runnable runnable) {
		UIHandler.post(runnable);
	}

	private void settingModifierTrigger() {
		SimpleDialogFragment.display(fragmentManager, "service-trigger", resources.getString(R.string.settings_modifier_enable_guide));
		SimpleDialogFragment.setOnSubmitListener(() -> {
			Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
			intent.setData(Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, 200);
		});

		//also for alert window permission
		//startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 200) {
			if (Settings.System.canWrite(getApplicationContext())) {
				Intent serviceIntent = new Intent(this, ActivitySeekerService.class);
				startService(serviceIntent);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "Start testing...");

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		resources = this.getResources();
		theme = this.getTheme();
		fragmentManager = getSupportFragmentManager();

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		windowTrueWidth = dm.widthPixels;
		windowTrueHeight = dm.heightPixels;
		binding.ruleList.setMinimumHeight(windowTrueHeight);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(jianfou.getAppContext());
		if (!sharedPreferences.contains("rules")) {
			SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
			ruleInitEditor.putString("rules", gson.toJson(list));
			ruleInitEditor.apply();
		}
        /*SharedPreferences.Editor edit = sharedPreferences.edit();
        list.add(new RuleInfo(true, 0, "0", "1.0", "software", "any", "general type", new PackageWidgetDescription()));
        list.add(new RuleInfo(true, 1, "1", "1.0", "software", "any", "general type", new PackageWidgetDescription()));
        list.add(new RuleInfo(true, 2, "2", "1.0", "software", "any", "general type", new PackageWidgetDescription()));
        edit.putString("rules", gson.toJson(list));
        edit.apply();*/
		list = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
		}.getType());

		Banner banner = binding.banner;
		banner.setLeftButtonListener(banner1 -> {
			banner.dismiss();
		});
		banner.setRightButtonListener(banner2 -> {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("master-switch", true);
			ActivitySeekerService.isServiceRunning = true;
			editor.apply();

			if (ActivitySeekerService.isStart()) {
				Toast.makeText(this.getApplicationContext(), R.string.operation_done, Toast.LENGTH_SHORT).show();
			}
			banner.dismiss();
		});
		if (!sharedPreferences.getBoolean("master-switch", true)) {
			banner.setMessage(R.string.function_closed);
			banner.show();

			ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), false);
		}

		//TODO this should test more
		//see also https://blog.csdn.net/weixin_42474371/article/details/104405463
		//see also https://www.jianshu.com/p/0acb66694860
		if (!Settings.System.canWrite(getApplicationContext())) {
			settingModifierTrigger();
		} else {
			Intent serviceIntent = new Intent(this, ActivitySeekerService.class);
			startService(serviceIntent);
		}

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
			if (menuItem.getItemId() == R.id.help) {
				Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
				return true;
			}
			return false;
		});

		binding.topAppBar.setNavigationOnClickListener(v -> binding.drawerLayout.openDrawer(binding.navigation));

		binding.navigation.setNavigationItemSelectedListener(menuItem -> {
			binding.drawerLayout.closeDrawer(binding.navigation);

			if (menuItem.getItemId() == R.id.item_settings) {
				Intent settingsOpener = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(settingsOpener);
				return false;
			} else if (menuItem.getItemId() == R.id.item_about) {
				Intent aboutOpener = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(aboutOpener);
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
