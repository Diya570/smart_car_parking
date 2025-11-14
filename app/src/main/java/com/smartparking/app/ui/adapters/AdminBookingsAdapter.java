package com.smartparking.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.databinding.ItemAdminBookingBinding;
import com.smartparking.app.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.AdminBookingViewHolder> {

    private final List<Booking> bookings = new ArrayList<>();
    private final OnBookingClickListener clickListener;

    /**
     * Listener interface for handling a click on a booking item.
     */
    public interface OnBookingClickListener {
        void onBookingClicked(Booking booking);
    }

    /**
     * Constructor that accepts the click listener.
     * @param clickListener The fragment that will handle the click event.
     */
    public AdminBookingsAdapter(OnBookingClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminBookingBinding binding = ItemAdminBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminBookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener(v -> clickListener.onBookingClicked(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    /**
     * ViewHolder class for displaying a single booking in the admin list.
     */
    static class AdminBookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminBookingBinding binding;

        public AdminBookingViewHolder(ItemAdminBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            Context context = itemView.getContext();

            // Set the text fields, using the human-readable names
            binding.bookingIdText.setText("Booking #" + booking.getBookingId().substring(0, 8).toUpperCase());
            binding.lotInfoText.setText(booking.getLotName() + " - " + booking.getSlotLabel());
            binding.bookingTimeText.setText(TimeUtils.formatDateTime(booking.getStartTime()));

            String status = booking.getStatus();
            long now = System.currentTimeMillis();

            // Automatically determine if the booking is completed
            if (!"canceled".equalsIgnoreCase(status) && booking.getEndTime() < now) {
                status = "Completed";
            }

            binding.statusText.setText(status);
            styleChip(binding.statusText, status, context);
        }

        private void styleChip(Chip chip, String status, Context context) {
            switch (status.toLowerCase()) {
                case "confirmed":
                    chip.setChipBackgroundColorResource(R.color.status_green_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_green_text));
                    chip.setChipIconResource(R.drawable.ic_menu_check_circle);
                    break;
                case "checkedin":
                    chip.setChipBackgroundColorResource(R.color.status_green_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_green_text));
                    chip.setChipIconResource(R.drawable.ic_menu_check_in);
                    break;
                case "completed":
                    chip.setChipBackgroundColorResource(R.color.status_gray_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_gray_text));
                    chip.setChipIconResource(R.drawable.ic_menu_history);
                    break;
                case "canceled":
                    chip.setChipBackgroundColorResource(R.color.status_red_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_red_text));
                    chip.setChipIconResource(R.drawable.ic_menu_cancel);
                    break;
                default: // For "pending" or other statuses
                    chip.setChipBackgroundColorResource(R.color.status_orange_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_orange_text));
                    chip.setChipIcon(null);
                    break;
            }
        }
    }
}