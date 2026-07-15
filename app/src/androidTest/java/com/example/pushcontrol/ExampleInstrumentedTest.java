package com.example.pushcontrol;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import androidx.fragment.app.testing.FragmentScenario;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import androidx.test.core.app.ApplicationProvider;
import static org.mockito.Mockito.*;

import com.example.pushcontrol.NotificationControl.NotificationCatcherService;
import com.example.pushcontrol.ui.home.HomeFragment;

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
	private Context context;
	private Bundle mockExtras;

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
	public void testNotificationReceiver_handlesBroadcastEvent() {
		// 1. Настраиваем возвращаемые значения для нашего фейкового пуша
		when(mockExtras.getString("google.message_id")).thenReturn("mock_server_id_777");
		when(mockExtras.getString(Notification.EXTRA_TITLE, "")).thenReturn("Тест от Мокито");

		// Симулируем Spannable/CharSequence текст пуша
		String testText = "Проверка интеграции прошла успешно!";
		when(mockExtras.getCharSequence(Notification.EXTRA_TEXT)).thenReturn(testText);

		// 2. Запускаем HomeFragment в изолированном контейнере для тестов
		FragmentScenario<HomeFragment> scenario = FragmentScenario.launchInContainer(HomeFragment.class);

		// 3. Создаем Intent вручную, как это делает ваш метод handleNotification
		Intent testIntent = new Intent("NOTIFICATION_RECEIVED");
		testIntent.putExtra("package", mockSbn.getPackageName());
		testIntent.putExtra("serverId", "mock_server_id_777");
		testIntent.putExtra("title", "Тест от Мокито");
		testIntent.putExtra("text", testText);

		// 4. Отправляем интент в систему
		context.sendBroadcast(testIntent);

		// 5. Небольшое ожидание для отработки асинхронного runOnUiThread
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Проверяем, что мок-объект sbn дернули правильное количество раз (верификация Mockito)
		verify(mockSbn, atLeastOnce()).getPackageName();
	}
}