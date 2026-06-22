package com.example.pushcontrol.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class ListPush {

	public List<String> getAppsWithNotifications(Context context) {
		PackageManager packageManager = context.getPackageManager();
		// Получаем список всех установленныъ приложений
		List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		List<String> allowedAppsList = new ArrayList<>();

		for (ApplicationInfo appInfo : installedApps) {
			// Исключаем системные процессы, проверяем только пользовательские
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				// оверяем включены ли уведомления для конкретного package
				NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
				boolean areNotificationsEnable = notificationManager.areNotificationsEnabled();

				// Для проверки чужих приложений на Android 13+ используется проверка через
				// AppOps или PackageManager
				if (isNotificationPermissionGrantedApp(context, appInfo.packageName)) {
					String appName = appInfo.loadLabel(packageManager).toString();
					allowedAppsList.add(appName + " (" + appInfo.packageName + ")");

				}
			}
		}
		return  allowedAppsList;
	}

	// помогательный метод для проверки разрешения для конкретного пакета
	private boolean isNotificationPermissionGrantedApp(Context context, String packageName) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				// Для Android 13+ проверяем стиатус конкретного разрешения POST_NOTIFICATIONS
				PackageManager pm = context.getPackageManager();
				int hasPermissio = pm.checkPermission(Manifest.permission.POST_NOTIFICATIONS, packageName);
				return hasPermissio == PackageManager.PERMISSION_GRANTED;
			} else {
				// Для старых версий Android по умолчанию разрешено, если пользователь не отключил вручную
				return NotificationManagerCompat.from(context).areNotificationsEnabled();
			}
		} catch (Exception e) {
			// Добавить обработку
			return false;
		}

	}
}
