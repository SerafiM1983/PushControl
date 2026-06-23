package com.example.pushcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pushcontrol.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
	private static final int ENABLE_NOTIFICATION_LISTENER = 1;

	private AppBarConfiguration mAppBarConfiguration;
	private ActivityMainBinding binding;
	private NotificationReceiver notificationReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Проверка доступа к уведомлениям перенесена ниже метода setContentView
		checkNotificationListenerPermission();

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.appBarMain.toolbar);
		binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null)
						.setAnchorView(R.id.fab).show();
			}
		});
		DrawerLayout drawer = binding.drawerLayout;
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

		// Инициализируем приемник бродкастов от нашего сервиса
		notificationReceiver = new NotificationReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Обязательно отписываемся от приемника, чтобы избежать утечек памяти
		if (notificationReceiver != null) {
			unregisterReceiver(notificationReceiver);
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