package com.example.pushcontrol.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushcontrol.DataBaze.NotificBD;
import com.example.pushcontrol.R;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifViewHolder> {

	private final List<NotificBD> notifications;

	public NotificationsAdapter(List<NotificBD> notifications) {
		this.notifications = notifications;
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
		holder.tvTitle.setText(notif.getTitle());
		holder.tvText.setText(notif.getText());
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
		TextView tvTitle, tvText;

		public NotifViewHolder(@NonNull View itemView) {
			super(itemView);
			tvTitle = itemView.findViewById(R.id.notif_title);
			tvText = itemView.findViewById(R.id.notif_text);
		}
	}
}

