package com.scrisstudio.seenot.ui.about;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.scrisstudio.seenot.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.scrisStudioApp.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.scrisStudioApp.getPaint().setAntiAlias(true);
        binding.scrisStudioApp.setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://r-q.name");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        binding.licensePage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.licensePage.getPaint().setAntiAlias(true);
        binding.licensePage.setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://seenot.r-q.name/license-report");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        binding.privacyNoticePage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.privacyNoticePage.getPaint().setAntiAlias(true);
        binding.privacyNoticePage.setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://seenot.r-q.name/privacy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}