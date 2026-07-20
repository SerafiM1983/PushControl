package com.serafimApp.pushcontrol.ui.settings;

import android.graphics.drawable.Drawable;

public class AppModel {
	private String appName;
	private String packageName;
	private int uid; // Добавили поле UID для поддержки контейнеров Knox
	private Drawable appIcon;
	private boolean isNotificationEnable;

	// Обновленный конструктор: теперь принимает uid пятым параметром
	public AppModel(String appName, String packageName, int uid, Drawable appIcon, boolean isNotificationEnable) {
		this.appName = appName;
		this.packageName = packageName;
		this.uid = uid;
		this.appIcon = appIcon;
		this.isNotificationEnable = isNotificationEnable;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public boolean isNotificationEnable() {
		return isNotificationEnable;
	}

	public void setNotificationEnable(boolean notificationEnable) {
		isNotificationEnable = notificationEnable;
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

