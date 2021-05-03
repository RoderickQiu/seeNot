package com.scrisstudio.jianfou.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

	ActivityAboutBinding binding;

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
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().setStatusBarColor(getResources().getColor(R.color.scris, getTheme()));

		binding.settingsAppBar.setNavigationOnClickListener(v -> {
			AboutActivity.this.finish();
		});

		binding.scrisStudioApp.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		binding.scrisStudioApp.getPaint().setAntiAlias(true);
		binding.scrisStudioApp.setOnClickListener((v) -> {
			Uri uri = Uri.parse("https://scris.top");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		});
	}
}
