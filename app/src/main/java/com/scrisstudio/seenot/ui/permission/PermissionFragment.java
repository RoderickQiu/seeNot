package com.scrisstudio.seenot.ui.permission;

import static com.scrisstudio.seenot.MainActivity.packageName;
import static com.scrisstudio.seenot.SeeNot.l;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.databinding.FragmentPermissionBinding;

import java.util.Objects;

import dev.doubledot.doki.ui.DokiActivity;

public class PermissionFragment extends Fragment {

    private FragmentPermissionBinding binding;

    private final BroadcastReceiver permissionMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            init();
        }
    };

    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
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
                        if (accessibilityService.contains(packageName)) {
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

    private void init() {
        //通知权限
        if (!MainActivity.isNotificationEnabled) {
            binding.permissionNotificationUngranted.setVisibility(View.VISIBLE);
        } else {
            binding.permissionNotificationGranted.setVisibility(View.VISIBLE);
        }
        binding.permissionNotification.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
            startActivityForResult(intent, 111);
        });

        //悬浮窗权限
        if (!MainActivity.isOverlayEnabled) {
            binding.permissionOverlayUngranted.setVisibility(View.VISIBLE);
        } else {
            binding.permissionOverlayGranted.setVisibility(View.VISIBLE);
        }
        binding.permissionOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName));
            startActivityForResult(intent, 111);
        });

        //省电
        if (!MainActivity.isBatteryOptimIgnored) {
            binding.permissionBatteryUngranted.setVisibility(View.VISIBLE);
        } else {
            binding.permissionBatteryGranted.setVisibility(View.VISIBLE);
        }
        binding.permissionBattery.setOnClickListener(v -> {
            if (!MainActivity.isBatteryOptimIgnored) {
                @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, 111);
            } else
                Toast.makeText(SeeNot.getAppContext(), "已经获取权限，无需设置", Toast.LENGTH_SHORT).show();
        });

        //无障碍权限
        if (!isAccessibilitySettingsOn(SeeNot.getAppContext())) {
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

        //自启和后台运行
        binding.permissionBackground.setOnClickListener(v -> {
            if (SeeNot.getLocale().equals("zh")) {
                Intent intent = new Intent();//打开浏览器链接
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://keep-alive.pages.dev/#" + SeeNot.getManufacturer());
                intent.setData(content_url);
                startActivity(intent);
            } else {
                DokiActivity.Companion.start(requireContext());
            }
        });

        binding.permissionBack.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.putExtra("password", "success");
            startActivity(intent);
        });

        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(permissionMessageReceiver,
                new IntentFilter("permission_channel"));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPermissionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        init();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            SeeNot.shouldNavigateTo = R.id.nav_permission;

            Intent intentRestart = new Intent(getContext(), MainActivity.class);
            startActivity(intentRestart);
        }
    }
}