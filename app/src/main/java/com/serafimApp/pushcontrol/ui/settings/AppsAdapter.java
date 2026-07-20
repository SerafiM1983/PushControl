package com.serafimApp.pushcontrol.ui.settings;

import static com.serafimApp.pushcontrol.Constans.PreferencesConstants.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimApp.pushcontrol.R;

import java.util.ArrayList;
import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {
	// Исходный список который не меняется
	private List<AppModel> originalList;
	// Текущий список, который отображается
	private List<AppModel> currentList;
	private final PackageManager packageManager;
	private final SharedPreferences sharedPreferences;

	public AppsAdapter(Context context, List<AppModel> appsList) {
		this.originalList = new ArrayList<>(appsList);
		this.currentList = new ArrayList<>(appsList);
		this.packageManager = context.getPackageManager();
		this.sharedPreferences = context.getSharedPreferences(prefIsNotifigationEnble, Context.MODE_PRIVATE);
	}

	@NonNull
	@Override
	public AppsAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).
				inflate(R.layout.app_notification_layncer, parent, false);
		return new AppViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull AppsAdapter.AppViewHolder holder, int position) {
		AppModel app = currentList.get(position);

		holder.tvAppName.setText(app.getAppName());

		// Установка состояния передвижного переключателя
		holder.switchNotification.setOnCheckedChangeListener(null);
		holder.switchNotification.setChecked(app.isNotificationEnable());

		// Подгружаем иконку
		try {
			Drawable icon = packageManager.getApplicationIcon(app.getPackageName());
			holder.imgAppIcon.setImageDrawable(icon);
		} catch (PackageManager.NameNotFoundException e) {
			holder.imgAppIcon.setImageResource(android.R.drawable.sym_def_app_icon);
		}

		holder.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
			app.setNotificationEnable(isChecked);

			// Сохраняем статус: ключ — просто имя пакета
			sharedPreferences.edit()
					.putBoolean(app.getPackageName(), isChecked)
					.apply();
		});
	}

	@Override
	public int getItemCount() {
		return currentList != null ? currentList.size() : 0;
	}

	// Метод поиска и фильртации
	public void filter(String query) {
		currentList.clear(); // Очищяем текущий список
		if (query == null || query.isEmpty()) {
			// Запрос пустой возвращяем все элементы
			currentList.addAll(originalList);
		} else {
			String lowerCaseQuery = query.toLowerCase().trim();

			for (AppModel item : originalList) {
				if (item.getAppName() != null && item.getAppName().toLowerCase().contains(lowerCaseQuery)) {
					currentList.add(item);
				}
			}
		}
		notifyDataSetChanged();
	}

	public static class AppViewHolder extends RecyclerView.ViewHolder {
		ImageView imgAppIcon;
		TextView tvAppName;
		SwitchCompat switchNotification;

		public AppViewHolder(@NonNull View itemView) {
			super(itemView);
			imgAppIcon = itemView.findViewById(R.id.image_app_load);
			tvAppName = itemView.findViewById(R.id.tvAppName);
			switchNotification = itemView.findViewById(R.id.app_switch);
		}
	}
}
