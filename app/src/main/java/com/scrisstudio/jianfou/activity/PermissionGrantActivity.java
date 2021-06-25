package com.scrisstudio.jianfou.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.databinding.ActivityPermissionGrantBinding;
import com.scrisstudio.jianfou.jianfou;

import static com.scrisstudio.jianfou.mask.ActivitySeekerService.l;

public class PermissionGrantActivity extends AppCompatActivity {
	ActivityPermissionGrantBinding binding;

	public static boolean isAccessibilitySettingsOn(Context mContext) {
		int accessibilityEnabled = 0;
		final String service = "com.scrisstudio.jianfou/com.scrisstudio.jianfou.mask.ActivitySeekerService";
		try {
			accessibilityEnabled = Settings.Secure.getInt(
					mContext.getApplicationContext().getContentResolver(),
					android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);

			TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

			if (accessibilityEnabled == 1) {
				String settingValue = Settings.Secure.getString(
						mContext.getApplicationContext().getContentResolver(),
						Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
				if (settingValue != null) {
					mStringColonSplitter.setString(settingValue);
					while (mStringColonSplitter.hasNext()) {
						String accessibilityService = mStringColonSplitter.next();
						if (accessibilityService.equalsIgnoreCase(service)) {
							return true;
						}
					}
				}
			} else {
				return false;
			}
		} catch (Settings.SettingNotFoundException e) {
			l("Error finding setting, default accessibility not found: " + e.getMessage());
		}

		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 111) {
			//reload page when back from settings
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityPermissionGrantBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		binding.settingsAppBar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.help:
					Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
					return true;
				default:
					return false;
			}
		});

		binding.settingsAppBar.setNavigationOnClickListener(v -> {
			finish();
		});

		//通知权限
		if (!getSystemService(NotificationManager.class).areNotificationsEnabled()) {
			binding.permissionNotificationUngranted.setVisibility(View.VISIBLE);
		} else {
			binding.permissionNotificationGranted.setVisibility(View.VISIBLE);
		}
		binding.permissionNotification.setOnClickListener(v -> {
			Intent intent = new Intent();
			intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
			intent.putExtra("android.provider.extra.APP_PACKAGE", PermissionGrantActivity.this.getPackageName());
			startActivityForResult(intent, 111);
		});

		//悬浮窗权限
		if (!Settings.canDrawOverlays(this)) {
			binding.permissionOverlayUngranted.setVisibility(View.VISIBLE);
		} else {
			binding.permissionOverlayGranted.setVisibility(View.VISIBLE);
		}
		binding.permissionOverlay.setOnClickListener(v -> {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, 111);
		});

		//无障碍权限
		if (!isAccessibilitySettingsOn(jianfou.getAppContext())) {
			binding.permissionAccessibilityUngranted.setVisibility(View.VISIBLE);
		} else {
			binding.permissionAccessibilityGranted.setVisibility(View.VISIBLE);
		}
		binding.permissionAccessibility.setOnClickListener(v -> {
			try {
				startActivityForResult(new Intent("android.settings.ACCESSIBILITY_SETTINGS"), 111);
			} catch (Exception e) {
				startActivityForResult(new Intent("android.settings.SETTINGS"), 111);
				e.printStackTrace();
			}
		});

		//省电
		if (!((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
			binding.permissionBatteryUngranted.setVisibility(View.VISIBLE);
		} else {
			binding.permissionBatteryGranted.setVisibility(View.VISIBLE);
		}
		binding.permissionBattery.setOnClickListener(v -> {
			if (!((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())) {
				@SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, 111);
			} else
				Toast.makeText(jianfou.getAppContext(), "已经获取权限，无需设置", Toast.LENGTH_SHORT).show();
		});

		//自启和后台运行
		binding.permissionBackground.setOnClickListener(v -> {
			//打开浏览器链接
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse("https://keep-alive.pages.dev/#" + getManufacturer());
			intent.setData(content_url);
			startActivity(intent);
		});

	}

	private String getManufacturer() {
		String brand = android.os.Build.BRAND.toLowerCase();
		switch (brand) {
			case "xiaomi":
				return "miui";
			case "huawei":
			case "honor":
				return "emui";
			case "oppo":
			case "oneplus":
			case "realme":
				return "coloros";
			case "vivo":
			case "iqoo":
				return "funtouchos";
			default:
				return "other";
		}
	}
}