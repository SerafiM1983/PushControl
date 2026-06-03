package com.example.pushcontrol.NotificationControl;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationCatcherService extends NotificationListenerService {
	private static final String TAG = "NotifCatcher";

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		// Получаем информацию об уведомлении
		String packageName = sbn.getPackageName();

		// Получаем заголовок и текст если есть
		String title = "";
		String text = "";

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
				title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE, "");
				text = sbn.getNotification().extras.getString(Notification.EXTRA_TEXT, "");

			}
		}

		// Логирую перехваченное сообщение
		Log.d(TAG, "Package: " + packageName);
		Log.d(TAG, "Title: " + title);
		Log.d(TAG, "Text: " + text);

		// Здесь можно производить манипуляции с данными
		handleNotification(packageName, title, text);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		Log.d(TAG, "Уведомление удалено: " + sbn.getPackageName());
	}

	private void  handleNotification(String packageName, String title, String text) {
		// Например отправить через BroadcastReceiver в мою Activity
		android.content.Intent intent = new Intent("NOTIFICATION_RECEIVED");
		intent.putExtra("package", packageName);
		intent.putExtra("titile", title);
		intent.putExtra("text", text);
		sendBroadcast(intent);
	}
}
