package com.scrisstudio.jianfou.info;

public class SkipInfo {
	private int type;
	private String param;

	public SkipInfo(int type, String param) {
		this.type = type;
		this.param = param;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

}
