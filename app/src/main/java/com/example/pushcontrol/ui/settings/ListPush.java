package com.example.pushcontrol.ui.settings;

import static com.example.pushcontrol.Constans.PreferencesConstants.*; // Импортируем вашу константу имени настроек

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import androidx.core.app.NotificationManagerCompat;
import com.example.pushcontrol.ui.settings.AppModel;
import java.util.ArrayList;
import java.util.List;

public class ListPush {

	public List<AppModel> getAppsWithNotifications(Context context) {
		PackageManager packageManager = context.getPackageManager();

		// Считываем настройки по вашей константе
		SharedPreferences prefs = context.getSharedPreferences(prefIsNotifigationEnble, Context.MODE_PRIVATE);
		List<AppModel> allowedAppsList = new ArrayList<>();

		// Получаем все установленные приложения в системе
		List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

		for (ApplicationInfo appInfo : installedApps) {
			// Отсекаем системные процессы, берем только пользовательские приложения
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				String appName = appInfo.loadLabel(packageManager).toString();
				String packageName = appInfo.packageName;

				// Получаем базовый статус уведомлений
				boolean systemEnable = NotificationManagerCompat.from(context).areNotificationsEnabled();

				// ИСПРАВЛЕНО: Читаем строго по простому packageName, как в вашем AppsAdapter!
				boolean isNotificationsEnable = prefs.getBoolean(packageName, systemEnable);

				// Подгружаем иконку приложения
				Drawable icon = appInfo.loadIcon(packageManager);

				// Добавляем в список. Передаем appInfo.uid третьим параметром для совместимости с вашей моделью
				allowedAppsList.add(new AppModel(appName, packageName, appInfo.uid, icon, isNotificationsEnable));
			}
		}
		return allowedAppsList;
	}
}

