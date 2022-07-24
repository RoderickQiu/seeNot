package com.scrisstudio.seenot.service;

public class FilterInfo {
    private boolean status;
    private String type;
    private String param1, param2;

    public FilterInfo(boolean status, String type, String param1, String param2) {
        this.status = status;
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam2() {
        return param2;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
