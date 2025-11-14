package com.smartparking.app.ui.fragments.user;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.databinding.FragmentBookingDetailsBinding;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.QRUtils;
import com.smartparking.app.utils.TimeUtils;

public class BookingDetailsFragment extends BaseFragment<FragmentBookingDetailsBinding,
        BookingDetailsViewModel> {

    private Booking currentBooking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentBooking = BookingDetailsFragmentArgs.fromBundle(getArguments()).getBooking();
        }
    }

    @NonNull
    @Override
    protected FragmentBookingDetailsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentBookingDetailsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<BookingDetailsViewModel> getViewModelClass() {
        return BookingDetailsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        //displayBookingDetails();
    }

    @Override
    protected void observeData() {
        // No LiveData to observe, as all data is passed in arguments.
        if (currentBooking == null) return;

        // Observe the list of all lots
        viewModel.getAllLots().observe(getViewLifecycleOwner(), result -> {
            if (result.status == com.smartparking.app.data.source.Result.Status.SUCCESS && result.data != null) {
                // Find the matching lot from the list
                ParkingLot lot = result.data.stream()
                        .filter(p -> currentBooking.getLotId().equals(p.getId()))
                        .findFirst()
                        .orElse(null);

                displayBookingDetails(lot);
            }
        });
    }

    private void displayBookingDetails(ParkingLot lot) {
        if (currentBooking != null) {

            // THE FIX IS HERE: Use the new fields with a fallback for old data
            String lotName = currentBooking.getLotName() != null ? currentBooking.getLotName() : "Lot ID: " + currentBooking.getLotId();
            String slotLabel = currentBooking.getSlotLabel() != null ? currentBooking.getSlotLabel() : "Slot ID: "+ currentBooking.getSlotId();

            binding.lotNameText.setText(lotName);
            binding.lotAddressText.setText("Slot: " + slotLabel); // We can show the address here if we fetch the lot

            binding.dateText.setText(TimeUtils.formatDate(currentBooking.getStartTime()));
            binding.timeText.setText(String.format("%s - %s",
                    TimeUtils.formatTime(currentBooking.getStartTime()),
                    TimeUtils.formatTime(currentBooking.getEndTime())));
            binding.otpText.setText("OTP: " + currentBooking.getOtp());


                Bitmap qrCodeBitmap = QRUtils.generateQrCode(
                        currentBooking.getBookingId(),
                        currentBooking.getUserId(),
                        currentBooking.getLotId(),
                        currentBooking.getEntryToken()
                );
                if (qrCodeBitmap != null) {
                    binding.qrCodeImage.setImageBitmap(qrCodeBitmap);
                }
        }
    }
}