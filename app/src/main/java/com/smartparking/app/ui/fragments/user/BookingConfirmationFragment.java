package com.smartparking.app.ui.fragments.user;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.databinding.FragmentBookingConfirmationBinding;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.CurrencyUtils;
import com.smartparking.app.utils.QRUtils;
import com.smartparking.app.utils.TimeUtils;

import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.Slot;


public class BookingConfirmationFragment extends BaseFragment<FragmentBookingConfirmationBinding, BookingConfirmationViewModel> {

    private Booking confirmedBooking;
    private ParkingLot lot;
    private Slot slot;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            BookingConfirmationFragmentArgs args = BookingConfirmationFragmentArgs.fromBundle(getArguments());
            confirmedBooking = args.getConfirmedBooking();
            lot = args.getParkingLot();
            slot = args.getSlot();
        }
    }

    @NonNull
    @Override
    protected FragmentBookingConfirmationBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentBookingConfirmationBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<BookingConfirmationViewModel> getViewModelClass() {
        return BookingConfirmationViewModel.class;
    }

    @Override
    protected void setupViews() {
        displayConfirmationDetails();
        // THE FIX IS HERE: The listener for the non-existent doneButton is removed.
        // The user will use the toolbar's back arrow to navigate.
        binding.toolbar.setNavigationOnClickListener(v ->
                // This action pops all the way back to the map and then navigates to the bookings list.
                Navigation.findNavController(v).navigate(
                        BookingConfirmationFragmentDirections.actionBookingConfirmationFragmentToMyBookingsFragment()
                )
        );
    }

    @Override
    protected void observeData() {
        // No LiveData to observe in this simple confirmation screen
    }

    private void displayConfirmationDetails() {
        if (confirmedBooking != null && lot != null && slot != null) {
            binding.lotNameText.setText(lot.getName());
            binding.lotAddressText.setText(lot.getAddress());
            binding.bookingIdText.setText("#" + confirmedBooking.getBookingId().substring(0, 8).toUpperCase());
            binding.dateText.setText(TimeUtils.formatDate(confirmedBooking.getStartTime()));
            binding.timeText.setText(String.format("%s - %s",
                    TimeUtils.formatTime(confirmedBooking.getStartTime()),
                    TimeUtils.formatTime(confirmedBooking.getEndTime())));
            binding.costText.setText(CurrencyUtils.formatCurrency(confirmedBooking.getCost(), lot.getPricing().getCurrency()));
            binding.otpText.setText("OTP: " + confirmedBooking.getOtp());

            Bitmap qrCodeBitmap = QRUtils.generateQrCode(
                    confirmedBooking.getBookingId(),
                    confirmedBooking.getUserId(),
                    confirmedBooking.getLotId(),
                    confirmedBooking.getEntryToken()
            );
            if (qrCodeBitmap != null) {
                binding.qrCodeImage.setImageBitmap(qrCodeBitmap);
            }
        }
    }
}