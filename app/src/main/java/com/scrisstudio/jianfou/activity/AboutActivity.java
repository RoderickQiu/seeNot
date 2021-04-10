package com.scrisstudio.jianfou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

	ActivityAboutBinding binding;

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		Intent intent = new Intent(AboutActivity.this, MainActivity.class);
		startActivity(intent);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAboutBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//注意要清除 FLAG_TRANSLUCENT_STATUS flag
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().setStatusBarColor(getResources().getColor(R.color.scris, getTheme()));

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
			Intent intent = new Intent(AboutActivity.this, MainActivity.class);
			startActivity(intent);
		});
	}
}
