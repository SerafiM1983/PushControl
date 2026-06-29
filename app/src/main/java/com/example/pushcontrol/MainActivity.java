package com.example.pushcontrol;

import static com.example.pushcontrol.Constans.PreferencesConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;

import com.example.pushcontrol.ui.home.HomeFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pushcontrol.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final int ENABLE_NOTIFICATION_LISTENER = 1;
	private GestureDetector globalToolbarDetector;
	private AppBarConfiguration mAppBarConfiguration;
	private ActivityMainBinding binding;
	private NotificationReceiver notificationReceiver;
	private boolean isReceiverRegistered = false; // Флаг отслеживания регистрации


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Отключает принудительное перекрашивание иконок в один цвет
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		// Проверка доступа к уведомлениям перенесена ниже метода setContentView
		checkNotificationListenerPermission();

		binding.navView.setItemIconTintList(null);
		setSupportActionBar(binding.appBarMain.toolbar);

		DrawerLayout drawer = binding.drawerLayout;

		// Добавляем слушатель состояния шторки
		drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				// Обновляем список приложений каждый раз, когда пользователь открывает шторку
				updateDrawerWithSelectedApps();
			}
		});

		NavigationView navigationView = binding.navView;



		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
				R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
				.setOpenableLayout(drawer)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);


		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle("Общая Лента");
		}

		navigationView.setNavigationItemSelectedListener(item -> {
			Intent intent = item.getIntent();
			// Проверяем что интент существует и содержит имя пакета приложения
			if (intent != null && intent.hasExtra(packageName)) {
				String clickedPackage = intent.getStringExtra(packageName);
				String appNameTitle = intent.getStringExtra(selectedAppName);

				// Упаковка имя пакета в Bundle для передачи во фрагмент
				Bundle bundle = new Bundle();
				bundle.putString(selectedPackage, clickedPackage);
				bundle.putString(selectedAppName, appNameTitle);

				// НАСТРОЙКА: Очищаем стек, чтобы новые экраны заменяли старые, а не наслаивались друг на друга
				androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
						.setPopUpTo(R.id.nav_home, true) // Принудительно вычищаем прошлый nav_home из стека
						.setLaunchSingleTop(true)
						.build();

				// Открываем фрагмент через NavController и передаем ему bundle.
				// R.id.nav_home — это ID вашего HomeFragment в nav_graph.xml.
				navController.navigate(R.id.nav_home, bundle, navOptions);

				// Закрываем боковую шторку меню
				drawer.closeDrawers();
				return true;
			}
			// Для стандартных пунктов меню (если они остались) используем стандартную навигацию
			boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
			if (handled) drawer.closeDrawers();
			return handled;
		});

		// Инициализируем приемник бродкастов от нашего сервиса
		notificationReceiver = new NotificationReceiver();

			// ПРАВИЛЬНЫЙ ОБРАБОТЧИК КНОПКИ НАЗАД ДЛЯ ВАШЕГО ПРОЕКТА
		// ПРАВИЛЬНЫЙ ОБРАБОТЧИК КНОПКИ НАЗАД ДЛЯ JETPACK NAVIGATION
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				// Проверяем аргументы текущего экрана прямо через NavController
				Bundle currentArgs = navController.getCurrentBackStackEntry() != null ?
						navController.getCurrentBackStackEntry().getArguments() : null;

				// Если аргументы есть и там есть ключ выбранного пакета — значит мы в чате программы
				if (currentArgs != null && currentArgs.containsKey(selectedPackage)) {

					// Настройка навигации для возврата: полностью чистим историю
					androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
							.setPopUpTo(R.id.nav_home, true)
							.build();

					// Открываем nav_home БЕЗ параметров (это вернет нас на "Общую Ленту")
					navController.navigate(R.id.nav_home, null, navOptions);

					// После возврата на Главную принудительно возвращаем заголовок
					if (getSupportActionBar() != null) {
						getSupportActionBar().setTitle("Общая Лента");
					}
				} else {
					// Если мы уже на Общей ленте (аргументов нет) — закрываем приложение
					setEnabled(false); // Отключаем колбэк, чтобы избежать бесконечного цикла
					getOnBackPressedDispatcher().onBackPressed(); // Закрываем Activity
				}
			}
		});
	}

	private void updateDrawerWithSelectedApps() {
		if (binding == null || binding.navView == null) return;

		NavigationView navigationView = binding.navView;
		Menu menu = navigationView.getMenu();

		// Очищаем старую группу перед добавлением, чтобы пункты не дублировались
		menu.removeGroup(R.id.dynamic_apps_croup);

		PackageManager pm = getPackageManager();

		// Используем ваше имя файла настроек из PreferencesConstants
		SharedPreferences prefs = getSharedPreferences(prefIsNotifigationEnble, Context.MODE_PRIVATE);

		// Получаем список только установленных пользователем приложений
		List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		int itemId = 1000;

		for (ApplicationInfo appInfo : installedApps) {
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

				// Читаем статус: ключ — строго packageName приложения
				boolean isSelected = prefs.getBoolean(appInfo.packageName, false);

				if (isSelected) {
					String appName = appInfo.loadLabel(pm).toString();
					try {
						Drawable icon = pm.getApplicationIcon(appInfo.packageName);

						// Добавляем пункт в шторку
						MenuItem item = menu.add(R.id.dynamic_apps_croup, itemId, Menu.NONE, appName);
						item.setIcon(icon);

						// ПередаемpackageName через вашу константу
						Intent intent = new Intent();
						intent.putExtra(packageName, appInfo.packageName);
						intent.putExtra(selectedAppName, appName);
						item.setIntent(intent);
						Log.d("TITLE", "Titlt = " + appName);

						itemId++;
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	@Override
	protected void onResume() {
		super.onResume();

		// Проверяем, что ресивер создан и ЕЩЕ НЕ зарегистрирован
		if (notificationReceiver != null && !isReceiverRegistered) {
			IntentFilter filter = new IntentFilter("NOTIFICATION_RECEIVED");

			// Регистрируем приемник
			registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
			isReceiverRegistered = true; // Поднимаем флаг
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Отписываемся ТОЛЬКО если флаг равен true (приемник действительно зарегистрирован)
		if (notificationReceiver != null && isReceiverRegistered) {
			try {
				unregisterReceiver(notificationReceiver);
			} catch (IllegalArgumentException e) {
				// На всякий случай гасим непредвиденную ошибку системы
				Log.e("MainActivity", "Ошибка при отписке ресивера: " + e.getMessage());
			}
			isReceiverRegistered = false; // Сбрасываем флаг
		}
	}


	private void checkNotificationListenerPermission() {
		if (!isNotificationListenerEnabled()) {
			// Показываем пользователю всплывающее окно (Snackbar) с кнопкой-переходом в настройки
			Snackbar.make(binding.getRoot(), "Приложению необходим доступ к уведомлениям", Snackbar.LENGTH_INDEFINITE)
					.setAction("Включить", view -> {
						Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
						startActivity(intent);
					}).show();
		}
	}

	private boolean isNotificationListenerEnabled() {
		String enabledListeners = Settings.Secure.getString(
				getContentResolver(),
				"enabled_notification_listeners"
		);
		String packageName = getPackageName();
		return enabledListeners!= null && enabledListeners.contains(packageName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

		// Пытаемся автоматически переключить экран по совпадению ID.
		// Если это стандартный пункт навигации, NavigationUI все сделает сам.
		if (NavigationUI.onNavDestinationSelected(item, navController)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}


	// Внутренний класс для перехвата сообщений из NotificationCatcherService в реальном времени
	private class NotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && "NOTIFICATION_RECEIVED".equals(intent.getAction())) {
				String packageName = intent.getStringExtra("package");
				String title = intent.getStringExtra("titile"); // учитываем вашу опечатку "titile"
				String text = intent.getStringExtra("text");

				// Сюда будут попадать пуши ТОЛЬКО от выбранных пользователем приложений
				// Теперь вы можете динамически обновлять UI (например, добавлять элемент в список на главном экране)
			}
		}
	}
}