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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.WorkManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.databinding.FragmentMyBookingsBinding;
import com.smartparking.app.ui.adapters.UserBookingsAdapter;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.NotificationHelper;
import java.util.List;

public class MyBookingsFragment extends BaseFragment<FragmentMyBookingsBinding, MyBookingsViewModel> {

    private UserBookingsAdapter adapter;

    @NonNull
    @Override
    protected FragmentMyBookingsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMyBookingsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<MyBookingsViewModel> getViewModelClass() {
        return MyBookingsViewModel.class;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch bookings every time the view is created to ensure data is fresh
        viewModel.fetchUserBookings();
    }

    @Override
    protected void setupViews() {
        // Initialize the adapter with two listeners: one for cancellation, one for item clicks.
        adapter = new UserBookingsAdapter(
                // OnCancelClickListener
                booking -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Cancel Booking?")
                            .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                            .setPositiveButton("Yes, Cancel", (dialog, which) ->
                            {
                                WorkManager.getInstance(requireContext())
                                        .cancelAllWorkByTag("booking_expiry_" + booking.getBookingId());


                                viewModel.cancelBooking(booking,null);

                                sendCancellationNotification(booking);
                            })
                            .setNegativeButton("No", null)
                            .show();
                },
                booking -> {
                    MyBookingsFragmentDirections.ActionMyBookingsFragmentToBookingDetailsFragment action =
                            MyBookingsFragmentDirections.actionMyBookingsFragmentToBookingDetailsFragment(booking);
                    Navigation.findNavController(requireView()).navigate((NavDirections) action);
                }
        );
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBookings.setAdapter(adapter);

        // This call is now in onViewCreated() for better data freshness
        // viewModel.fetchUserBookings();
    }

    @Override
    protected void observeData() {
        // Observer for the list of bookings.
        viewModel.getUserBookings().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.recyclerViewBookings.setVisibility(View.GONE);
                    binding.textEmptyState.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.data != null && !result.data.isEmpty()) {
                        adapter.setBookings(result.data);
                        binding.recyclerViewBookings.setVisibility(View.VISIBLE);
                        binding.textEmptyState.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewBookings.setVisibility(View.GONE);
                        binding.textEmptyState.setText(R.string.no_bookings_found);
                        binding.textEmptyState.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerViewBookings.setVisibility(View.GONE);
                    binding.textEmptyState.setText("Error: " + result.message);
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    break;
            }
        });

        // Observer for the result of a cancellation attempt.
        viewModel.getCancelBookingResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    // You can optionally show a small toast or log
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Booking Canceled Successfully", Toast.LENGTH_SHORT).show();
                    viewModel.fetchUserBookings(); // Refresh the list after cancellation.
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Cancellation Failed: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
    private void sendCancellationNotification(Booking booking) {
        // Check if context is still valid before showing notification
        if (getContext() == null) {
            return;
        }

        String title = getString(R.string.booking_canceled_title);
        String body = getString(R.string.booking_canceled_body,
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