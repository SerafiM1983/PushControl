package com.example.pushcontrol.ui.settings;

import static com.example.pushcontrol.Constans.PreferencesConstants.*;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class ListPush {

	public List<AppModel> getAppsWithNotifications(Context context) {
		PackageManager packageManager = context.getPackageManager();
		// Получаем список всех установленныъ приложений
		SharedPreferences prefs = context
				.getSharedPreferences(prefIsNotifigationEnble, Context.MODE_PRIVATE);

		List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		List<AppModel> allowedAppsList = new ArrayList<>();

		for (ApplicationInfo appInfo : installedApps) {
			// Исключаем системные процессы, проверяем только пользовательские
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				String appName = appInfo.loadLabel(packageManager).toString();
				String packageName = appInfo.packageName;

				// 1. Получаем реальный системный статус уведомлений на данный момент
				boolean systemEnable = NotificationManagerCompat.from(context).areNotificationsEnabled();

				// 2. Проверяем, сохранял ли пользователь свой выбор ранее.
				// Если настроек нет, по умолчанию возвращаем системное значение (systemEnabled)
				boolean isNotificationsEnable = prefs.getBoolean(packageName, systemEnable);

				allowedAppsList.add(new AppModel(appName, packageName, appInfo.loadIcon(packageManager), isNotificationsEnable));
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
