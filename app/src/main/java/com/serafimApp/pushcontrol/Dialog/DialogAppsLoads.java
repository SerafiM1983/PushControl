package com.serafimApp.pushcontrol.Dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.serafimApp.pushcontrol.databinding.DialogLoadAppsBinding;

public class DialogAppsLoads extends DialogFragment {
	private DialogLoadAppsBinding binding;
	private String currentMessage = "com.serafimApps"; // Текст по умолчанию

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = DialogLoadAppsBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Как только разметка создана, сразу выводим актуальный текст
		binding.textLoads.setText(currentMessage);
	}

	public void dilog_massage(String mgs) {
		this.currentMessage = mgs;
		if (binding != null && binding.textLoads != null) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					if (binding != null) {
						binding.textLoads.setText(mgs);
					}
				}
			});
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Обязательно зануляем биндинг для предотвращения утечек памяти (Memory Leaks)
		binding = null;
	}
}
