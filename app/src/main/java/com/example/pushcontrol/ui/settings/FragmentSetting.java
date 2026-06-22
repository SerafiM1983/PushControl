package com.example.pushcontrol.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
		ListPush listPush = new ListPush();
		List<String> list = listPush.getAppsWithNotifications(getActivity().getApplicationContext());

		Log.d("List", list.toString());

		// save mwme
		return root;
	}
}
