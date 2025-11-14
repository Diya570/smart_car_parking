package com.smartparking.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.smartparking.app.R;
import com.smartparking.app.data.model.User;
import com.smartparking.app.databinding.ItemUserAdminViewBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users = new ArrayList<>();
    private final OnUserClickListener clickListener; // <-- ADD THIS

    // --- ADD THIS INTERFACE ---
    public interface OnUserClickListener {
        void onUserClicked(User user);
    }
    // -------------------------

    // --- UPDATE THE CONSTRUCTOR ---
    public UsersAdapter(OnUserClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setUsers(List<User> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserAdminViewBinding binding = ItemUserAdminViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
        // --- ADD THIS CLICK LISTENER ---
        holder.itemView.setOnClickListener(v -> clickListener.onUserClicked(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserAdminViewBinding binding;

        public UserViewHolder(ItemUserAdminViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            Context context = itemView.getContext();
            binding.userNameText.setText(user.getDisplayName());
            binding.userEmailText.setText(user.getEmail());

            String role = user.getRole() != null ? user.getRole() : "user";
            binding.userRoleTag.setText(role.substring(0, 1).toUpperCase() + role.substring(1));

            if ("admin".equalsIgnoreCase(role)) {
                binding.userRoleTag.setTextColor(ContextCompat.getColor(context, R.color.status_green_text));
                binding.userRoleTag.setBackgroundColor(ContextCompat.getColor(context, R.color.status_green_bg));
            } else {
                binding.userRoleTag.setTextColor(ContextCompat.getColor(context, R.color.status_gray_text));
                binding.userRoleTag.setBackgroundColor(ContextCompat.getColor(context, R.color.status_gray_bg));
            }

            Glide.with(context)
                    .load("https://i.pravatar.cc/150?u=" + user.getEmail())
                    .placeholder(R.drawable.ic_menu_profile)
                    .into(binding.userAvatar);
        }
    }
}