package com.scrisstudio.jianfou.mask;

import java.util.ArrayList;

public class MixedRuleInfo {
	private final int id;
	private boolean status;
	private String ruleTitle;
	private String ruleVersion;
	private String ruleFor;
	private String ruleForPackageName;
	private String ruleForVersion;
	private int subRuleLength;
	private ArrayList<SubRuleInfo> subRules;

	public MixedRuleInfo(boolean status, int id, String ruleTitle, String ruleVersion, String ruleFor, String ruleForVersion, String ruleForPackageName, ArrayList<SubRuleInfo> subRules, int subRuleLength) {
		this.status = status;
		this.id = id;
		this.ruleTitle = ruleTitle;
		this.ruleVersion = ruleVersion;
		this.ruleFor = ruleFor;
		this.ruleForVersion = ruleForVersion;
		this.ruleForPackageName = ruleForPackageName;
		this.subRules = subRules;
		this.subRuleLength = subRuleLength;
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

	public String getForPackageName() {
		return ruleForPackageName;
	}

	public void setForPackageName(String ruleForPackageName) {
		this.ruleForPackageName = ruleForPackageName;
	}

	public String getVersion() {
		return ruleVersion;
	}

	public void setVersion(String ruleVersion) {
		this.ruleVersion = ruleVersion;
	}

	public ArrayList<SubRuleInfo> getSubRules() {
		return subRules;
	}

	public void setSubRules(ArrayList<SubRuleInfo> subRules) {
		this.subRules = subRules;
	}

	public int getSubRuleLength() {
		return subRuleLength;
	}

	public void setSubRuleLength(int subRuleLength) {
		this.subRuleLength = subRuleLength;
	}
}
