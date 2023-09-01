package com.scrisstudio.seenot;

import static com.scrisstudio.seenot.SeeNot.getLocale;
import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.lastTimeDestination;
import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.ui.permission.PermissionFragment.isAccessibilitySettingsOn;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.databinding.ActivityMainBinding;
import com.scrisstudio.seenot.service.APKVersionInfoUtils;
import com.scrisstudio.seenot.service.ExecutorService;
import com.scrisstudio.seenot.struct.FetcherInfo;
import com.scrisstudio.seenot.struct.PushedInfo;
import com.scrisstudio.seenot.struct.RuleInfo;
import com.scrisstudio.seenot.struct.TimedInfo;
import com.scrisstudio.seenot.ui.notification.NotifyDialogFragment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.github.g00fy2.versioncompare.Version;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static Handler UIHandler = new Handler(Looper.getMainLooper());
    private static final Gson gson = new Gson();
    private static ArrayList<RuleInfo> rulesList = new ArrayList<>();
    private static ArrayList<TimedInfo> timedList = new ArrayList<>();
    ArrayList<Integer> pushedReadList = new ArrayList<>();
    private static String password, extra;
    public static Resources resources;
    public static SharedPreferences sharedPreferences;
    public static Boolean isNotificationEnabled = false, isOverlayEnabled = false, isBatteryOptimIgnored = false;
    public static String packageName = "";
    public static SoftReference<View> viewCustomization = null, viewTarget = null;
    public ActionBarDrawerToggle mDrawerToggle;
    private static String currentDestination = "Home";

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public static int dip2px(float dipValue) {
        float m = resources.getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        MainActivity.sharedPreferences = sharedPreferences;
        rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<List<RuleInfo>>() {
        }.getType());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        l("Now entering main activity...");

        packageName = getPackageName();

        resources = getResources();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());
        if (!sharedPreferences.contains("reservation-lock")) {
            SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
            ruleInitEditor.putString("reservation-lock", "0");
            ruleInitEditor.apply();
        }
        if (!sharedPreferences.contains("rl-time") ||
                new Date().getTime() - sharedPreferences.getLong("rl-time", new Date().getTime() - 1000000L) > 10L * 60000L) {
            SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
            ruleInitEditor.putLong("rl-time", new Date().getTime() + 1000000L);
            ruleInitEditor.apply();
        }
        if (!sharedPreferences.contains("rules")) {
            SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
            ruleInitEditor.putString("rules", gson.toJson(rulesList));
            ruleInitEditor.apply();
        }
        if (!sharedPreferences.contains("read-push")) {
            SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
            ruleInitEditor.putString("read-push", gson.toJson(pushedReadList));
            ruleInitEditor.apply();
        }
        rulesList = gson.fromJson(sharedPreferences.getString("rules", "{}"), new TypeToken<ArrayList<RuleInfo>>() {
        }.getType());
        pushedReadList = gson.fromJson(sharedPreferences.getString("read-push", "{}"), new TypeToken<ArrayList<Integer>>() {
        }.getType());
        if (!sharedPreferences.contains("timed")) {
            SharedPreferences.Editor timedInitEditor = sharedPreferences.edit();
            timedInitEditor.putString("timed", gson.toJson(timedList));
            timedInitEditor.apply();
        } else {
            timedList = gson.fromJson(sharedPreferences.getString("timed", "{}"), new TypeToken<ArrayList<TimedInfo>>() {
            }.getType());
            for (TimedInfo timed : timedList) {
                if (timed.getScope() == 0) {
                    if (new Date().getDate() != new Date(timed.getFirstLaunchTime()).getDate()) {
                        timed.setStatus(false);
                    }
                } // clear obsolete only-once rules
            }
            SharedPreferences.Editor timedInitEditor = sharedPreferences.edit();
            timedInitEditor.putString("timed", gson.toJson(timedList));
            timedInitEditor.apply();
        }

        Intent serviceIntent = new Intent(this, ExecutorService.class);
        startService(serviceIntent);

        try {
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.scrisstudio.seenot/.service.ExecutorService");
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, "1");
        } catch (Exception ignored) {
        }

        isNotificationEnabled = getSystemService(NotificationManager.class).areNotificationsEnabled();
        isOverlayEnabled = Settings.canDrawOverlays(this);
        isBatteryOptimIgnored = ((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName());

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        viewCustomization = new SoftReference<>(inflater.inflate(R.layout.layout_assigner, null));//workaround for static view
        viewTarget = new SoftReference<>(inflater.inflate(R.layout.layout_view_target, null));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(mDrawerToggle);
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings, R.id.nav_permission, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.toString().contains("Home"))
                currentDestination = "Home";
            else if (destination.toString().contains("Permission"))
                currentDestination = "Permission";
            else if (destination.toString().contains("About"))
                currentDestination = "About";
            else if (destination.toString().contains("Settings"))
                currentDestination = "Settings";
            if (destination.toString().contains("Home")) {
                binding.toolbar.setTitle(R.string.app_full_name);
                checkIfBannerNecessary(getResources(), PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext()));
            }
            if (SeeNot.shouldNavigateTo == 0 && ((lastTimeDestination.contains("Permission") && (destination.toString().contains("Home") || destination.toString().contains("Permission"))) || (lastTimeDestination.contains("Settings") && (destination.toString().contains("Home") || destination.toString().contains("Settings"))) || (lastTimeDestination.contains("About") && (destination.toString().contains("Home"))))) {
                finish();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("password", "success");
                startActivity(intent);//sometime breaks, need more diag
            }
            lastTimeDestination = destination.toString();
        });

        ((TextView) binding.navView.getHeaderView(0).findViewById(R.id.version_nav)).setText("v" + APKVersionInfoUtils.getVersionName(this));

        if (SeeNot.shouldNavigateTo != 0) {
            navController.navigate(SeeNot.shouldNavigateTo);
            SeeNot.shouldNavigateTo = 0;
        }

        checkIfBannerNecessary(getResources(), PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext()));
        // password
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                extra = "";
            } else {
                extra = extras.getString("password");
            }
        } else {
            extra = (String) savedInstanceState.getSerializable("password");
        }
        if (extra == null) extra = "";
        if (!extra.equals("success")) passwordInit();

        // welcome
        if (!sharedPreferences.contains("welcome")) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
        }

        new Thread(() -> {
            URL url;
            Looper.prepare();
            try {
                url = new URL("https://seenot-1259749012.cos.ap-hongkong.myqcloud.com/push.json");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String inputLine;
                StringBuilder rawData = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    rawData.append(inputLine);
                FetcherInfo fetched = gson.fromJson(String.valueOf(rawData), new TypeToken<FetcherInfo>() {
                }.getType());
                ArrayList<PushedInfo> list = fetched.getPush();
                runOnUI(() -> APKVersionInfoUtils.openVersionDialog(MainActivity.this, fetched,
                        resources, sharedPreferences, true));
                for (PushedInfo info : list) {
                    if (!pushedReadList.contains(info.getId())) {
                        try {
                            //noinspection RestrictedApi
                            ((ActionMenuItemView) findViewById(R.id.notification_btn))
                                    .setIcon(ResourcesCompat.getDrawable(resources, R.drawable.baseline_notification_important_24, getTheme()));
                        } catch (Exception e) {
                            le("ERR: " + e);
                        }
                        break;
                    }
                }
                in.close();
            } catch (Exception e) {
                le("ERR: " + e);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        else if (item.getItemId() == R.id.notification_btn) {
            NotifyDialogFragment notifyDialogFragment = new NotifyDialogFragment();
            notifyDialogFragment.show(getSupportFragmentManager(), "NotifyDialogFragment");
        } else if (item.getItemId() == R.id.helper_btn) {
            if (!Objects.equals(currentDestination, "About")) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                View view = getLayoutInflater().inflate(R.layout.dialog_webview_helper, null);
                WebView webView = view.findViewById(R.id.helper_webview);
                ProgressBar progressBar = view.findViewById(R.id.helper_webview_progress);
                progressBar.setVisibility(View.VISIBLE);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                webView.loadUrl("https://seenot.pages.dev/" + getLocale() + "/" + currentDestination + ".html");
                builder.setView(view);
                builder.setPositiveButton(R.string.ok, null);
                builder.show();
            } else {
                new MaterialAlertDialogBuilder(this).setTitle(R.string.no_helper)
                        .setMessage(R.string.no_helper_msg).setPositiveButton(R.string.ok, null)
                        .show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static void passwordInit() {
        try {
            password = sharedPreferences.getString("password", "");
            if (!password.equals("")) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(SeeNot.getAppContext(), PasswordActivity.class);
                SeeNot.getAppContext().startActivity(intent);
            }
        } catch (Exception e) {
            le("Password init failed with message: " + e);
        }
    }

    public void checkIfBannerNecessary(Resources resources, SharedPreferences sharedPreferences) {
        // permission check
        if (!(isAccessibilitySettingsOn(SeeNot.getAppContext()) && Settings.canDrawOverlays(SeeNot.getAppContext()) &&
                getSystemService(NotificationManager.class).areNotificationsEnabled() &&
                ((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName()))) {
            Intent intent = new Intent("banner_channel");
            intent.putExtra("banner_message", resources.getString(R.string.service_not_running));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (!sharedPreferences.getBoolean("master-switch", true)) {// master switch check
            Intent intent = new Intent("banner_channel");
            intent.putExtra("banner_message", resources.getString(R.string.function_closed));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}