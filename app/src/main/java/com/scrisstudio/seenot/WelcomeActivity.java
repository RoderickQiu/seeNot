package com.scrisstudio.seenot;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.scrisstudio.seenot.ui.welcome.WelcomeFragment;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, WelcomeFragment.newInstance())
                    .commitNow();
        }

        SharedPreferences.Editor ruleInitEditor = PreferenceManager.getDefaultSharedPreferences(SeeNot.getAppContext()).edit();
        ruleInitEditor.putBoolean("welcome", true);
        ruleInitEditor.apply();
    }
}