package com.scrisstudio.seenot.struct;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class FetcherInfo {
    ArrayList<PushedInfo> push;
    String version;

    public FetcherInfo(ArrayList<PushedInfo> push, String version) {
        this.push = push;
        this.version = version;
    }

    public ArrayList<PushedInfo> getPush() {
        return push;
    }

    public String getVersion() {
        return version;
    }

    @NonNull
    @Override
    public String toString() {
        return "FetcherInfo{" +
                "push=" + push +
                ", version=" + version +
                '}';
    }
}
