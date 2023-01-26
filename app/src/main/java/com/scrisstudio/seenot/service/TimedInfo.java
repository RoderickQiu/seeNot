package com.scrisstudio.seenot.service;

public class TimedInfo {
    final private int id;
    private String name;
    private boolean status;
    private boolean mode;
    private int idFor;
    private int scope;
    private long startTime, endTime;

    public TimedInfo(int id, String name, boolean status, boolean mode, int idFor, int scope, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.mode = mode;
        this.idFor = idFor;
        this.scope = scope;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean getMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getIdFor() {
        return idFor;
    }

    public void setIdFor(int idFor) {
        this.idFor = idFor;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TimedInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", idFor=" + idFor +
                ", scope=" + scope +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
