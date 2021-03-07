package com.scrisstudio.jianfou.ui;
public class RuleInfo {
	private final int id;
	private boolean status;
	private String ruleTitle;
	private String ruleVersion;
	private String ruleFor;
	private String ruleForVersion;
	private String ruleType;

	public RuleInfo(boolean status, int id, String ruleTitle, String ruleVersion, String ruleFor, String ruleForVersion, String ruleType) {
		this.status = status;
		this.id = id;
		this.ruleTitle = ruleTitle;
		this.ruleVersion = ruleVersion;
		this.ruleFor = ruleFor;
		this.ruleForVersion = ruleForVersion;
		this.ruleType = ruleType;
	}

	public boolean getStatus() {return status;}

	public void setStatus(boolean status) {this.status = status;}

	public int getId() {return id;}

	public String getTitle() {return ruleTitle;}

	public void setTitle(String ruleTitle) {this.ruleTitle = ruleTitle;}

	public String getFor() {return ruleFor;}

	public void setFor(String ruleFor) {this.ruleFor = ruleFor;}

	public String getForVersion() {return ruleForVersion;}

	public void setForVersion(String ruleForVersion) {this.ruleForVersion = ruleForVersion;}

	public String getVersion() {return ruleVersion;}

	public void setVersion(String ruleVersion) {this.ruleVersion = ruleVersion;}

	public String getType() {return ruleType;}

	public void setType(String ruleType) {this.ruleType = ruleType;}
}
