package com.smartparking.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.databinding.ItemBookingUserBinding;
import com.smartparking.app.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class UserBookingsAdapter extends RecyclerView.Adapter<UserBookingsAdapter.BookingViewHolder> {

    private final List<Booking> bookings = new ArrayList<>();
    private final OnCancelClickListener cancelClickListener;
    private final OnItemClickListener itemClickListener;

    public interface OnCancelClickListener {
        void onCancelClick(Booking booking);
    }

    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }

    public UserBookingsAdapter(OnCancelClickListener cancelListener, OnItemClickListener itemListener) {
        this.cancelClickListener = cancelListener;
        this.itemClickListener = itemListener;
    }

    public void setBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingUserBinding binding = ItemBookingUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding, cancelClickListener, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingUserBinding binding;
        private final OnCancelClickListener cancelClickListener;
        private final OnItemClickListener itemClickListener;

        public BookingViewHolder(ItemBookingUserBinding binding, OnCancelClickListener cancelListener, OnItemClickListener itemListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.cancelClickListener = cancelListener;
            this.itemClickListener = itemListener;
        }

        void bind(Booking booking) {
            Context context = itemView.getContext();
            long now = System.currentTimeMillis();

            // Determine the display status
            String displayStatus = booking.getStatus();
            boolean isCanceled = "canceled".equalsIgnoreCase(displayStatus);
            boolean isPastBooking = booking.getEndTime() < now;

            if (isPastBooking && !isCanceled) {
                displayStatus = "Completed";
            }

            // A booking is "Current" if it's not canceled and not completed
            boolean isCurrent = !isPastBooking && !isCanceled;

            binding.bookingTypeLabel.setText(isCurrent ? "Current" : "Past");
            binding.lotNameText.setText(booking.getLotName() != null ? booking.getLotName() : "Lot ID: " + booking.getLotId());
            binding.slotIdText.setText("Slot: " + (booking.getSlotLabel() != null ? booking.getSlotLabel() : booking.getSlotId()));
            binding.bookingDateText.setText(TimeUtils.formatDate(booking.getStartTime()));
            binding.bookingTimeText.setText(TimeUtils.formatTime(booking.getStartTime()));

            binding.statusChip.setText(displayStatus);
            styleChip(binding.statusChip, displayStatus, context);

            // "Cancel" button is only visible for "confirmed" (not "checkedIn") bookings in the future
            boolean canCancel = "confirmed".equalsIgnoreCase(booking.getStatus()) && !isPastBooking;
            binding.cancelButton.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            binding.cancelButton.setOnClickListener(v -> cancelClickListener.onCancelClick(booking));

            // Item is clickable to see QR code only if it's "confirmed" and in the future
            itemView.setOnClickListener(v -> {
                if (canCancel) {
                    itemClickListener.onItemClick(booking);
                }
            });
        }

        private void styleChip(Chip chip, String status, Context context) {
            switch (status.toLowerCase()) {
                case "confirmed":
                case "checkedin":
                    chip.setChipBackgroundColorResource(R.color.status_green_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_green_text));
                    chip.setChipIconResource(R.drawable.ic_menu_check_circle);
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
                default:
                    chip.setChipBackgroundColorResource(R.color.status_orange_bg);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.status_orange_text));
                    chip.setChipIcon(null);
                    break;
            }
        }
    }
}