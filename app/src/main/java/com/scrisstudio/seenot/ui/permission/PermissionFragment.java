package com.scrisstudio.seenot.ui.permission;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.scrisstudio.seenot.databinding.FragmentPermissionBinding;

public class PermissionFragment extends Fragment {

    private FragmentPermissionBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PermissionViewModel permissionViewModel =
                new ViewModelProvider(this).get(PermissionViewModel.class);

        binding = FragmentPermissionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPermission;
        permissionViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}