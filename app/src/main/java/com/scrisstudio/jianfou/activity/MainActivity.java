package com.scrisstudio.jianfou.activity;

import static com.scrisstudio.jianfou.activity.PermissionGrantActivity.isAccessibilitySettingsOn;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.scrisstudio.jianfou.mask.MixedAssignerUtil;
import com.scrisstudio.jianfou.mask.MixedRuleInfo;
import com.scrisstudio.jianfou.ui.MixedInfoAdapter;
import com.scrisstudio.jianfou.ui.RuleInfoCardDecoration;
import com.scrisstudio.jianfou.ui.SimpleDialogFragment;
import com.sergivonavi.materialbanner.Banner;
import com.sergivonavi.materialbanner.BannerInterface;

import java.lang.ref.SoftReference;
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
	public static FragmentManager fragmentManager;
	public static LayoutInflater inflater;
	public static SoftReference<View> viewCustomization = null,
			viewTarget = null, viewLastTimeChoice = null, viewToast = null;
	public Banner banner;
	private ActivityMainBinding binding;
	private List<MixedRuleInfo> mixed = new ArrayList<>();
	private RecyclerView recyclerView;
	private MixedInfoAdapter adapter;

	public static int dip2px(float dipValue) {
		float m = jianfou.getAppContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	public static void openSimpleDialog(String type, String info) {
		SimpleDialogFragment.display(fragmentManager, type, info);
		SimpleDialogFragment.setOnSubmitListener(() -> {
		});
	}

	public static void runOnUI(Runnable runnable) {
		UIHandler.post(runnable);
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		xcrash.XCrash.init(this);
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
		} else if (requestCode == 201) {
			//reload page when back from settings
			reloader();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(TAG, "Start testing...");

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		resources = this.getResources();
		theme = this.getTheme();
		fragmentManager = getSupportFragmentManager();
		inflater = LayoutInflater.from(MainActivity.this);
		viewCustomization = new SoftReference<>(inflater.inflate(R.layout.layout_mixed_mask_assigner, null));//workaround for static view
		viewTarget = new SoftReference<>(inflater.inflate(R.layout.layout_accessibility_node_desc, null));
		viewLastTimeChoice = new SoftReference<>(inflater.inflate(R.layout.layout_last_choice_frame, null));
		viewToast = new SoftReference<>(inflater.inflate(R.layout.layout_toast_view, null));

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		windowTrueWidth = dm.widthPixels;
		windowTrueHeight = dm.heightPixels;
		binding.ruleList.setMinimumHeight(windowTrueHeight);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(jianfou.getAppContext());
		if (!sharedPreferences.contains("mixed")) {
			SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
			mixed.add(new MixedRuleInfo(true, 0, "未设置", "1.0.0", "未设置", "any", "com.example.software", new ArrayList<>(), 0));
			ruleInitEditor.putString("mixed", gson.toJson(mixed));
			ruleInitEditor.apply();
		}
		mixed = gson.fromJson(sharedPreferences.getString("mixed", "{}"), new TypeToken<List<MixedRuleInfo>>() {
		}.getType());

		banner = binding.banner;
		banner.setLeftButtonListener(banner1 -> banner.dismiss());
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
		}

		ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), false, sharedPreferences.getBoolean("split", true));

		Intent serviceIntent = new Intent(this, ActivitySeekerService.class);
		startService(serviceIntent);

		if (jianfou.isDebugApp() && Settings.System.canWrite(MainActivity.this)) {
			Settings.Secure.putString(getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.scrisstudio.jianfou/.mask.ActivitySeekerService");
			Settings.Secure.putString(getContentResolver(),
					Settings.Secure.ACCESSIBILITY_ENABLED, "1");
			banner.dismiss();
		} else {
			permissionBannerOpener();
		}

		recyclerView = binding.ruleList;
		LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		adapter = new MixedInfoAdapter(getBaseContext(), mixed, sharedPreferences);
		recyclerView.setAdapter(adapter);
		recyclerView.addItemDecoration(new RuleInfoCardDecoration());

		/*binding.topAppBar.setOnMenuItemClickListener(menuItem -> {
			if (menuItem.getItemId() == R.id.help) {
				Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
				return true;
			}
			return false;
		});*/
		binding.topAppBar.setOnMenuItemClickListener(menuItem -> {
			Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
			return false;
		});

		binding.topAppBar.setNavigationOnClickListener(v -> binding.drawerLayout.openDrawer(binding.navigation));

		binding.navigation.setNavigationItemSelectedListener(menuItem -> {
			binding.drawerLayout.closeDrawer(binding.navigation);

			if (menuItem.getItemId() == R.id.item_settings) {
				Intent settingsOpener = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(settingsOpener, 201);
				return false;
			} else if (menuItem.getItemId() == R.id.item_about) {
				Intent aboutOpener = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(aboutOpener);
				return false;
			} else if (menuItem.getItemId() == R.id.item_permissions) {
				Intent grantOpener = new Intent(MainActivity.this, PermissionGrantActivity.class);
				startActivity(grantOpener);
				return false;
			} else {
				return true;
			}
		});

		binding.floatingActionButton.setOnClickListener(v -> {
			final CharSequence[] choices = {resources.getString(R.string.add_rule_way_type_manual), resources.getString(R.string.add_rule_way_paste), resources.getString(R.string.add_rule_way_community)};

			AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).setTitle(R.string.add_rule).setItems(choices, (dialog, which) -> {
				if (which == 0) {// manual add rule
					SharedPreferences.Editor edit = sharedPreferences.edit();
					mixed.add(new MixedRuleInfo(true, sharedPreferences.getInt("rule-id-max", 0), "未设置", "1.0.0", "未设置", "any", "com.example.software", new ArrayList<>(), 0));
					edit.putString("mixed", gson.toJson(mixed));
					edit.putInt("rule-id-max", sharedPreferences.getInt("rule-id-max", 0) + 1);
					edit.apply();
					adapter.dataChange(mixed);
					ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-swtich", true), sharedPreferences.getBoolean("split", true));
				} else
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

		MixedAssignerUtil.setOnQuitListener((position, list) -> {
			mixed = list;
			ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-swtich", true), sharedPreferences.getBoolean("split", true));//TODO rules->mixed
			adapter.dataChange(list);
		});
	}

	private void permissionBannerOpener() {
		//如果不是所有权限都已打开
		if (!(isAccessibilitySettingsOn(jianfou.getAppContext()) && Settings.canDrawOverlays(jianfou.getAppContext()) &&
				((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName()) &&
				getSystemService(NotificationManager.class).areNotificationsEnabled())) {
			Banner bannerForPermissions = binding.banner;
			bannerForPermissions.setLeftButtonListener(BannerInterface::dismiss);
			bannerForPermissions.setRightButton("设置", banner2 -> {
				Intent grantOpener = new Intent(MainActivity.this, PermissionGrantActivity.class);
				startActivity(grantOpener);
				banner2.dismiss();
			});
			bannerForPermissions.setMessage(R.string.service_not_running);
			if (!jianfou.isDebugApp()) bannerForPermissions.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		reloader();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void reloader() {
		permissionBannerOpener();

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(jianfou.getAppContext());
		mixed = gson.fromJson(sharedPreferences.getString("mixed", "{}"), new TypeToken<List<MixedRuleInfo>>() {
		}.getType());
		adapter = new MixedInfoAdapter(getBaseContext(), mixed, sharedPreferences);
		recyclerView.setAdapter(adapter);

		if (!sharedPreferences.getBoolean("master-switch", true)) {
			banner.setMessage(R.string.function_closed);
			banner.show();
		} else banner.dismiss();
		ActivitySeekerService.setServiceBasicInfo(sharedPreferences.getString("rules", "{}"), sharedPreferences.getBoolean("master-switch", true), sharedPreferences.getBoolean("split", true));

		viewCustomization = new SoftReference<>(MainActivity.inflater.inflate(R.layout.layout_mixed_mask_assigner, null));
	}
}
