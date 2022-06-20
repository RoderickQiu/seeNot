package com.scrisstudio.seenot.ui.permission;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PermissionViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PermissionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is permission fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}