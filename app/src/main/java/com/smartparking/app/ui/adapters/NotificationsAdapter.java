package com.smartparking.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.data.model.Announcement;
import com.smartparking.app.databinding.ItemNotificationBinding;
import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private final List<Announcement> announcements = new ArrayList<>();

    public void setAnnouncements(List<Announcement> newAnnouncements) {
        this.announcements.clear();
        this.announcements.addAll(newAnnouncements);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(announcements.get(position));
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Announcement announcement) {
            binding.notificationTitleText.setText(announcement.getTitle());
            binding.notificationMessageText.setText(announcement.getMessage());
        }
    }
}