package com.scrisstudio.seenot.ui.settings;

import static com.scrisstudio.seenot.SeeNot.l;
import static com.scrisstudio.seenot.SeeNot.le;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scottyab.aescrypt.AESCrypt;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.RuleTimedActivity;
import com.scrisstudio.seenot.SeeNot;
import com.scrisstudio.seenot.service.ExecutorService;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final Gson gson = new Gson();
    public static final String password = "seenot";

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (Objects.equals(key, "master-switch")) {
            ExecutorService.isServiceRunning = sharedPreferences.getBoolean(key, true);
        } else if (Objects.equals(key, "import")) {
            if (!sharedPreferences.contains(key)) return;

            String encryptedMsg = sharedPreferences.getString(key, "U2FsdGVkX18hRajw1ysftIH4UHYmZK3OH6YV7QzBCu8=");
            if (encryptedMsg.equals("")) return;

            // aes decrypt
            Map<String, ?> map = null;
            try {
                try {
                    String messageAfterDecrypt = AESCrypt.decrypt(password, encryptedMsg);
                    map = gson.fromJson(messageAfterDecrypt, new TypeToken<Map<String, ?>>() {
                    }.getType());
                    l("Imported: " + map);
                } catch (GeneralSecurityException e) {
                    le(e.getLocalizedMessage());
                    Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ignored) {
            }

            // clear redundant string
            EditTextPreference importPreference = findPreference(key);
            if (importPreference != null) importPreference.setText("");

            if (map != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // import everything
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    le(entry.getValue().getClass() + ", " + entry.getKey() + ", " + entry.getValue());
                    if (entry.getValue().getClass().equals(String.class)) {
                        editor.putString(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue().getClass().equals(Boolean.class)) {
                        editor.putBoolean(entry.getKey(), (boolean) entry.getValue());
                    } else if (entry.getValue().getClass().equals(Integer.class)) {
                        editor.putInt(entry.getKey(), (int) entry.getValue());
                    } else if (entry.getValue().getClass().equals(Double.class)) {
                        editor.putInt(entry.getKey(), (int) Math.round((double) entry.getValue()));
                    } else if (entry.getValue().getClass().equals(Float.class)) {
                        editor.putInt(entry.getKey(), Math.round((float) entry.getValue()));
                    } else le("Illegal type: " + entry.getValue().getClass());
                }

                // clear temp import string
                editor.remove(key);
                editor.apply();

                Toast.makeText(getContext(), R.string.import_succeed, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            } else Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_SHORT).show();
        }
        MainActivity.setSharedPreferences(sharedPreferences);
    };

    public static boolean checkReservation(Preference preference, Context context) {
        boolean isSwitch = preference instanceof SwitchPreferenceCompat;
        int reservationLockTime = Integer.parseInt(MainActivity.sharedPreferences.getString("reservation-lock", "0"));
        if (reservationLockTime > 0 && (!isSwitch || !((SwitchPreferenceCompat) preference).isChecked())) {
            long timeDelta = new Date().getTime() - MainActivity.sharedPreferences.getLong("rl-time", new Date().getTime());
            if (timeDelta <= reservationLockTime * 60 * 1000L) {
                if (isSwitch) ((SwitchPreferenceCompat) preference).setChecked(true);

                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.reserved_title)
                        .setMessage(R.string.reserved_msg)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(timeDelta > 0 ? ("预约还剩 " + (int) (Math.ceil((double) timeDelta / 60000f)) + " 分") :
                                "现在预约", (dialogInterface, i) -> {
                            if (timeDelta <= 0) {
                                SharedPreferences.Editor ruleInitEditor = MainActivity.sharedPreferences.edit();
                                ruleInitEditor.putLong("rl-time", new Date().getTime());
                                ruleInitEditor.apply();
                            }
                        })
                        .show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
        requireActivity().setTheme(R.style.Theme_SeeNot);

        Preference masterSwitch = findPreference("master-switch");
        if (masterSwitch != null) {
            masterSwitch.setOnPreferenceClickListener(preference -> {
                checkReservation(masterSwitch, requireContext());
                return false;
            });
        }

        Preference timedSettings = findPreference("timed-settings");
        if (timedSettings != null) {
            timedSettings.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(SeeNot.getAppContext(), RuleTimedActivity.class);
                SeeNot.getAppContext().startActivity(intent);
                return false;
            });
        }

        Preference preferenceExport = findPreference("export");
        if (preferenceExport != null) {
            preferenceExport.setOnPreferenceClickListener(preference -> {
                Map<String, ?> allEntries = Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).getAll();
                String output = gson.toJson(allEntries);

                // aes encrypt
                String encryptedMsg = "";
                try {
                    encryptedMsg = AESCrypt.encrypt(password, output);

                    // copy to clipboard
                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("copied", encryptedMsg);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_LONG).show();
                } catch (GeneralSecurityException e) {
                    le(e.getLocalizedMessage());
                    Toast.makeText(requireContext(), R.string.strange_error, Toast.LENGTH_LONG).show();
                }
                le("Output: " + encryptedMsg);

                return false;
            });
        }

        Preference preferenceSingleExport = findPreference("export-single");
        if (preferenceSingleExport != null) {
            preferenceSingleExport.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(requireContext()).
                        setTitle(R.string.settings_export_single).
                        setMessage("请在首页该规则菜单中选择”导出“以导出。").
                        setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        }).
                        create().show();
                return false;
            });
        }

        Preference passwordPreference = findPreference("password");
        if (passwordPreference != null) {
            passwordPreference.setOnPreferenceClickListener(preference -> {
                if (checkReservation(preference, requireContext())) {
                    PwdDialogFragment.display(getFragmentManager(), MainActivity.sharedPreferences);
                    PwdDialogFragment.setOnSubmitListener(() -> {
                    });
                }
                return false;
            });
        }

        Preference reservationLockPreference = findPreference("reservation-lock");
        if (reservationLockPreference != null) {
            reservationLockPreference.setOnPreferenceClickListener(preference -> {
                if (checkReservation(preference, requireContext())) {
                    RLockDialogFragment.display(getFragmentManager(), MainActivity.sharedPreferences);
                    RLockDialogFragment.setOnSubmitListener(() -> {
                    });
                }
                return false;
            });
        }

        Preference exportLogPreference = findPreference("export-log");
        if (exportLogPreference != null) {
            exportLogPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(requireContext()).
                        setTitle(R.string.export_log).
                        setMessage("请到系统文件应用中前往 " + requireContext().getExternalFilesDir(null)
                                + "/logs 以手动导出所需要的日志。").
                        setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        }).
                        create().show();
                return false;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getPreferenceManager().getSharedPreferences())
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getPreferenceManager().getSharedPreferences())
                .unregisterOnSharedPreferenceChangeListener(listener);
    }
}