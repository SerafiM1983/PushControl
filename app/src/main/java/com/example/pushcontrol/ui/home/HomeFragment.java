package com.example.pushcontrol.ui.home;

import static com.example.pushcontrol.Constans.PreferencesConstants.*;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import com.example.pushcontrol.DataBaze.DatabaseHelper;
import com.example.pushcontrol.DataBaze.NotificBD;
import com.example.pushcontrol.R;
import com.example.pushcontrol.databinding.FragmentHomeBinding;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.MobileAds;

import java.util.List;

public class HomeFragment extends Fragment {
	private final String TAG = "AdRequest";

	private FragmentHomeBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		HomeViewModel homeViewModel =
				new ViewModelProvider(this).get(HomeViewModel.class);

		MobileAds.initialize(getActivity().getApplicationContext(), () -> {});


		binding = FragmentHomeBinding.inflate(inflater, container, false);

		binding.banner.setAdUnitId("R-M-19407785-1");
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
		List<NotificBD> notificationList;
		NotificationsAdapter adapter;

		// Проверяем пришли ли аргументы из боковой панели
		if (getArguments() != null && getArguments().containsKey(selectedPackage)) {
			String pascageName = getArguments().getString(selectedPackage);
			String appNameTitle = getArguments().getString(selectedAppName);

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
				((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Общая лента");
			}

			// Если открыли приложение просто так - показать общую ленту
			//requireActivity().getActionBar().setTitle("Общая лента");
			notificationList = dbHelper.getAllNotifications();
			adapter = new NotificationsAdapter(notificationList, true);
		}

		binding.rvNotifications.setAdapter(adapter);
		// Создаем стандартный вертикальный разделитель
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
				requireContext(),
				LinearLayoutManager.VERTICAL
		);

// Привязываем разделитель к вашему RecyclerView
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
				// Если у вас есть своя иконка в drawable, замените android.R.drawable.ic_menu_delete на неё

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

		// Переменная pascageName должна быть доступна внутри меню.
        // Чтобы использовать её там, объявите её как final выше в коде, где вы её получаете:
        // final String pascageName = getArguments().getString(packageName);


		requireActivity().addMenuProvider(new MenuProvider() {
			@Override
			public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
				// 1. Очищаем меню, чтобы пункты не дублировались при переходах
				menu.clear();

				// 2. Наполняем меню из вашего XML-файла
				menuInflater.inflate(R.menu.main, menu);

				// 3. Строгая проверка: открыто конкретное приложение или общая лента
				boolean isSpecificApp = false;
				if (getArguments() != null && getArguments().containsKey(selectedPackage)) {
					String currentPkg = getArguments().getString(selectedPackage);
					if (currentPkg != null && !currentPkg.trim().isEmpty()) {
						isSpecificApp = true;
					}
				}

				// 4. Управляем кнопкой "Очистить чат"
				MenuItem clearChatEntry = menu.findItem(R.id.action_clear_chat);
				if (clearChatEntry != null) {
					clearChatEntry.setVisible(isSpecificApp); // Покажет ТОЛЬКО в чате программы
					//menu.findItem(R.id.action_clear_all).setVisible(false);
				}

				// Управление кнопкой очистить все
				boolean isSpecificAll = true;
				if (getArguments() != null && getArguments().containsKey(selectedPackage)) {
					String currentPkg = getArguments().getString(selectedPackage);
					if (currentPkg != null && !currentPkg.trim().isEmpty()) {
						isSpecificAll = false;
					}
				}
				MenuItem clearChatEntryAll = menu.findItem(R.id.action_clear_all);
				if (clearChatEntryAll != null) {
					clearChatEntryAll.setVisible(isSpecificAll); // Покажет ТОЛЬКО в чате программы
				}

				// Пункты "Настройки приложений" (nav_settings) и "Очистить все" (action_clear_all)
				// останутся видимыми везде по умолчанию, так как мы их не прячем.
			}

			@Override
			public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
				int id = menuItem.getItemId();

				if (id == R.id.action_clear_chat) {
					if (getArguments() != null) {
						String pascageName = getArguments().getString(selectedPackage);
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
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}