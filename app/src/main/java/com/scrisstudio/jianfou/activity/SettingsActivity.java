package com.scrisstudio.jianfou.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.databinding.ActivitySettingsBinding;
import com.scrisstudio.jianfou.mask.MixedExecutorService;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

	private final SharedPreferences.OnSharedPreferenceChangeListener mListener = (sharedPreferences, key) -> {
		if (Objects.equals(key, "master-switch")) {
			MixedExecutorService.isServiceRunning = sharedPreferences.getBoolean(key, true);
		}
	};
	ActivitySettingsBinding binding;

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		finish();
	}

	@Override
	protected void onResume() {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
				registerOnSharedPreferenceChangeListener(mListener);
		super.onResume();
	}

	@Override
	protected void onPause() {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
				unregisterOnSharedPreferenceChangeListener(mListener);
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySettingsBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit();

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
	}
}
