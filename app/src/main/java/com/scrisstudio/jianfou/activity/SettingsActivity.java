package com.scrisstudio.jianfou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

	ActivitySettingsBinding binding;

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		startActivity(intent);
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
			Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
			startActivity(intent);
		});
	}
}
