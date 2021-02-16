package com.scrisstudio.jianfou;
public class RuleInfo {
	private int id;
	private String ruleTitle;
	private String ruleFor;
	private String ruleType;

	public RuleInfo(int id, String ruleTitle, String ruleFor, String ruleType) {
		this.id = id;
		this.ruleTitle = ruleTitle;
		this.ruleFor = ruleFor;
		this.ruleType = ruleType;
	}

	public int getId() {return id;}

	public String getTitle() {return ruleTitle;}

	public String getFor() {return ruleFor;}

	public String getType() {return ruleType;}
}
