package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.l;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.scrisstudio.seenot.MainActivity;

public class ApplicationObserver implements DefaultLifecycleObserver {
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        l("onResume");
        MainActivity.passwordInit();
    }
}