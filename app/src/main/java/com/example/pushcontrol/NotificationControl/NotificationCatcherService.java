package com.example.pushcontrol.NotificationControl;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.pushcontrol.DataBaze.DatabaseHelper;
import com.example.pushcontrol.DataBaze.NotificBD;

public class NotificationCatcherService extends NotificationListenerService {
	private static final String TAG = "NotifCatcher";

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (sbn == null) return;

		Context context = getApplicationContext() != null ? getApplicationContext() : this;
		NotificBD push = null;
		// Получаем информацию об уведомлении
		String packageName = sbn.getPackageName();

		// Получаем заголовок и текст если есть
		String title = "";
		String text = "";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
				title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE, "");
				text = sbn.getNotification().extras.getString(Notification.EXTRA_TEXT, "");
				push = new NotificBD(packageName, text, title);

			}
		}

		// Если макс или телега или вотсап
		if ("ru.oneme.app".equals(packageName) || "org.telegram.messenger".equals(packageName)) {
			// Здесь можно производить манипуляции с данными
			try {
				DatabaseHelper dbHelper = new DatabaseHelper(context);
				dbHelper.insertNotification(push);
				// Проверяем накопление истории — выводим всю базу в лог
				dbHelper.logAllNotifications();
			} catch (Exception e) {
				Log.d(TAG, "Не удалось записать данные в SQLite: " + e.getMessage());
			}

		}
		handleNotification(packageName, title, text);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		Log.d(TAG, "Уведомление удалено: " + sbn.getPackageName());
	}

	public void  handleNotification(String packageName, String title, String text) {
		// Например отправить через BroadcastReceiver в мою Activity
		Intent intent = new Intent("NOTIFICATION_RECEIVED");
		intent.putExtra("package", packageName);
		intent.putExtra("titile", title);
		intent.putExtra("text", text);
		sendBroadcast(intent);
	}
}


