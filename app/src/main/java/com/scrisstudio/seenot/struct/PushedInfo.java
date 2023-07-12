package com.scrisstudio.seenot.struct;

import androidx.annotation.NonNull;

public class PushedInfo {
    String title;
    String msg;
    String version;
    int id;

    public PushedInfo(String title, String msg, String version, int id) {
        this.title = title;
        this.msg = msg;
        this.version = version;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getMsg() {
        return msg;
    }

    public String getVersion() {
        return version;
    }

    public int getId() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        return "PushedInfo{" +
                "title='" + title + '\'' +
                ", msg='" + msg + '\'' +
                ", version='" + version + '\'' +
                ", id=" + id +
                '}';
    }
}
