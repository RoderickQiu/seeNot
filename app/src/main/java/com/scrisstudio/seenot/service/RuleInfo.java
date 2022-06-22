package com.scrisstudio.seenot.service;

import java.util.ArrayList;

public class RuleInfo {
    private final int id;
    private boolean status;
    private String ruleTitle;
    private String ruleFor;
    private String ruleForName;
    private ArrayList<WidgetInfo> filter;
    private int filterLength;

    public RuleInfo(boolean status, int id, String ruleTitle, String ruleFor, String ruleForName, ArrayList<WidgetInfo> filter, int filterLength) {
        this.status = status;
        this.id = id;
        this.ruleTitle = ruleTitle;
        this.ruleFor = ruleFor;
        this.ruleForName = ruleForName;
        this.filter = filter;
        this.filterLength = filterLength;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return ruleTitle;
    }

    public void setTitle(String ruleTitle) {
        this.ruleTitle = ruleTitle;
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

    public ArrayList<WidgetInfo> getFilter() {
        return filter;
    }

    public void setFilter(ArrayList<WidgetInfo> filter) {
        this.filter = filter;
    }

    public int getFilterLength() {
        return filterLength;
    }

    public void setFilterLength(int filterLength) {
        this.filterLength = filterLength;
    }
}
