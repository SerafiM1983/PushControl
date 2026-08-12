package com.serafimApp.pushcontrol.ui.settings;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.PREF_IS_NOTIFIGATION_ENBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serafimApp.pushcontrol.Dialog.DialogAppsLoads;
import com.serafimApp.pushcontrol.databinding.FragmentSettingsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FragmentSetting extends Fragment {

	private FragmentSettingsBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// inizialise mwme

		binding = FragmentSettingsBinding.inflate(inflater, container, false);
		SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREF_IS_NOTIFIGATION_ENBLE, Context.MODE_PRIVATE);


		View root = binding.getRoot();
		// Настраиваем RecyclerView изначально пустым списком
		binding.appRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		final List<AppModel> emptyList = new ArrayList<>();
		final AppsAdapter appsAdapter = new AppsAdapter(getActivity(), emptyList);
		binding.appRecyclerView.setAdapter(appsAdapter);

		// 1. Создаем и показываем диалог загрузки
		final DialogAppsLoads progressDialog = new DialogAppsLoads();
		progressDialog.setCancelable(false);
		progressDialog.show(getParentFragmentManager(), "loader_dialog");

		// 2. Уводим тяжелую операцию в фоновый поток
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Предварительный текст
					progressDialog.dilog_massage("Подготовка списка...");

					ListPush listPush = new ListPush();

					// Передаем progressDialog внутрь метода!
					// Теперь сам метод ListPush будет двигать прогресс на экране
					final List<AppModel> fullList = listPush.getAppsWithNotifications(
							getActivity().getApplicationContext(),
							progressDialog
					);

					// Возвращаемся на главный поток для отображения результатов и закрытия окна
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// Обновляем адаптер со всеми найденными приложениями
							AppsAdapter newAdapter = new AppsAdapter(getActivity(), fullList);
							binding.appRecyclerView.setAdapter(newAdapter);
							setupSearchView(newAdapter);

							// 3. Закрываем диалог загрузки, так как сканирование полностью завершено
							if (progressDialog.isAdded()) {
								progressDialog.dismiss();
							}
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
					// Безопасное закрытие в случае сбоя
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (progressDialog.isAdded()) progressDialog.dismiss();
						}
					});
				}
			}
		}).start();


	/*	// Получаем лист обьектов AppNotificationCheck
		ListPush listPush = new ListPush();

		List<AppModel> list = listPush.getAppsWithNotifications(getActivity().getApplicationContext());

		binding.appRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		AppsAdapter appsAdapter = new AppsAdapter(getActivity(), list);
		binding.appRecyclerView.setAdapter(appsAdapter);*/


		return root;
	}

	private void launchAppPicker() {
		Intent intent = new Intent(Intent.ACTION_PICK);
	}

	private void setupSearchView(final AppsAdapter adapter) {
		binding.appSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String s) {
				// Этот метод срабатывает автоматически при вводе каждой буквы
				adapter.filter(s);
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String s) {
				// Этот метод срабатывает, только если нажать «Поиск» на клавиатуре
				return false;
			}
		});
	}

}
