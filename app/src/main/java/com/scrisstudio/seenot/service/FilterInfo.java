package com.scrisstudio.seenot.service;

public class FilterInfo {
    private boolean status;
    private int type, ruleId;
    private String param1, param2;

    public FilterInfo(boolean status, int type, int ruleId, String param1, String param2) {
        this.status = status;
        this.ruleId = ruleId;
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }

    public int getRuleId() {
        return ruleId;
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

    public void setType(int type) {
        this.type = type;
    }

    /**
     * case 0: return R.string.filter_ban_app;
     * <p>
     * case 1: return R.string.filter_ban_activity;
     * <p>
     * case 2: return R.string.filter_ban_text;
     * <p>
     * case 3: return R.string.filter_ban_id;
     * <p>
     * case 4: return R.string.filter_auto_click;
     */
    public int getType() {
        return type;
    }
}
