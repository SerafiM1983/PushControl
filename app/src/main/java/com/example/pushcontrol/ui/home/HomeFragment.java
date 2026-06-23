package com.example.pushcontrol.ui.home;

import static com.example.pushcontrol.Constans.PreferencesConstants.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pushcontrol.DataBaze.DatabaseHelper;
import com.example.pushcontrol.DataBaze.NotificBD;
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

		// Проверяем пришли ли аргументы из боковой панели
		if (getArguments() != null && getArguments().containsKey(selectedPackage)) {
			String pascageName = getArguments().getString(selectedPackage);

			// Загружаем пуши только для кликнутой программы
			notificationList = dbHelper.getNotificationsByPackage(pascageName);
		} else {
			// Если открыли приложение просто так - показать общую ленту
			notificationList = dbHelper.getAllNotifications();
		}

		// Передаю данные в адаптер и вывожу на экран
		NotificationsAdapter adapter = new NotificationsAdapter(notificationList);
		binding.rvNotifications.setAdapter(adapter);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}