package com.serafimApp.pushcontrol.ui.home;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimApp.pushcontrol.DataBaze.DatabaseHelper;
import com.serafimApp.pushcontrol.DataBaze.NotificBD;
import com.serafimApp.pushcontrol.LogCat;
import com.serafimApp.pushcontrol.R;
import com.serafimApp.pushcontrol.databinding.FragmentHomeBinding;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.MobileAds;
import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.ID_1;

import java.util.List;

public class HomeFragment extends Fragment {
	LogCat logCat = new LogCat(false); // Отключены уведомления
	private boolean isGeneralFeed = false;
	private NotificationsAdapter adapter;
	private List<NotificBD> notificationList;
	private NotificationReceiver notificationReceiver;



	private FragmentHomeBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		HomeViewModel homeViewModel =
				new ViewModelProvider(this).get(HomeViewModel.class);

		MobileAds.initialize(getActivity().getApplicationContext(), () -> {});


		binding = FragmentHomeBinding.inflate(inflater, container, false);

		// Яндекс баннер
		binding.banner.setAdUnitId(ID_1);
		binding.banner.setAdSize(BannerAdSize.stickySize(getActivity().getApplicationContext(), 320));
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.banner.loadAd(adRequest);
		View root = binding.getRoot();

		return root;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Настраиваю менеджер компонента списка сообщений
		binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
		DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

		// Проверяем пришли ли аргументы из боковой панели
		if (getArguments() != null && getArguments().containsKey(SELECTED_PACKAGE)) {
			String pascageName = getArguments().getString(SELECTED_PACKAGE);
			String appNameTitle = getArguments().getString(SELECTED_APP_NAME);

			// УСТАНАВЛИВАЕМ НАЗВАНИЕ ПРОГРАММЫ
			if (requireActivity().getActionBar() != null) {
				requireActivity().getActionBar().setTitle(appNameTitle);
			} else if (((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
				((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(appNameTitle);
			}

			// Загружаем пуши только для кликнутой программы
			notificationList = dbHelper.getNotificationsByPackage(pascageName);
			adapter = new NotificationsAdapter(notificationList, false);
		} else {
			// ЕСЛИ АРГУМЕНТОВ НЕТ — СТАВИМ ЗАГЛОВОК «ОБЩАЯ ЛЕНТА»
			// Если открыли приложение просто так - вернуть стандартный заголовок общей ленты
			if (((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
				((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(
						ContextCompat.getString(getContext(), R.string.general_feed));
			}

			// Если открыли приложение просто так - показать общую ленту
			notificationList = dbHelper.getAllNotifications();
			if (notificationList.size() == 0) {
				notificationList.add(new NotificBD("com.serafimApp.pushcontrol", "Как только у вас появятся сообщения они будут отображены здесь", "Системное сообщение"));
			}
			adapter = new NotificationsAdapter(notificationList, true);
		}

		binding.rvNotifications.setAdapter(adapter);
		// Создаем стандартный вертикальный разделитель
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
				requireContext(),
				LinearLayoutManager.VERTICAL
		);

		// Привязываем разделитель к RecyclerView
		binding.rvNotifications.addItemDecoration(dividerItemDecoration);

		// Настройка свайпа
		ItemTouchHelper.SimpleCallback swipeHandler = new ItemTouchHelper.
				SimpleCallback(0, ItemTouchHelper.LEFT) {
			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
				int position = viewHolder.getAdapterPosition();
				// Берем элемент из адаптера по позиции свайпа
				NotificBD itemToDelete = adapter.getItemAt(position);
				// Удаление из БД по ID
				long idInDatabase = itemToDelete.getId();
				dbHelper.deleteItem(idInDatabase);
				// Удаляю из списка на экране
				adapter.removeItem(position);
			}
			// Рисование корзины

			@Override
			public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
			                        @NonNull RecyclerView.ViewHolder viewHolder, float dX,
			                        float dY, int actionState, boolean isCurrentlyActive) {
				View itemView = viewHolder.itemView;
				int itemHeight = itemView.getBottom() - itemView.getTop();
				// Рисуем красный фон в открывающейся щели
				ColorDrawable background = new ColorDrawable(ContextCompat.getColor(
						getContext().getApplicationContext(), R.color.background));
				background.setBounds(
						itemView.getRight() + (int) dX, itemView.getTop(),
						itemView.getRight(), itemView.getBottom()
				);
				background.draw(c);

				// 2. Рисуем встроенную в Android векторную иконку удаления (белая корзина)
				// Если  есть своя иконка в drawable, замените android.R.drawable.ic_menu_delete на неё

				Drawable deleteIcon = ContextCompat.getDrawable(requireContext(),
						android.R.drawable.ic_menu_delete);

				if (deleteIcon != null) {
					int intrinsicWidth = deleteIcon.getIntrinsicWidth();
					int intrinsicHeight = deleteIcon.getIntrinsicHeight();

					// Вычисляем размеры и отступы, чтобы иконка была ровно по центру красной зоны
					int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
					int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
					int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
					int deleteIconRight = itemView.getRight() - deleteIconMargin;
					int deleteIconBottom = deleteIconTop + intrinsicHeight;

					// Если пользователь сдвинул элемент достаточно далеко — показываем иконку
					if (dX < -deleteIconMargin) {
						deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);

						// Делаем иконку белой (опционально, если системная иконка серая)
						deleteIcon.setTint(ContextCompat.getColor(getContext().getApplicationContext(),
								R.color.white));

						deleteIcon.draw(c);
					}
				}
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		};

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
		itemTouchHelper.attachToRecyclerView(binding.rvNotifications);

		requireActivity().addMenuProvider(new MenuProvider() {
			@Override
			public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
				// 1. Очищаем меню, чтобы пункты не дублировались при переходах
				menu.clear();

				// 2. Наполняем меню из XML-файла
				menuInflater.inflate(R.menu.main, menu);

				// 3. Строгая проверка: открыто конкретное приложение или общая лента

				if (getArguments() != null && getArguments().containsKey(SELECTED_PACKAGE)) {
					String currentPkg = getArguments().getString(SELECTED_PACKAGE);
					if (currentPkg != null && !currentPkg.trim().isEmpty()) {
						isGeneralFeed = true;
					}
				}

				// 4. Управляем кнопкой "Очистить чат"
				MenuItem clearChatEntry = menu.findItem(R.id.action_clear_chat);
				if (clearChatEntry != null) {
					clearChatEntry.setVisible(isGeneralFeed); // Покажет ТОЛЬКО в чате программы
				}

				// Управление кнопкой очистить все
				boolean isSpecificAll = true;
				if (getArguments() != null && getArguments().containsKey(SELECTED_PACKAGE)) {
					String currentPkg = getArguments().getString(SELECTED_PACKAGE);
					if (currentPkg != null && !currentPkg.trim().isEmpty()) {
						isSpecificAll = false;
					}
				}
				MenuItem clearChatEntryAll = menu.findItem(R.id.action_clear_all);
				if (clearChatEntryAll != null) {
					clearChatEntryAll.setVisible(isSpecificAll); // Покажет ТОЛЬКО в чате программы
				}

				// Пункты "Настройки приложений" (nav_settings) и "Очистить все" (action_clear_all)
				// останутся видимыми везде по умолчанию.
			}

			@Override
			public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
				int id = menuItem.getItemId();

				if (id == R.id.action_clear_chat) {
					if (getArguments() != null) {
						String pascageName = getArguments().getString(SELECTED_PACKAGE);
						dbHelper.clearNotificationsByPackage(pascageName);

						notificationList.clear();
						adapter.notifyDataSetChanged();
					}
					return true;

				} else if (id == R.id.action_clear_all) {
					dbHelper.clearAllNotifications();
					notificationList.clear();
					adapter.notifyDataSetChanged();
					return true;
				} else if (id == R.id.nav_settings) {
					// ЗДЕСЬ ДОБАВЬТЕ КОД ДЛЯ ПЕРЕХОДА В НАСТРОЙКИ
					// Пример: Navigation.findNavController(requireView()).navigate(R.id.action_to_settings);
					return true;
				}

				return false;
			}
		}, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
	}

	@Override
	public void onStart() {
		super.onStart();
		// 1. Создаем объект приёмника, если он еще не создан
		if (notificationReceiver == null) {
			notificationReceiver = new NotificationReceiver();
		}
		// 2. Настраиваем волну (Action), которую будем слушать
		IntentFilter filter = new IntentFilter("NOTIFICATION_RECEIVED");

		// 3. Регистрируем приёмник в контексте Activity
		if (getActivity() != null) {
			getActivity().registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		// Обязательно отключаем приёмник при уходе с экрана (защита от утечек памяти)
		if (getActivity() != null && notificationReceiver != null) {
			getActivity().unregisterReceiver(notificationReceiver);
		}
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	// Внутренний класс для перехвата сообщений из NotificationCatcherService в реальном времени
	private class NotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && "NOTIFICATION_RECEIVED".equals(intent.getAction())) {

				// ЖЕЛЕЗНОЕ УСЛОВИЕ: Если это НЕ общая лента (пользователь сидит внутри чата),
				// мы просто выходим из метода и ничего не добавляем на экран динамически.

				if (isGeneralFeed) {
					return;
				}

				String packageName = intent.getStringExtra("package");
				String title = intent.getStringExtra("title");
				String text = intent.getStringExtra("text");


				// Создаем объект модели для вывода на экран
				NotificBD newNotif = new NotificBD(packageName, text, title);

				// Безопасно обновляем RecyclerView в главном потоке
				if (getActivity() != null) {
					getActivity().runOnUiThread(() -> {
						// ИСПРАВЛЕНО: Используем правильные глобальные имена переменных класса HomeFragment
						if (notificationList != null && adapter != null && binding != null) {

							// Добавляем элемент в самый верх Общей ленты
							notificationList.add(0, newNotif);

							// Запускаем красивую анимацию появления новой карточки
							adapter.notifyItemInserted(0);

							// ИСПРАВЛЕНО: Скроллим правильный RecyclerView (rvNotifications) к самому первому элементу
							binding.rvNotifications.scrollToPosition(0);
						}
					});
				}
			}
		}
	}
}