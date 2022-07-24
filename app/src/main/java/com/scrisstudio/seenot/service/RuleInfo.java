package com.scrisstudio.seenot.service;

import java.util.ArrayList;

public class RuleInfo {
    private final int id;
    private String title;
    private String ruleFor;
    private String ruleForName;
    private ArrayList<FilterInfo> filter;
    private int filterLength;

    public RuleInfo(int id, String title, String ruleFor, String ruleForName, ArrayList<FilterInfo> filter, int filterLength) {
        this.id = id;
        this.title = title;
        this.ruleFor = ruleFor;
        this.ruleForName = ruleForName;
        this.filter = filter;
        this.filterLength = filterLength;
    }

    public int getId() {
        return id;
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
}
