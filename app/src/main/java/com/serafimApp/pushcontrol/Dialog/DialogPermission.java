package com.serafimApp.pushcontrol.Dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.serafimApp.pushcontrol.LogCat;
import com.serafimApp.pushcontrol.R;
import com.serafimApp.pushcontrol.databinding.DialogPermissionBinding;

public class DialogPermission extends DialogFragment {
	// 1. Создаем интерфейс для обработки клика
	public interface OnDialogActionListener {
		void onActionClick(int buttonId);
	}

	private OnDialogActionListener actionListener; // Переменная хранит текущее действие

	// Переменные для динамического текста
	private int textResId, btnOffResId, btnOkResId;
	LogCat logCat = new LogCat(false); // Отключен лог

	private DialogPermissionBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = DialogPermissionBinding.inflate(inflater, container, false);
		binding.titleDialog.setText(ContextCompat.getString(getContext(), textResId));
		binding.appPermissionOff.setText(ContextCompat.getString(getContext(), btnOffResId));
		binding.appPermissionOk.setText(ContextCompat.getString(getContext(), btnOkResId));

		binding.appPermissionOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (actionListener != null) {
					actionListener.onActionClick(binding.appPermissionOk.getId());
				}
				dismiss(); // Закрываем диалог после клика
			}
		});

		binding.appPermissionOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (actionListener != null) {
					actionListener.onActionClick(binding.appPermissionOff.getId());
				}
				dismiss(); // Закрываем диалог после клика
			}
		});

		return binding.getRoot();
	}

	public void dialogNotificationListenerPermission(FragmentManager fm, OnDialogActionListener listener) {
		// Проверяем что диалог не добавлен и менеджер не null
		if (fm != null && !this.isAdded()) {
			this.actionListener = listener;
			this.textResId = R.string.permission_loads;
			this.btnOffResId = R.string.permission_loads_of_notification_listener;
			this.btnOkResId = R.string.permission_loads_ok_notification_listener;
			if (fm != null && !this.isAdded()) this.show(fm, "DialogPermission");
		}

	}

	public void dialogPostNotificationPermission(FragmentManager fm, OnDialogActionListener listener) {
		// Проверяем что диалог не добавлен и менеджер не null
		if (fm != null && !this.isAdded()) {
			this.actionListener = listener;
			this.textResId = R.string.text_dialog_2;
			this.btnOffResId = R.string.permission_loads_of_notification_listener;
			this.btnOkResId = R.string.permission_loads_ok_notification_listener;
			if (fm != null && !this.isAdded()) this.show(fm, "DialogPermission");
		}

	}
}
