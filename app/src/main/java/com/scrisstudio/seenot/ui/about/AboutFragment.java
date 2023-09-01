package com.scrisstudio.seenot.ui.about;

import static com.scrisstudio.seenot.MainActivity.runOnUI;
import static com.scrisstudio.seenot.MainActivity.sharedPreferences;
import static com.scrisstudio.seenot.SeeNot.le;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.databinding.FragmentAboutBinding;
import com.scrisstudio.seenot.service.APKVersionInfoUtils;
import com.scrisstudio.seenot.struct.FetcherInfo;
import com.scrisstudio.seenot.struct.PushedInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.version.setText("v" + APKVersionInfoUtils.getVersionName(requireContext()));

        binding.scrisStudioApp.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.scrisStudioApp.getPaint().setAntiAlias(true);
        binding.scrisStudioApp.setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://r-q.name");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        binding.webpage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.webpage.getPaint().setAntiAlias(true);
        binding.webpage.setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://seenot.r-q.name");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        binding.updateManually.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.updateManually.getPaint().setAntiAlias(true);
        binding.updateManually.setOnClickListener((v) -> {
            getUpdateMsg(false);
        });

        binding.gplLicensed.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.gplLicensed.getPaint().setAntiAlias(true);
        binding.gplLicensed.setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://www.gnu.org/licenses/gpl-3.0.html");
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

        getUpdateMsg(true);

        return root;
    }

    private void getUpdateMsg(boolean isAuto) {
        Gson gson = new Gson();
        new Thread(() -> {
            URL url;
            Looper.prepare();
            try {
                url = new URL("https://seenot-1259749012.cos.ap-hongkong.myqcloud.com/push.json");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String inputLine;
                StringBuilder rawData = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    rawData.append(inputLine);
                FetcherInfo fetched = gson.fromJson(String.valueOf(rawData), new TypeToken<FetcherInfo>() {
                }.getType());
                runOnUI(() -> APKVersionInfoUtils.openVersionDialog(requireContext(), fetched,
                        getResources(), sharedPreferences, isAuto));
                in.close();
            } catch (Exception e) {
                le("ERR: " + e);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}