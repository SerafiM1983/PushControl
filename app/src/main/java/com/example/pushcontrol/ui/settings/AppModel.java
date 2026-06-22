package com.example.pushcontrol.ui.settings;

import android.graphics.drawable.Drawable;

public class AppModel {
	private String appName;
	private String packageName;
	private Drawable appIcon;
	boolean isNotificationEnable;

	public boolean isNotificationEnable() {
		return isNotificationEnable;
	}

	public void setNotificationEnable(boolean notificationEnable) {
		isNotificationEnable = notificationEnable;
	}

	public AppModel(String appName, String packageName, Drawable appIcon) {
		this.appName = appName;
		this.packageName = packageName;
		this.appIcon = appIcon;
		isNotificationEnable = false;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Drawable getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}
}
