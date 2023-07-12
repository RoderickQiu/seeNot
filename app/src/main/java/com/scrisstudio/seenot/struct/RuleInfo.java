package com.scrisstudio.seenot.struct;

import java.util.ArrayList;

public class RuleInfo {
    private int id;
    private boolean status;
    private String title;
    private String ruleFor;
    private String ruleForName;
    private ArrayList<FilterInfo> filter;
    private int filterLength;
    private long reopenTime;

    public RuleInfo(int id, boolean status, String title, String ruleFor, String ruleForName, ArrayList<FilterInfo> filter, int filterLength, long reopenTime) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.ruleFor = ruleFor;
        this.ruleForName = ruleForName;
        this.filter = filter;
        this.filterLength = filterLength;
        this.reopenTime = reopenTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFor() {
        return ruleFor;
    }

    public void setFor(String ruleFor) {
        this.ruleFor = ruleFor;
    }

    public String getForName() {
        return ruleForName;
    }

    public void setForName(String ruleForName) {
        this.ruleForName = ruleForName;
    }

    public ArrayList<FilterInfo> getFilter() {
        return filter;
    }

    public void setFilter(ArrayList<FilterInfo> filter) {
        this.filter = filter;
    }

    public int getFilterLength() {
        return filterLength;
    }

    public void setFilterLength(int filterLength) {
        this.filterLength = filterLength;
    }

    public long getReopenTime() {
        return reopenTime;
    }

    public void setReopenTime(long reopenTime) {
        this.reopenTime = reopenTime;
    }
}
