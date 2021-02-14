package com.scrisstudio.jianfou;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.scrisstudio.jianfou.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "Jianfou";
	ActivityMainBinding binding;

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		binding.topAppBar.setOnMenuItemClickListener(menuItem -> {
			switch (menuItem.getItemId()) {
				case R.id.help:
					Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
					return true;
				default:
					return false;
			}
		});

		binding.topAppBar.setNavigationOnClickListener(v -> binding.drawerLayout.openDrawer(binding.navigation));

		binding.navigation.setNavigationItemSelectedListener(menuItem -> {
			for (int i = 0; i < binding.navigation.getMenu().size(); i++)
				binding.navigation.getMenu().getItem(i).setChecked(false);

			menuItem.setChecked(true);
			binding.drawerLayout.closeDrawer(binding.navigation);

			switch (menuItem.getItemId()) {
				default:
					Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
					return false;
			}
		});

		binding.floatingActionButton.setOnClickListener(v -> {
			Toast.makeText(this.getApplicationContext(), "还没有完成。", Toast.LENGTH_LONG).show();
		});

	}
}
