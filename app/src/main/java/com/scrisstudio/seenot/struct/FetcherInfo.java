package com.scrisstudio.seenot.struct;

import androidx.annotation.NonNull;

import com.scrisstudio.seenot.SeeNot;

import java.util.ArrayList;

public class FetcherInfo {
    ArrayList<PushedInfo> pushZh, pushEn;
    String version;
    ArrayList<String> updaterZh, updaterEn;

    public FetcherInfo(ArrayList<PushedInfo> pushEn, ArrayList<PushedInfo> pushZh,
                       String version, ArrayList<String> updaterZh, ArrayList<String> updaterEn) {
        this.pushEn = pushEn;
        this.pushZh = pushZh;
        this.updaterZh = updaterZh;
        this.updaterEn = updaterEn;
        this.version = version;
    }

    public ArrayList<PushedInfo> getPush() {
        return SeeNot.getLocale().equals("zh") ? pushZh : pushEn;
    }

    public String getVersion() {
        return version;
    }

    public ArrayList<String> getUpdater() {
        return SeeNot.getLocale().equals("zh") ? updaterZh : updaterEn;
    }

    @NonNull
    @Override
    public String toString() {
        return "FetcherInfo{" +
                "pushZh=" + pushZh +
                ", pushEn=" + pushEn +
                ", version='" + version + '\'' +
                ", updaterZh=" + updaterZh +
                ", updaterEn=" + updaterEn +
                '}';
    }
}
