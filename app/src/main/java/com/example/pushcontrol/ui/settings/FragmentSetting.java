package com.example.pushcontrol.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pushcontrol.databinding.FragmentSettingsBinding;

import java.util.List;


public class FragmentSetting extends Fragment {

	private FragmentSettingsBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// inizialise mwme

		binding = FragmentSettingsBinding.inflate(inflater, container, false);

		View root = binding.getRoot();
		// Получаем лист обьектов AppNotificationCheck
		ListPush listPush = new ListPush();

		List<AppModel> list = listPush.getAppsWithNotifications(getActivity().getApplicationContext());

		binding.appRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		AppsAdapter appsAdapter = new AppsAdapter(getActivity(), list);
		binding.appRecyclerView.setAdapter(appsAdapter);

		// save mwme
		return root;
	}
}
