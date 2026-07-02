package com.example.pushcontrol.NotificationControl;

import static com.example.pushcontrol.Constans.PreferencesConstants.*;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.pushcontrol.DataBaze.DatabaseHelper;
import com.example.pushcontrol.DataBaze.NotificBD;

public class NotificationCatcherService extends NotificationListenerService {
	private static final String TAG = "NotifCatcher";

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (sbn == null) return;

		Context context = getApplicationContext() != null ? getApplicationContext() : this;
		NotificBD push = null;
		String packageName = sbn.getPackageName();

		String title = "";
		String text = "";
		byte[] imageBytes = null; // Переменная для хранения картинки

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
				Bundle extras = sbn.getNotification().extras;
				inspectBundle(extras, "Notification(" + sbn.getPackageName() + ")");
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
													Log.e(TAG, "Не удалось прочитать фото по URI: " + e.getMessage());
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
															Log.d("MAX_INSPECTOR_SUCCESS", "🎉 Успешно скачали ФОТО из URI! Размер байт: " + imageBytes.length);
															break; // Фото найдено и сохранено, выходим из цикла!
														}
													} catch (Exception e) {
														Log.e("MAX_INSPECTOR_ERROR", "Не удалось прочитать поток по URI: " + e.getMessage());
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
				push = new NotificBD(packageName, text, title, imageBytes);
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
			} catch (Exception e) {
				Log.d(TAG, "Не удалось записать данные в SQLite: " + e.getMessage());
			}
			handleNotification(packageName, title, text);
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
		Log.d(TAG, "Уведомление удалено: " + sbn.getPackageName());
	}

	public void handleNotification(String packageName, String title, String text) {
		Intent intent = new Intent("NOTIFICATION_RECEIVED");
		intent.putExtra("package", packageName);
		intent.putExtra("title", title);
		intent.putExtra("text", text);
		sendBroadcast(intent);
	}
	// Вспомогательный рекурсивный метод для обхода ВСЕХ вложенных Bundle и коллекций
	private void inspectBundle(Bundle bundle, String path) {
		if (bundle == null) return;

		for (String key : bundle.keySet()) {
			Object value = bundle.get(key);
			String fullPath = path + " -> [" + key + "]";

			if (value == null) {
				Log.d("MAX_INSPECTOR", fullPath + " = null");
				continue;
			}

			String typeName = value.getClass().getSimpleName();

			// 1. Если нашли картинку Bitmap в чистом виде
			if (value instanceof Bitmap) {
				Bitmap bmp = (Bitmap) value;
				Log.w("MAX_INSPECTOR", fullPath + " 🔴 НАЙДЕН BITMAP! Размеры: " + bmp.getWidth() + "x" + bmp.getHeight());
			}
			// 2. Если нашли системный объект Иконки (Android 6.0+)
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && value instanceof android.graphics.drawable.Icon) {
				Log.w("MAX_INSPECTOR", fullPath + " 🟡 НАЙДЕН ОБЪЕКТ ICON!");
			}
			// 3. Если нашли ссылку на внутренний медиафайл (Uri)
			else if (value instanceof android.net.Uri) {
				Log.w("MAX_INSPECTOR", fullPath + " 🔵 НАЙДЕН АДРЕС URI: " + value.toString());
			}
			// 4. Если нашли вложенную папку (Bundle), уходим вглубь на разведку
			else if (value instanceof Bundle) {
				Log.d("MAX_INSPECTOR", fullPath + " (Вложенный Bundle):");
				inspectBundle((Bundle) value, fullPath);
			}
			// 5. Если это массив сообщений (MessagingStyle), который используют мессенджеры
			else if (value instanceof Parcelable[]) {
				Log.d("MAX_INSPECTOR", fullPath + " (Массив Parcelable[], проверяем элементы):");
				Parcelable[] array = (Parcelable[]) value;
				for (int i = 0; i < array.length; i++) {
					if (array[i] instanceof Bundle) {
						inspectBundle((Bundle) array[i], fullPath + "[" + i + "]");
					}
				}
			}
			// 6. Любые другие текстовые/числовые данные печатаем обычной строкой
			else {
				Log.d("MAX_INSPECTOR", fullPath + " (Тип: " + typeName + ") = " + value.toString());
			}
		}
	}

}



