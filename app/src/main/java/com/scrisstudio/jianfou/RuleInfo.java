package com.scrisstudio.jianfou;
public class RuleInfo {
	private boolean status;
	private int id;
	private String ruleTitle;
	private String ruleFor;
	private String ruleType;

	public RuleInfo(boolean status, int id, String ruleTitle, String ruleFor, String ruleType) {
		this.status = status;
		this.id = id;
		this.ruleTitle = ruleTitle;
		this.ruleFor = ruleFor;
		this.ruleType = ruleType;
	}

	public boolean getStatus() {return status;}

	public void setStatus(boolean status) {this.status = status;}

	public int getId() {return id;}

	public String getTitle() {return ruleTitle;}

	public void setTitle(String ruleTitle) {this.ruleTitle = ruleTitle;}

	public String getFor() {return ruleFor;}

	public void setFor(String ruleFor) {this.ruleFor = ruleFor;}

	public String getType() {return ruleType;}

	public void setType(String ruleType) {this.ruleType = ruleType;}
}
