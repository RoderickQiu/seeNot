package com.scrisstudio.seenot.ui.notification;

import static com.scrisstudio.seenot.MainActivity.sharedPreferences;
import static com.scrisstudio.seenot.SeeNot.le;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.struct.FetcherInfo;
import com.scrisstudio.seenot.struct.PushedInfo;
import com.scrisstudio.seenot.ui.rule.RuleInfoCardDecoration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class NotifyDialogFragment extends DialogFragment {

    private static final String TAG = "AKDialogFragment";
    private static Gson gson = new Gson();
    private static ArrayList<PushedInfo> list = new ArrayList<>();
    private static FetcherInfo fetched;
    private static ArrayList<Integer> pushedReadList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_full_screen, container, false);
        MaterialToolbar toolbar = (MaterialToolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.notifications_title);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        RecyclerView recyclerView = rootView.findViewById(R.id.pushed_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        PushedInfoAdapter adapter = new PushedInfoAdapter(requireContext(), getParentFragmentManager(), list, sharedPreferences);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RuleInfoCardDecoration());

        pushedReadList = gson.fromJson(sharedPreferences.getString("read-push", "{}"), new TypeToken<ArrayList<Integer>>() {
        }.getType());

        new Thread(() -> {
            URL url = null;
            try {
                url = new URL("https://seenot-1259749012.cos.ap-hongkong.myqcloud.com/push.json");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String inputLine;
                StringBuilder rawData = new StringBuilder("");
                while ((inputLine = in.readLine()) != null)
                    rawData.append(inputLine);
                fetched = gson.fromJson(String.valueOf(rawData), new TypeToken<FetcherInfo>() {
                }.getType());
                list = fetched.getPush();
                requireActivity().runOnUiThread(() -> adapter.dataChange(list));
                for (PushedInfo info : list) {
                    if (!pushedReadList.contains(info.getId())) {
                        pushedReadList.add(info.getId());
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    //noinspection RestrictedApi
                    ((ActionMenuItemView) requireActivity().findViewById(R.id.notification_btn))
                            .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_notifications_none_24, requireActivity().getTheme()));
                });
                SharedPreferences.Editor ruleInitEditor = sharedPreferences.edit();
                ruleInitEditor.putString("read-push", gson.toJson(pushedReadList));
                ruleInitEditor.apply();
                in.close();
            } catch (Exception e) {
                le("ERR: " + e);
            }
        }).start();

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        View theDialogView = onCreateView(getLayoutInflater(), null, savedInstanceState);
        builder.setView(theDialogView);
        return builder.create();
    }
}