package com.example.pushcontrol.DataBaze;

import androidx.annotation.NonNull;

public class NotificBD {
	private String title;
	private String text;
	private String packageName;
	private byte[] image;
	long id;

	public NotificBD(String packageName, String text, String title, byte[] image, long id) {
		this.packageName = packageName;
		this.text = text;
		this.title = title;
		this.image = image;
		this.id = id;
	}
	public NotificBD(String packageName, String text, String title, byte[] image) {
		this.packageName = packageName;
		this.text = text;
		this.title = title;
		this.image = image;
		this.id = 0;
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

	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}

	@NonNull
	@Override
	public String toString() {
		return new String(
				"title = " + title + "\n"
				+ "text = " + text + "\n"
				+ "packageName = " + packageName + "\n"
				+ "id = " + id + "\n"
				+ "image = " + java.util.Arrays.toString(image)
		);
	}
}
