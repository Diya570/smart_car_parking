package com.smartparking.app.ui.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.databinding.FragmentAdminBookingDetailsBinding;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.CurrencyUtils;
import com.smartparking.app.utils.TimeUtils;

public class AdminBookingDetailsFragment extends BaseFragment<FragmentAdminBookingDetailsBinding, AdminBookingDetailsViewModel> {

    private Booking booking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            booking = AdminBookingDetailsFragmentArgs.fromBundle(getArguments()).getBooking();
        }
    }

    @NonNull
    @Override
    protected FragmentAdminBookingDetailsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAdminBookingDetailsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<AdminBookingDetailsViewModel> getViewModelClass() {
        return AdminBookingDetailsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        if (booking != null) {
            viewModel.fetchUserDetails(booking.getUserId());

            binding.toolbar.setTitle("Booking #" + booking.getBookingId().substring(0, 8).toUpperCase());
            binding.bookingIdText.setText("Booking ID: " + booking.getBookingId());
            binding.lotNameText.setText("Lot: " + booking.getLotName());
            binding.slotLabelText.setText("Slot: " + booking.getSlotLabel());
            binding.statusText.setText("Status: " + booking.getStatus());
            binding.costText.setText("Cost: " + CurrencyUtils.formatCurrency(booking.getCost(), "INR"));

            String time = TimeUtils.formatDateTime(booking.getStartTime()) + " - " + TimeUtils.formatTime(booking.getEndTime());
            binding.timeText.setText(time);
        }
    }

    @Override
    protected void observeData() {
        viewModel.getUserDetails().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.status == com.smartparking.app.data.source.Result.Status.SUCCESS && result.data != null) {
                binding.userNameText.setText("User: " + result.data.getDisplayName());
                binding.userEmailText.setText(result.data.getEmail());
            } else if (result.status == com.smartparking.app.data.source.Result.Status.ERROR) {
                binding.userNameText.setText("User: Not found");
                binding.userEmailText.setText(booking.getUserId());
            }
        });
    }
}