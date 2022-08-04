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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scottyab.aescrypt.AESCrypt;
import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.service.ExecutorService;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final Gson gson = new Gson();
    private final String password = "seenot";

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
    };

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
        Preference preferenceExport = findPreference("export");
        if (preferenceExport != null) {
            preferenceExport.setOnPreferenceClickListener(preference -> {
                Map<String, ?> allEntries = Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).getAll();
                String output = gson.toJson(allEntries);

                // aes encrypt
                String encryptedMsg = "";
                try {
                    encryptedMsg = AESCrypt.encrypt(password, output);
                } catch (GeneralSecurityException e) {
                    le(e.getLocalizedMessage());
                }
                le("Output: " + encryptedMsg);

                // copy to clipboard
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copied", encryptedMsg);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_LONG).show();

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