package com.scrisstudio.seenot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static Handler UIHandler = new Handler(Looper.getMainLooper());
    private static final Gson gson = new Gson();
    private ArrayList<RuleInfo> rulesList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static Resources resources;

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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());
        if (!sharedPreferences.contains("rules")) {
            SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
            rulesList.add(new RuleInfo(true, 0, "未设置", "com.software.any", "any", new ArrayList<>(), 0));
            ruleInitEditor.putString("rules", gson.toJson(rulesList));
            ruleInitEditor.apply();
        }
        rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());

        Intent serviceIntent = new Intent(this, ExecutorService.class);
        startService(serviceIntent);

        if (Settings.System.canWrite(MainActivity.this)) {
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.scrisstudio.seenot/.service.ExecutorService");
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, "1");
            //banner.dismiss();
        } else {
            //permissionBannerOpener();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resources = getResources();

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
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}