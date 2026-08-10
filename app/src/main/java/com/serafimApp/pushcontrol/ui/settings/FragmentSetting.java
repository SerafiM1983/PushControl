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

import com.serafimApp.pushcontrol.databinding.FragmentSettingsBinding;

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
		// Получаем лист обьектов AppNotificationCheck
		ListPush listPush = new ListPush();

		List<AppModel> list = listPush.getAppsWithNotifications(getActivity().getApplicationContext());

		binding.appRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		AppsAdapter appsAdapter = new AppsAdapter(getActivity(), list);
		binding.appRecyclerView.setAdapter(appsAdapter);

		binding.appSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String s) {
				// Вытаскиваем автоматически при вводе каждого символа
				appsAdapter.filter(s);
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String s) {
				// Действие при нажатии кнопки поиск на клавиатуре

				return false;
			}
		});

		return root;
	}

	private void launchAppPicker() {
		Intent intent = new Intent(Intent.ACTION_PICK);
	}
}
