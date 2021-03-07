package com.scrisstudio.jianfou.activity;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.scrisstudio.jianfou.R;
public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings, rootKey);
	}
}

