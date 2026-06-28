package com.example.pushcontrol.DataBaze;

public class NotificBD {
	private String title;
	private String text;
	private String packageName;
	long id;

	public NotificBD(String packageName, String text, String title, Long id) {
		this.packageName = packageName;
		this.text = text;
		this.title = title;
		this.id = id;
	}
	public NotificBD(String packageName, String text, String title) {
		this.packageName = packageName;
		this.text = text;
		this.title = title;
		this.id = 0; // Для новых записей ID временно равен 0, база данных сама заменит его на правильный
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
