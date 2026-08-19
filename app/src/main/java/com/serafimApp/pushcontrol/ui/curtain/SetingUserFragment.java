package com.serafimApp.pushcontrol.ui.curtain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.serafimApp.pushcontrol.databinding.FragmentSetingUserBinding;
import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.USER_JPEG;
import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.USER_SBN_SHOV;
import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.PREF_IS_NOTIFIGATION_ENBLE;



public class SetingUserFragment extends Fragment {
	private SharedPreferences sPref;
	private FragmentSetingUserBinding binding;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentSetingUserBinding.inflate(inflater, container, false);
		View getRoot = binding.getRoot();

		sPref = requireContext().getSharedPreferences(PREF_IS_NOTIFIGATION_ENBLE, Context.MODE_PRIVATE);

		binding.cbUserJpg.setChecked(sPref.getBoolean(USER_JPEG, false));
		binding.cbUserSbnClear.setChecked(sPref.getBoolean(USER_SBN_SHOV, false));

		binding.cbUserJpg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
				sPref.edit()
						.putBoolean(USER_JPEG, b)
						.apply();
			}
		});

		binding.cbUserSbnClear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
				sPref.edit()
						.putBoolean(USER_SBN_SHOV, b)
						.apply();
			}
		});

		return getRoot;
	}
}