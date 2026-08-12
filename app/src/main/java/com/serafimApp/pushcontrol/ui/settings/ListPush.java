package com.serafimApp.pushcontrol.ui.settings;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.*; // Импортируем вашу константу имени настроек

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import androidx.core.app.NotificationManagerCompat;

import com.serafimApp.pushcontrol.Dialog.DialogAppsLoads;

import java.util.ArrayList;
import java.util.List;

public class ListPush {

	public List<AppModel> getAppsWithNotifications(Context context, DialogAppsLoads progressDialog) {
		String myOwnPackage = context.getPackageName();
		PackageManager packageManager = context.getPackageManager();

		// Считываем настройки по вашей константе
		SharedPreferences prefs = context.getSharedPreferences(PREF_IS_NOTIFIGATION_ENBLE, Context.MODE_PRIVATE);
		List<AppModel> allowedAppsList = new ArrayList<>();

		// Получаем все установленные приложения в системе
		List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		int i = 0;
		int totalApps = installedApps.size();

		for (ApplicationInfo appInfo : installedApps) {
			i++;
			// Отсекаем системные процессы, берем только пользовательские приложения
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				String appName = appInfo.loadLabel(packageManager).toString();
				String packageName = appInfo.packageName;
				if (packageName.equals(myOwnPackage)) {
					continue; // Переходим к следующему приложению в цикле
				}

				// ИСПРАВЛЕНО: Читаем строго по простому packageName, как в вашем AppsAdapter!
				boolean isNotificationsEnable = prefs.getBoolean(packageName, false);

				// Подгружаем иконку приложения
				Drawable icon = appInfo.loadIcon(packageManager);

				// Передаем привет диалогу: обновляем текст каждые 10 приложений, чтобы UI не зависал
				if (progressDialog != null && (i % 10 == 0 || i == totalApps)) {
					progressDialog.dilog_massage(appName );
				}

				// Добавляем в список. Передаем appInfo.uid третьим параметром для совместимости с вашей моделью
				allowedAppsList.add(new AppModel(appName, packageName, appInfo.uid, icon, isNotificationsEnable));
			}
		}
		return allowedAppsList;
	}
}

