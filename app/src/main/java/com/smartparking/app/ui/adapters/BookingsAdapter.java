package com.smartparking.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.databinding.ItemBookingBinding;
import com.smartparking.app.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    private final List<Booking> bookings = new ArrayList<>();
    private final OnCancelClickListener cancelClickListener;

    public interface OnCancelClickListener {
        void onCancelClick(Booking booking);
    }

    public BookingsAdapter(OnCancelClickListener listener) {
        this.cancelClickListener = listener;
    }

    public void setBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding, cancelClickListener);
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
        private final ItemBookingBinding binding;
        private final OnCancelClickListener cancelClickListener;

        public BookingViewHolder(ItemBookingBinding binding, OnCancelClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.cancelClickListener = listener;
        }

        void bind(Booking booking) {
            // In a real app, you would perform a lookup to get the actual lot/slot names
            binding.lotNameText.setText("Lot ID: " + booking.getLotId());
            binding.slotLabelText.setText("Slot ID: " + booking.getSlotId());
            binding.bookingTimeText.setText(String.format("%s - %s",
                    TimeUtils.formatDateTime(booking.getStartTime()),
                    TimeUtils.formatTime(booking.getEndTime())));
            binding.statusText.setText(booking.getStatus().toUpperCase());

            // Logic to show cancel button: booking must be 'confirmed' and in the future
            boolean canCancel = "confirmed".equalsIgnoreCase(booking.getStatus()) &&
                    (booking.getStartTime() > System.currentTimeMillis());

            binding.cancelButton.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            binding.cancelButton.setOnClickListener(v -> cancelClickListener.onCancelClick(booking));
        }
    }
}