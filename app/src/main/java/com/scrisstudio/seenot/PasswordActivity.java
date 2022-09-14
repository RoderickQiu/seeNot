package com.scrisstudio.seenot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.scrisstudio.seenot.databinding.ActivityPasswordBinding;

public class PasswordActivity extends AppCompatActivity {

    private ActivityPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext());

        binding.passwordSubmit.setOnClickListener(v -> {
            String input = binding.passwordInput.getText().toString();
            if (sharedPreferences.getString("password", "").equals(input)) {
                Toast.makeText(PasswordActivity.this, R.string.password_right, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(PasswordActivity.this, MainActivity.class);
                intent.putExtra("password", "success");
                startActivity(intent);
            } else {
                Toast.makeText(PasswordActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                binding.passwordInput.setText("");
            }
        });
    }
}