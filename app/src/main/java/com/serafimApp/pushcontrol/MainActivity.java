package com.serafimApp.pushcontrol;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.serafimApp.pushcontrol.Dialog.DialogPermission;
import com.serafimApp.pushcontrol.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	LogCat logCat = new LogCat(false); // Отключен лог
	private AppBarConfiguration mAppBarConfiguration;
	private ActivityMainBinding binding;
	private NotificationReceiver notificationReceiver;
	private boolean isReceiverRegistered = false; // Флаг отслеживания регистрации
	private TextView tv1, tv2;
	private DrawerLayout drawer;
	private NavController navController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Отключает принудительное перекрашивание иконок в один цвет
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.navView.setItemIconTintList(null);
		setSupportActionBar(binding.appBarMain.toolbar);

		drawer = binding.drawerLayout;

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
				R.id.nav_home)
				.setOpenableLayout(drawer)
				.build();
		navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);


		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(ContextCompat.getString(getApplicationContext(), R.string.general_feed));
		}

		navigationView.setNavigationItemSelectedListener(item -> {
			Intent intent = item.getIntent();
			// Проверяем что интент существует и содержит имя пакета приложения
			if (intent != null && intent.hasExtra(PACKAGE_NAME)) {
				String clickedPackage = intent.getStringExtra(PACKAGE_NAME);
				String appNameTitle = intent.getStringExtra(SELECTED_APP_NAME);

				// Упаковка имя пакета в Bundle для передачи во фрагмент
				Bundle bundle = new Bundle();
				bundle.putString(SELECTED_PACKAGE, clickedPackage);
				bundle.putString(SELECTED_APP_NAME, appNameTitle);

				// НАСТРОЙКА: Очищаем стек, чтобы новые экраны заменяли старые, а не наслаивались друг на друга
				androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
						.setPopUpTo(R.id.nav_home, true) // Принудительно вычищаем прошлый nav_home из стека
						.setLaunchSingleTop(true)
						.build();

				// Открываем фрагмент через NavController и передаем ему bundle.
				// R.id.nav_home — это ID HomeFragment в nav_graph.xml.
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

		// ПРАВИЛЬНЫЙ ОБРАБОТЧИК КНОПКИ НАЗАД ДЛЯ JETPACK NAVIGATION
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				// Проверяем аргументы текущего экрана прямо через NavController
				Bundle currentArgs = navController.getCurrentBackStackEntry() != null ?
						navController.getCurrentBackStackEntry().getArguments() : null;

				// Если аргументы есть и там есть ключ выбранного пакета — значит мы в чате программы
				if (currentArgs != null && currentArgs.containsKey(SELECTED_PACKAGE)) {

					// Настройка навигации для возврата: полностью чистим историю
					androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
							.setPopUpTo(R.id.nav_home, true)
							.build();

					// Открываем nav_home БЕЗ параметров (это вернет нас на "Общую Ленту")
					navController.navigate(R.id.nav_home, null, navOptions);

					// После возврата на Главную принудительно возвращаем заголовок
					if (getSupportActionBar() != null) {
						getSupportActionBar().setTitle(ContextCompat.getString(getApplicationContext(), R.string.general_feed));
					}
				} else {
					// Если мы уже на Общей ленте (аргументов нет) — закрываем приложение
					setEnabled(false); // Отключаем колбэк, чтобы избежать бесконечного цикла
					getOnBackPressedDispatcher().onBackPressed(); // Закрываем Activity
				}
			}
		});
		tv1 = binding.navView.findViewById(R.id.tv_about_the_apps);
		tv1.setOnClickListener(this);


	}
	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.tv_about_the_apps) {
			// Закрываем боковую шторку меню
			navController.navigate(R.id.nav_bout_app);
			drawer.closeDrawers();
		}
	}

	private void updateDrawerWithSelectedApps() {

		if (binding == null) return;

		NavigationView navigationView = binding.navView;
		Menu menu = navigationView.getMenu();

		// Очищаем старую группу перед добавлением, чтобы пункты не дублировались
		menu.removeGroup(R.id.dynamic_apps_croup);

		PackageManager pm = getPackageManager();

		// Используем имя файла настроек из PreferencesConstants
		SharedPreferences prefs = getSharedPreferences(PREF_IS_NOTIFIGATION_ENBLE, Context.MODE_PRIVATE);

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

						// Передаем packageName через константу
						Intent intent = new Intent();
						intent.putExtra(PACKAGE_NAME, appInfo.packageName);
						intent.putExtra(SELECTED_APP_NAME, appName);
						item.setIntent(intent);
						itemId++;
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// Если нет итемов в панели шторки
		if (itemId == 1000) {
			String gmailPackage = "com.google.android.gm";
			try {
				ApplicationInfo gmailInfo = pm.getApplicationInfo(gmailPackage, 0);
				String appName = gmailInfo.loadLabel(pm).toString();
				Drawable icon = pm.getApplicationIcon(gmailPackage);

				MenuItem item = menu.add(R.id.dynamic_apps_croup, itemId, Menu.NONE, appName);
				item.setIcon(icon);

				Intent intent = new Intent();
				intent.putExtra(PACKAGE_NAME, gmailPackage);
				intent.putExtra(SELECTED_APP_NAME, appName);
				item.setIntent(intent);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
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
				logCat.logShow(EXEPTION, e.getMessage());
			}
			isReceiverRegistered = false; // Сбрасываем флаг
		}
	}

	private void checkNotificationListenerPermission() {
		if (!isNotificationListenerEnabled()) {
			// 1. Создаем экземпляр класса диалога
			DialogPermission dialog1 = new DialogPermission();

            // 2. Показываем его, передавая getSupportFragmentManager()
			dialog1.dialogNotificationListenerPermission(getSupportFragmentManager(), new DialogPermission.OnDialogActionListener() {
				@Override
				public void onActionClick(int buttonId) {
					if (buttonId == R.id.app_permission_ok) {
						Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
						startActivity(intent);
					} else if (buttonId == R.id.app_permission_off) {
						// Проверяем разрешение
						if (!isNotificationListenerEnabled()) {
							// Если не получилось идем в О приложении
							DialogPermission dialog2 = new DialogPermission();
							dialog2.dialogPostNotificationPermission(getSupportFragmentManager(), new DialogPermission.OnDialogActionListener() {
								@Override
								public void onActionClick(int buttonId) {
									if (buttonId == R.id.app_permission_ok){
										Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                       // Формируем уникальный путь (URI) конкретно для вашего Package Name
										Uri uri = Uri.fromParts("package", getPackageName(), null);
										intent.setData(uri);

                                       // Флаг NEW_TASK гарантирует стабильный запуск интента из фрагментов и диалогов
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(intent);

									}
								}
							});

						} else if (buttonId == R.id.app_permission_off) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
								if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
										!= PackageManager.PERMISSION_GRANTED) {

									// Запрашиваем разрешение на уведомления
									ActivityCompat.requestPermissions(MainActivity.this,
											new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
								}
							}
						}
					}
				}
			});
		} else if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
				!= PackageManager.PERMISSION_GRANTED) {
			// Запрашиваем разрешение на уведомления
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
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
				String title = intent.getStringExtra("title");
				String text = intent.getStringExtra("text");
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Проверка доступа к уведомлениям
		checkNotificationListenerPermission();
	}
}