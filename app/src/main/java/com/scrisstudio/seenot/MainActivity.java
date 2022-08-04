package com.scrisstudio.seenot;

import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.lastTimeDestination;
import static com.scrisstudio.seenot.ui.permission.PermissionFragment.isAccessibilitySettingsOn;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.databinding.ActivityMainBinding;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.service.RuleInfo;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static Handler UIHandler = new Handler(Looper.getMainLooper());
    private static final Gson gson = new Gson();
    private ArrayList<RuleInfo> rulesList = new ArrayList<>();
    public static Resources resources;
    public static SharedPreferences sharedPreferences;
    public static Boolean isNotificationEnabled = false, isOverlayEnabled = false;
    public static String packageName = "";
    public static SoftReference<View> viewCustomization = null, viewTarget = null;

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public static int dip2px(float dipValue) {
        float m = resources.getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        l("Now entering main activity...");

        packageName = getPackageName();

        resources = getResources();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());
        if (!sharedPreferences.contains("rules")) {
            SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
            ruleInitEditor.putString("rules", gson.toJson(rulesList));
            ruleInitEditor.apply();
        }
        rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        Intent serviceIntent = new Intent(this, ExecutorService.class);
        startService(serviceIntent);

        isNotificationEnabled = getSystemService(NotificationManager.class).areNotificationsEnabled();
        isOverlayEnabled = Settings.canDrawOverlays(this);

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        viewCustomization = new SoftReference<>(inflater.inflate(R.layout.layout_assigner, null));//workaround for static view
        viewTarget = new SoftReference<>(inflater.inflate(R.layout.layout_view_target, null));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.scrisstudio.seenot/.service.ExecutorService");
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, "1");
        } catch (Exception ignored) {
        }

        // master switch check
        if (!sharedPreferences.getBoolean("master-switch", true)) {
            Intent intent = new Intent("banner_channel");
            intent.putExtra("banner_message", resources.getString(R.string.function_closed));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        // permission check
        if (!(isAccessibilitySettingsOn(SeeNot.getAppContext()) && Settings.canDrawOverlays(SeeNot.getAppContext()) &&
                ((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName()) &&
                getSystemService(NotificationManager.class).areNotificationsEnabled())) {
            Intent intent = new Intent("banner_channel");
            intent.putExtra("banner_message", resources.getString(R.string.service_not_running));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings, R.id.nav_permission, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.toString().contains("Home")) {
                binding.appBarMain.toolbar.setTitle(R.string.app_full_name);
            }
            if (SeeNot.shouldNavigateTo == 0 && ((lastTimeDestination.contains("Permission") && (destination.toString().contains("Home") || destination.toString().contains("Permission"))) || (lastTimeDestination.contains("Settings") && (destination.toString().contains("Home") || destination.toString().contains("Settings"))))) {
                finish();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
            lastTimeDestination = destination.toString();
        });

        if (SeeNot.shouldNavigateTo != 0) {
            navController.navigate(SeeNot.shouldNavigateTo);
            SeeNot.shouldNavigateTo = 0;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}