package com.example.pushcontrol.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushcontrol.databinding.FragmentHomeBinding;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.MobileAds;

public class HomeFragment extends Fragment {

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

		final TextView textView = binding.textHome;
		homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
		return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}