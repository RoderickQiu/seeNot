package com.scrisstudio.jianfou.info;

public class PositionInfo {
	public String packageName;
	public String activityName;
	public int x;
	public int y;
	public int delay;
	public int period;
	public int number;

	public PositionInfo() {
		this.packageName = "";
		this.activityName = "";
		this.x = 0;
		this.y = 0;
		this.delay = 0;
		this.period = 0;
		this.number = 0;
	}

	public PositionInfo(String packageName, String activityName, int x, int y, int delay, int period, int number) {
		this.packageName = packageName;
		this.activityName = activityName;
		this.x = x;
		this.y = y;
		this.delay = delay;
		this.period = period;
		this.number = number;
	}

	public PositionInfo(PositionInfo positionDescription) {
		this.packageName = positionDescription.packageName;
		this.activityName = positionDescription.activityName;
		this.x = positionDescription.x;
		this.y = positionDescription.y;
		this.delay = positionDescription.delay;
		this.period = positionDescription.period;
		this.number = positionDescription.number;
	}
}

