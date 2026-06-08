package com.example.pushcontrol;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import androidx.test.core.app.ApplicationProvider;
import static org.mockito.Mockito.*;

import com.example.pushcontrol.NotificationControl.NotificationCatcherService;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
	private NotificationCatcherService serviceSpy;
	private StatusBarNotification mockSbn;
	private Notification mockNotification;
	private Bundle realExtras;
	private Context targetContext;

	@Before
	public void setUp() {
		// 1. Получаем настоящий тестовый контекст Android
		targetContext = ApplicationProvider.getApplicationContext();

		// 2. Создаем spy-объект сервиса
		NotificationCatcherService realService = new NotificationCatcherService();
		serviceSpy = Mockito.spy(realService);

		// КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Говорим Mockito возвращать реальный контекст
		// вместо null, когда внутри сервиса вызывается getApplicationContext()
		doReturn(targetContext).when(serviceSpy).getApplicationContext();

		// Изолируем отправку broadcast, чтобы он не искал реальную систему
		doNothing().when(serviceSpy).sendBroadcast(any());

		// 3. Создаем структуру заглушек для SBN
		mockSbn = mock(StatusBarNotification.class);
		mockNotification = mock(Notification.class);
		realExtras = new Bundle();

		mockNotification.extras = realExtras;
		when(mockSbn.getNotification()).thenReturn(mockNotification);
	}

	@Test
	public void testOnNotificationPosted_WhenPackageMatches_ShouldProcessAndCallHandle() {
		// Имитируем пуш от Telegram
		when(mockSbn.getPackageName()).thenReturn("org.telegram.messenger");
		realExtras.putCharSequence(Notification.EXTRA_TITLE, "Иван");
		realExtras.putCharSequence(Notification.EXTRA_TEXT, "Привет!");

		// Теперь этот метод не упадет, так как getApplicationContext() вернет targetContext
		serviceSpy.onNotificationPosted(mockSbn);

		// Проверяем, что метод handleNotification вызвался ровно 1 раз
		verify(serviceSpy, times(1)).handleNotification(
				"org.telegram.messenger",
				"Иван",
				"Привет!"
		);
	}
}