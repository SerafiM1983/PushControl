package com.example.pushcontrol.ui.home;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushcontrol.DataBaze.NotificBD;
import com.example.pushcontrol.R;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifViewHolder> {

	private final List<NotificBD> notifications;
	// Флаг для отображения иконки и названия приложения
	private boolean isGeneralFeed;

	public NotificationsAdapter(List<NotificBD> notifications, boolean isGeneralFeed) {
		this.notifications = notifications;
		this.isGeneralFeed = isGeneralFeed;
	}

	@NonNull
	@Override
	public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
		return new NotifViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
		NotificBD notif = notifications.get(position);
		// АБСОЛЮТНЫЙ СБРОС СОСТОЯНИЯ ЯЧЕЙКИ (Защита от багов переиспользования)
		holder.imgAttachment.setImageBitmap(null);
		holder.imgAttachment.setVisibility(View.GONE);
		holder.imageApp.setImageDrawable(null);
		holder.ll.setVisibility(View.GONE);

		// Заполнение базовых полей
		holder.tvTitle.setText(notif.getTitle());
		String text = notif.getText();

		if (text != null) {
			String lowerText = text.toLowerCase(); // Защита от разного регистра (.MP4 / .mp4)

			if (lowerText.contains("clip-") && lowerText.contains(".mp4") && lowerText.contains("screen_recording_")) {
				// Строго клип VK: заменяем текст на красивую строку
				holder.tvText.setText(holder.itemView.getContext().getString(R.string.loading_finish));
			} else {
				// Для всех остальных файлов .mp4 и обычных сообщений — выводим оригинальный текст
				holder.tvText.setText(text);
			}
		} else {
			holder.tvText.setText("");
		}

		// если это общяя лента показываем иконку и имя источника
		if (isGeneralFeed) {
			holder.ll.setVisibility(View.VISIBLE);
			holder.imgAttachment.setVisibility(View.GONE);

			PackageManager pm = holder.itemView.getContext().getPackageManager();
			// Получаем Имя
			String pkgName = notif.getPackageName();
			try {
				// Получаем название приложения
				ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
				String appName = pm.getApplicationLabel(appInfo).toString();
				holder.tvAppName.setText(appName);

				// Получаем иконку
				Drawable appIcon = pm.getApplicationIcon(pkgName);
				holder.imageApp.setImageDrawable(appIcon);
			} catch (PackageManager.NameNotFoundException e) {
				// Если приложение было удалено с телефона
				holder.tvAppName.setText(pkgName);
				holder.imageApp.setImageResource(android.R.drawable.sym_def_app_icon);
			}
		} else {
		// ВНУТРИ ЧАТА ПРОГРАММЫ
		holder.ll.setVisibility(View.GONE); // Скрываем шапку приложения

		byte[] imageBytes = notif.getImage();
		String currentText = notif.getText();

		// ЖЕЛЕЗНОЕ УСЛОВИЕ: Картинка показывается ТОЛЬКО если текст содержит маркер фото
		if (imageBytes != null && imageBytes.length > 0 && currentText != null && currentText.contains("📷 фото")) {

			Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
			if (bitmap != null) {
				holder.imgAttachment.setVisibility(View.VISIBLE);
				holder.imgAttachment.setImageBitmap(bitmap);
			} else {
				// Защита от сбоя декодирования
				holder.imgAttachment.setImageBitmap(null);
				holder.imgAttachment.setVisibility(View.GONE);
			}
		} else {
			// Для сообщения с текстом "Это официальная информация..."
			// Мы ПРИНУДИТЕЛЬНО очищаем и скрываем ImageView, убирая дубликат картинки!
			holder.imgAttachment.setImageBitmap(null);
			holder.imgAttachment.setVisibility(View.GONE);
		}
	}

}

	@Override
	public int getItemCount() {
		return notifications.size();
	}

	// Этот метод вызовет активити при свайпе чтобы получить что удалять
	public NotificBD getItemAt(int position) {
		return  notifications.get(position);
	}

	// Этот метод удалит элемент с экрана
	public void removeItem(int position) {
		notifications.remove(position);
		notifyItemRemoved(position);
		// Здесь можно удалить из базы наверное
	}

	public static class NotifViewHolder extends RecyclerView.ViewHolder {
		TextView tvTitle, tvText, tvAppName;
		ImageView imageApp, imgAttachment;
		LinearLayout ll;

		public NotifViewHolder(@NonNull View itemView) {
			super(itemView);
			tvTitle = itemView.findViewById(R.id.notif_title);
			tvText = itemView.findViewById(R.id.notif_text);
			tvAppName = itemView.findViewById(R.id.appNameTitle);
			imageApp = itemView.findViewById(R.id.appIconTitle);
			ll = itemView.findViewById(R.id.llAppImageAppName);
			imgAttachment = itemView.findViewById(R.id.img_notification_attachment);
		}
	}
}

