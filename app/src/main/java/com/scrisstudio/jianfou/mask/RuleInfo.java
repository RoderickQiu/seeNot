package com.scrisstudio.jianfou.mask;

import java.util.ArrayList;

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
	private String dynamicText;
	private int dynamicParentLevel;
	private ArrayList<WidgetInfo> filter;
	private int filterLength;

	public RuleInfo(boolean status, int id, String ruleTitle, String ruleVersion, String ruleFor, String ruleForVersion,
	                int ruleType, ArrayList<WidgetInfo> filter, String aidText, String skipText, String dynamicText, int dynamicParentLevel, int filterLength) {
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
		this.dynamicText = dynamicText;
		this.dynamicParentLevel = dynamicParentLevel;
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

	public ArrayList<WidgetInfo> getFilter() {
		return filter;
	}

	public void setFilter(ArrayList<WidgetInfo> filter) {
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

	public String getDynamicText() {
		return dynamicText;
	}

	public void setDynamicText(String dynamicText) {
		this.dynamicText = dynamicText;
	}

	public int getDynamicParentLevel() {
		return dynamicParentLevel;
	}

	public void setDynamicParentLevel(int dynamicParentLevel) {
		this.dynamicParentLevel = dynamicParentLevel;
	}

	public int getFilterLength() {
		return filterLength;
	}

	public void setFilterLength(int filterLength) {
		this.filterLength = filterLength;
	}
}
