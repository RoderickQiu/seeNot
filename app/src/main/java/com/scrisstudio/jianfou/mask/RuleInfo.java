package com.scrisstudio.jianfou.mask;

public class RuleInfo {
	private final int id;
	private boolean status;
	private String ruleTitle;
	private String ruleVersion;
	private String ruleFor;
	private String ruleForVersion;
	private int ruleType;
	private String aidText;
	private String skipText;
	private WidgetInfo filter;

	public RuleInfo(boolean status, int id, String ruleTitle, String ruleVersion, String ruleFor, String ruleForVersion, int ruleType, WidgetInfo filter, String aidText, String skipText) {
		this.status = status;
		this.id = id;
		this.ruleTitle = ruleTitle;
		this.ruleVersion = ruleVersion;
		this.ruleFor = ruleFor;
		this.ruleForVersion = ruleForVersion;
		this.ruleType = ruleType;
		this.filter = filter;
		this.aidText = aidText;
		this.skipText = skipText;
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

	public String getForVersion() {
		return ruleForVersion;
	}

	public void setForVersion(String ruleForVersion) {
		this.ruleForVersion = ruleForVersion;
	}

	public String getVersion() {
		return ruleVersion;
	}

	public void setVersion(String ruleVersion) {
		this.ruleVersion = ruleVersion;
	}

	public int getType() {
		return ruleType;
	}

	public void setType(int ruleType) {
		this.ruleType = ruleType;
	}

	public WidgetInfo getFilter() {
		return filter;
	}

	public void setFilter(WidgetInfo filter) {
		this.filter = filter;
	}

	public String getAidText() {
		return aidText;
	}

	public void setAidText(String aidText) {
		this.aidText = aidText;
	}

	public String getSkipText() {
		return skipText;
	}

	public void setSkipText(String skipText) {
		this.skipText = skipText;
	}
}
