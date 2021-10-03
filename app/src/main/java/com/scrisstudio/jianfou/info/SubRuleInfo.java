package com.scrisstudio.jianfou.info;

import java.util.ArrayList;

public class SubRuleInfo {
	private int type;
	private ArrayList<String> text;
	private ArrayList<Integer> dynamicParentLevel;
	private ArrayList<ArrayList<Integer>> filter;
	private int filterLength;
	private String conditionActivity;
	private ArrayList<SkipInfo> skip;
	private int skipLength;

	public SubRuleInfo(int type, ArrayList<ArrayList<Integer>> filter, ArrayList<String> text, ArrayList<Integer> dynamicParentLevel, int filterLength, String conditionActivity, ArrayList<SkipInfo> skip, int skipLength) {
		this.type = type;
		this.filter = filter;
		this.text = text;
		this.dynamicParentLevel = dynamicParentLevel;
		this.filterLength = filterLength;
		this.conditionActivity = conditionActivity;
		this.skip = skip;
		this.skipLength = skipLength;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public ArrayList<ArrayList<Integer>> getFilter() {
		return filter;
	}

	public void setFilter(ArrayList<ArrayList<Integer>> filter) {
		this.filter = filter;
	}

	public ArrayList<String> getText() {
		return text;
	}

	public void setText(ArrayList<String> text) {
		this.text = text;
	}

	public ArrayList<Integer> getDynamicParentLevel() {
		return dynamicParentLevel;
	}

	public void setDynamicParentLevel(ArrayList<Integer> dynamicParentLevel) {
		this.dynamicParentLevel = dynamicParentLevel;
	}

	public int getFilterLength() {
		return filterLength;
	}

	public void setFilterLength(int filterLength) {
		this.filterLength = filterLength;
	}

	public String getConditionActivity() {
		return conditionActivity;
	}

	public void setConditionActivity(String conditionActivity) {
		this.conditionActivity = conditionActivity;
	}

	public ArrayList<SkipInfo> getSkip() {
		return skip;
	}

	public void setSkip(ArrayList<SkipInfo> skip) {
		this.skip = skip;
	}

	public int getSkipLength() {
		return skipLength;
	}

	public void setSkipLength(int skipLength) {
		this.skipLength = skipLength;
	}

}
