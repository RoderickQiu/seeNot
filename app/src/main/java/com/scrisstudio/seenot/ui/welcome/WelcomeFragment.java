package com.scrisstudio.seenot.ui.welcome;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.scrisstudio.seenot.MainActivity;
import com.scrisstudio.seenot.R;
import com.scrisstudio.seenot.SeeNot;

public class WelcomeFragment extends Fragment {

    private WelcomeViewModel mViewModel;

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_main, container, false);

        view.findViewById(R.id.welcome_submit).setOnClickListener(v -> {
            /*FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new PrivNoteFragment())
                    .addToBackStack(null)
                    .commit();*/
            SeeNot.shouldNavigateTo = R.id.nav_permission;
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.privacy_notice).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://seenot.r-q.name/privacy");
            intent.setData(content_url);
            startActivity(intent);
        });

        return view;
    }

}