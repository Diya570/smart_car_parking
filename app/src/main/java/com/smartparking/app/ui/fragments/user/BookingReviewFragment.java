package com.smartparking.app.ui.fragments.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.databinding.FragmentBookingReviewBinding;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.CurrencyUtils;
import com.smartparking.app.utils.TimeUtils;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.smartparking.app.R;
import com.smartparking.app.utils.NotificationHelper;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.smartparking.app.constants.Constants;
import com.smartparking.app.workers.BookingExpireWorker;
import java.util.concurrent.TimeUnit;

public class BookingReviewFragment extends BaseFragment<FragmentBookingReviewBinding, BookingReviewViewModel> {

    private ParkingLot lot;
    private Slot slot;
    private long startTime;
    private long endTime;
    private double calculatedCost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            BookingReviewFragmentArgs args = BookingReviewFragmentArgs.fromBundle(getArguments());
            lot = args.getParkingLot();
            slot = args.getSelectedSlot();
            startTime = args.getStartTime();
            endTime = args.getEndTime();
        }
    }

    @NonNull
    @Override
    protected FragmentBookingReviewBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentBookingReviewBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<BookingReviewViewModel> getViewModelClass() {
        return BookingReviewViewModel.class;
    }

    @Override
    protected void setupViews() {
        displayReviewDetails();
        binding.confirmButton.setOnClickListener(v -> createBooking());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    @Override
    protected void observeData() {
        viewModel.getCreateBookingResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.confirmButton.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Booking Confirmed!", Toast.LENGTH_SHORT).show();
                    Booking confirmedBooking = result.data;
                    if (confirmedBooking != null) {
                        scheduleExpiryNotification(confirmedBooking);
                        sendConfirmationNotification(confirmedBooking);
                        BookingReviewFragmentDirections.ActionBookingReviewFragmentToBookingConfirmationFragment action =
                                BookingReviewFragmentDirections.actionBookingReviewFragmentToBookingConfirmationFragment(confirmedBooking, lot, slot);
                        Navigation.findNavController(requireView()).navigate((NavDirections) action);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.confirmButton.setEnabled(true);
                    // This will now show the specific error, e.g., "Slot is unavailable..."
                    Toast.makeText(getContext(), "Booking Failed: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void displayReviewDetails() {
        if (lot == null || slot == null) return;

        binding.lotNameText.setText(lot.getName());
        binding.lotAddressText.setText(lot.getAddress());
        binding.slotLabelText.setText("Lot " + lot.getName() + ", Space " + slot.getLabel());
        binding.bookingTimeText.setText(String.format("%s - %s",
                TimeUtils.formatTime(startTime), TimeUtils.formatTime(endTime)));

        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);
        binding.durationText.setText(String.format("%d minutes", durationMinutes));

        calculatedCost = calculateCost(durationMinutes);
        binding.totalCostText.setText(CurrencyUtils.formatCurrency(calculatedCost, lot.getPricing().getCurrency()));
    }

    private double calculateCost(long durationInMinutes) {
        if (lot.getPricing() == null) return 0.0;

        long billedMinutes = Math.max(durationInMinutes, lot.getPricing().getMinBillableMinutes());
        int rounding = lot.getPricing().getRoundingToMinutes();

        if (rounding > 0 && billedMinutes % rounding != 0) {
            billedMinutes = ((billedMinutes / rounding) + 1) * rounding;
        }

        double hours = billedMinutes / 60.0;
        return hours * lot.getPricing().getPerHour();
    }

    private void createBooking() {
        Booking newBooking = new Booking();
        newBooking.setLotId(lot.getId());
        newBooking.setLotName(lot.getName()); // <-- Add Lot Name
        newBooking.setSlotId(slot.getId());
        newBooking.setSlotLabel(slot.getLabel()); // <-- Add Slot Label
        newBooking.setStartTime(startTime);
        newBooking.setEndTime(endTime);
        newBooking.setCost(calculatedCost);
        newBooking.setOtp(String.format("%06d", new Random().nextInt(999999)));

        viewModel.createBooking(newBooking, lot.getName(), requireContext());
    }

    private void scheduleExpiryNotification(Booking booking) {
        long now = System.currentTimeMillis();
        long endTime = booking.getEndTime();

        // Calculate the delay from now until the booking's end time
        long delay = endTime - now;

        // Only schedule if the end time is in the future
        if (delay > 0) {
            // Pass the booking info to the worker
            Data inputData = new Data.Builder()
                    .putString(Constants.WORK_DATA_BOOKING_ID, booking.getBookingId())
                    .putString(Constants.WORK_DATA_LOT_NAME, booking.getLotName())
                    .build();

            OneTimeWorkRequest expiryWorkRequest = new OneTimeWorkRequest.Builder(BookingExpireWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("booking_expiry_" + booking.getBookingId()) // Unique tag
                    .build();

            WorkManager.getInstance(requireContext()).enqueue(expiryWorkRequest);
        }
    }

    private void sendConfirmationNotification(Booking booking) {
        // Get the strings we just added to strings.xml
        String title = getString(R.string.booking_confirmed_title);
        String body = getString(R.string.booking_confirmed_body,
                booking.getLotName(),
                booking.getSlotLabel());

        // Use the existing helper to show the notification
        NotificationHelper.showNotification(
                requireContext(),
                title,
                body,
                NotificationHelper.CHANNEL_ID_BOOKINGS
        );
    }
}