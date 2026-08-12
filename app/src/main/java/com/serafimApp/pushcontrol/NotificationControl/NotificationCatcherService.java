package com.serafimApp.pushcontrol.NotificationControl;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.serafimApp.pushcontrol.DataBaze.DatabaseHelper;
import com.serafimApp.pushcontrol.DataBaze.NotificBD;
import com.serafimApp.pushcontrol.MainActivity;
import com.serafimApp.pushcontrol.R;

public class NotificationCatcherService extends NotificationListenerService {
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (sbn == null) return;
		// 2. Жесткий фильтр системного мусора (зарядка, скриншоты и т.д.)

		if (sbn.getPackageName() == null || sbn.getPackageName().equals("com.android.systemui") || sbn.getPackageName().equals("android")) {
			Log.d("SdReguest", "return");
			return;
		}

		Context context = getApplicationContext() != null ? getApplicationContext() : this;
		String packageName = sbn.getPackageName();

		SharedPreferences prefs = context.getSharedPreferences(PREF_IS_NOTIFIGATION_ENBLE, Context.MODE_PRIVATE);
		boolean iaAllowedByUser = prefs.getBoolean(packageName, false);
		if (!iaAllowedByUser) {
			Log.d("SdReguest", "Нет в памяти выходим");
			return;
		}



		NotificBD push = null;

		String title = "";
		String text = "";
		String serverId = "";
		byte[] imageBytes = null; // Переменная для хранения картинки

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
				Bundle extras = sbn.getNotification().extras;
				// Извлекаем Google Message ID
				// Если его нет, берем уникальный ключ Android-уведомления (sbn.getKey())
				if (serverId == null || serverId.isEmpty()) {
					Log.d("SdReguest", sbn.getKey().toString());

					serverId = sbn.getKey();
				}
				// inspectBundle(extras, "Notification(" + sbn.getPackageName() + ")");
				title = extras.getString(Notification.EXTRA_TITLE, "");

				// Используем getCharSequence и приведение к строке, так как некоторые приложения (например, WhatsApp)
				// передают текст в виде сложного SpannableString, из-за чего обычный getString() может вернуть null.
				text = extras.getCharSequence(Notification.EXTRA_TEXT) != null ?
						extras.getCharSequence(Notification.EXTRA_TEXT).toString() : "";
				if (title.equals("Аудиосообщение") && text.equals("Входящий видеозвонок")) return;

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
						if (/*"ru.oneme.app".equals(packageName)*/true) {
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
					}
				}
				push = new NotificBD(packageName, text, title, imageBytes, 0, serverId);
				Log.d("SdReguest", "push = " + push.toString());
			}
		}
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		try {
			if (push.getTitle().isEmpty()) {
				dbHelper.close();
				return;
			}
			// Сюда уйдет объект уже с картинкой внутри
			if (!dbHelper.insertNotification(push)) return;
			handleNotification(push);
			showPushControlSummaryNotification();
			cancelNotification(sbn.getKey());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.close();
		}


		/*if (iaAllowedByUser) {
			cancelNotification(sbn.getKey());
			try {
				DatabaseHelper dbHelper = new DatabaseHelper(context);
				if (push.getTitle().isEmpty()) {
					dbHelper.close();
					return;
				}
				dbHelper.insertNotification(push);
				dbHelper.logAllNotifications();
				dbHelper.close();
				handleNotification(push);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// ИСПРАВЛЕНИЕ: Вызываем пуш с микрозадержкой, чтобы разделить транзакции в системе
			new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					showPushControlSummaryNotification();
				}
			}, 100); // 100 миллисекунд достаточно
		}*/

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

	private void showPushControlSummaryNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// ВАЖНО: поменяли ID на v2, чтобы сбросить старый низкий приоритет в кэше телефона
		String channelId = "push_control_channel_v2";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// ВАЖНО: Поставили IMPORTANCE_DEFAULT вместо IMPORTANCE_LOW
			NotificationChannel channel = new NotificationChannel(
					channelId,
					"PushControl Alerts",
					NotificationManager.IMPORTANCE_DEFAULT
			);
			notificationManager.createNotificationChannel(channel);
		}

		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("PushControl")
				.setContentText("У вас есть непрочитанные уведомления")
				// ВАЖНО: Поставили PRIORITY_DEFAULT вместо PRIORITY_LOW
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
				androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
						!= android.content.pm.PackageManager.PERMISSION_GRANTED) {
			Log.e("PushControl", "Нет разрешения POST_NOTIFICATIONS");
			return;
		}

		notificationManager.notify(999, builder.build());
	}


}