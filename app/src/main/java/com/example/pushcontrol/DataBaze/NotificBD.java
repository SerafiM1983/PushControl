package com.example.pushcontrol.DataBaze;

public class NotificBD {
	private String title;
	private String text;
	private String packageName;

	public NotificBD(String packageName, String text, String title) {
		this.packageName = packageName;
		this.text = text;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
