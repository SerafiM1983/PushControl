package com.example.pushcontrol.NotificationControl;

import static com.example.pushcontrol.Constans.PreferencesConstants.*;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationManagerCompat;

import com.example.pushcontrol.DataBaze.DatabaseHelper;
import com.example.pushcontrol.DataBaze.NotificBD;

public class NotificationCatcherService extends NotificationListenerService {
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (sbn == null) return;
		// 2. Жесткий фильтр системного мусора (зарядка, скриншоты и т.д.)
		if (packageName == null || packageName.equals("com.android.systemui") || packageName.equals("android")) {
			return;
		}

		Context context = getApplicationContext() != null ? getApplicationContext() : this;
		NotificBD push = null;
		String packageName = sbn.getPackageName();

		String title = "";
		String text = "";
		String serverId = "";
		byte[] imageBytes = null; // Переменная для хранения картинки

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
				Bundle extras = sbn.getNotification().extras;
				// Извлекаем Google Message ID
				serverId = extras.getString("google.message_id");
				// Если его нет, берем уникальный ключ Android-уведомления (sbn.getKey())
				if (serverId == null || serverId.isEmpty()) {
					serverId = sbn.getKey();
				}
				// inspectBundle(extras, "Notification(" + sbn.getPackageName() + ")");
				title = extras.getString(Notification.EXTRA_TITLE, "");

				// Используем getCharSequence и приведение к строке, так как некоторые приложения (например, WhatsApp)
				// передают текст в виде сложного SpannableString, из-за чего обычный getString() может вернуть null.
				text = extras.getCharSequence(Notification.EXTRA_TEXT) != null ?
						extras.getCharSequence(Notification.EXTRA_TEXT).toString() : "";

				// --- НАЧАЛО ПЕРЕХВАТА РЕАЛЬНОЙ ФОТОГРАФИИ ---
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
						Bundle extrasBuilde = sbn.getNotification().extras;

						//  СПЕЦИАЛЬНО ДЛЯ TELEGRAM, WHATSAPP И VIBER (MessagingStyle)
						if (imageBytes == null && extrasBuilde.containsKey(Notification.EXTRA_MESSAGES)) {
							Parcelable[] messages = (Parcelable[]) extrasBuilde.get(Notification.EXTRA_MESSAGES);
							if (messages != null && messages.length > 0) {
								// Берем самое последнее сообщение из списка (текущее пришедшее фото)
								Parcelable lastMessage = messages[messages.length - 1];
								if (lastMessage instanceof Bundle) {
									Bundle msgBundle = (Bundle) lastMessage;

									// Проверяем тип данных. Если это картинка (image/), достаем её URI или Data
									if (msgBundle.containsKey("dataMimeType") &&
											msgBundle.getString("dataMimeType", "").startsWith("image/")) {

										// Проверяем наличие прямого Bitmap внутри вложения
										if (msgBundle.containsKey("dataUri")) {
											android.net.Uri dataUri = (android.net.Uri) msgBundle.get("dataUri");
											if (dataUri != null) {
												try {
													// Читаем картинку из URI контент-провайдера мессенджера
													android.content.ContentResolver cr = context.getContentResolver();
													java.io.InputStream is = cr.openInputStream(dataUri);
													Bitmap bitmap = BitmapFactory.decodeStream(is);
													if (bitmap != null) {
														imageBytes = bitmapToByteArray(bitmap);
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
								}
							}
						}

						// Проверяем, что пуш пришел именно от МАКС
						if ("ru.oneme.app".equals(packageName)) {
							// Сканируем массив android.messages (как показал ваш лог)
							if (extras.containsKey(Notification.EXTRA_MESSAGES)) {
								Parcelable[] messages = (Parcelable[]) extras.get(Notification.EXTRA_MESSAGES);
								if (messages != null && messages.length > 0) {

									// МАКС дублирует вложения в цепочке сообщений.
									// Перебираем массив с конца к началу, чтобы найти САМОЕ ПОСЛЕДНЕЕ прикрепленное фото
									for (int i = messages.length - 1; i >= 0; i--) {
										if (messages[i] instanceof Bundle) {
											Bundle msgBundle = (Bundle) messages[i];

											// Проверяем тип, как в логе: [type] = image/*
											String type = msgBundle.getString("type", "");
											if (type != null && type.startsWith("image/")) {

												// Извлекаем URI: [uri]
												Object uriObj = msgBundle.get("uri");
												if (uriObj instanceof android.net.Uri) {
													android.net.Uri dataUri = (android.net.Uri) uriObj;

													// БЕЗОПАСНОЕ ЧТЕНИЕ: Используем контекст самого сервиса уведомлений,
													// чтобы обойти SecurityException ограничения контент-провайдера МАКС
													try (java.io.InputStream is = getContentResolver().openInputStream(dataUri)) {
														Bitmap bitmap = BitmapFactory.decodeStream(is);
														if (bitmap != null) {
															imageBytes = bitmapToByteArray(bitmap);
															break; // Фото найдено и сохранено, выходим из цикла!
														}
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											}
										}
									}
								}
							}
						}
						else {
							// Код для остальных приложений (Telegram, WhatsApp и т.д.)
							if (extras.containsKey(Notification.EXTRA_PICTURE)) {
								Bitmap bitmap = (Bitmap) extras.get(Notification.EXTRA_PICTURE);
								if (bitmap != null) {
									imageBytes = bitmapToByteArray(bitmap);
								}
							}
						}
					}
				}
				push = new NotificBD(packageName, text, title, imageBytes, 0, serverId);
			}
		}

		SharedPreferences prefs = context.getSharedPreferences(prefIsNotifigationEnble, Context.MODE_PRIVATE);
		boolean systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
		boolean iaAllowedByUser = prefs.getBoolean(packageName, systemEnabled);

		if (iaAllowedByUser) {
			try {
				DatabaseHelper dbHelper = new DatabaseHelper(context);
				if (push.getTitle().isEmpty()) {
					dbHelper.close();
					return;
				}
				dbHelper.insertNotification(push); // Сюда уйдет объект уже с картинкой внутри
				dbHelper.logAllNotifications();
				dbHelper.close();
				handleNotification(push);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Вспомогательный метод для перевода картинок в байты перед сохранением в SQLite
	private byte[] bitmapToByteArray(Bitmap bitmap) {
		if (bitmap == null) return null;
		java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
		// Сжимаем в формат JPEG с качеством 75%, чтобы картинка весила немного и не тормозила БД
		bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
		return stream.toByteArray();
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
	}

	public void handleNotification(NotificBD push) {
		Intent intent = new Intent("NOTIFICATION_RECEIVED");
		intent.setPackage(getPackageName());
		intent.putExtra("package", push.getPackageName());
		intent.putExtra("title", push.getTitle());
		intent.putExtra("text", push.getText());
		sendBroadcast(intent);
	}
}