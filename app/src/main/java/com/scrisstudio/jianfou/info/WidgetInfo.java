package com.scrisstudio.jianfou.info;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Objects;

public class WidgetInfo {
	public String packageName, activityName, className, idName, description, text;
	public Rect position;
	public ArrayList<Integer> indices;
	public boolean clickable, onlyClick;

	public WidgetInfo() {
		this.packageName = "";
		this.activityName = "";
		this.className = "";
		this.idName = "";
		this.description = "";
		this.text = "";
		this.position = new Rect();
		this.indices = new ArrayList<Integer>();
		this.clickable = false;
		this.onlyClick = false;
	}

	public WidgetInfo(String packageName, String activityName, String className, String idName, String description, String text, Rect position, ArrayList<Integer> indices, boolean clickable, boolean onlyClick) {
		this.packageName = packageName;
		this.activityName = activityName;
		this.className = className;
		this.idName = idName;
		this.description = description;
		this.text = text;
		this.position = position;
		this.indices = indices;
		this.clickable = clickable;
		this.onlyClick = onlyClick;
	}

	public WidgetInfo(WidgetInfo widgetDescription) {
		this.packageName = widgetDescription.packageName;
		this.activityName = widgetDescription.activityName;
		this.className = widgetDescription.className;
		this.idName = widgetDescription.idName;
		this.description = widgetDescription.description;
		this.text = widgetDescription.text;
		this.indices = widgetDescription.indices;
		this.position = new Rect(widgetDescription.position);
		this.clickable = widgetDescription.clickable;
		this.onlyClick = widgetDescription.onlyClick;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WidgetInfo)) return false;
		WidgetInfo widget = (WidgetInfo) obj;
		return position.equals(widget.position);
	}

	@Override
	public int hashCode() {
		return Objects.hash(position);
	}
}
