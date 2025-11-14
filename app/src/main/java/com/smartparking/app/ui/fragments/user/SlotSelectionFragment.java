package com.smartparking.app.ui.fragments.user;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.R;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.databinding.FragmentSlotSelectionBinding;
import com.smartparking.app.ui.adapters.SlotTypeAdapter;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.TimeUtils;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SlotSelectionFragment extends BaseFragment<FragmentSlotSelectionBinding, SlotSelectionViewModel> {

    private ParkingLot currentLot;
    private SlotTypeAdapter adapter;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private Slot selectedSlot = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve the ParkingLot object passed from the previous screen
            currentLot = SlotSelectionFragmentArgs.fromBundle(getArguments()).getParkingLot();
        }
    }

    @NonNull
    @Override
    protected FragmentSlotSelectionBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSlotSelectionBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<SlotSelectionViewModel> getViewModelClass() {
        return SlotSelectionViewModel.class;
    }

    @Override
    protected void setupViews() {
        // Initialize the adapter with a listener for when a slot is clicked
        adapter = new SlotTypeAdapter(slot -> {
            selectedSlot = slot;
        });
        binding.recyclerViewSlots.setLayoutManager(new GridLayoutManager(getContext(),2));
        binding.recyclerViewSlots.setAdapter(adapter);

        setupInitialTimes();
        updateDateTimeDisplays();

        // Set listeners for the date/time pickers and navigation buttons
        View.OnClickListener dateTimeClickListener = v -> showDateTimePicker(v.getId() == R.id.start_time_layout);
        binding.startTimeLayout.setOnClickListener(dateTimeClickListener);
        binding.endTimeLayout.setOnClickListener(dateTimeClickListener);

        binding.nextButton.setOnClickListener(v -> proceedToReview());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Trigger the initial data load for the default time
        updateDataForSelectedTime();
    }

    @Override
    protected void observeData() {
        // Observe the results from the ViewModel
        viewModel.getSlotsResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.recyclerViewSlots.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerViewSlots.setVisibility(View.VISIBLE);
                    if (result.data != null) {
                        adapter.setSlots(result.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error fetching slot status: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /**
     * Triggers the ViewModel to fetch all slots and their booking status for the currently selected time window.
     */
    private void updateDataForSelectedTime() {
        if (currentLot != null) {
            viewModel.fetchSlotsForLot(currentLot.getId(), startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis());
        }
    }

    private void setupInitialTimes() {
        // Set start time to the next nearest 30-minute mark
        int unroundedMinutes = startCalendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 30;
        startCalendar.add(Calendar.MINUTE, 30 - mod);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        // Set end time to 2 hours after start time, as shown in the UI design
        endCalendar.setTimeInMillis(startCalendar.getTimeInMillis());
        endCalendar.add(Calendar.HOUR_OF_DAY, 2);
    }

    private void updateDateTimeDisplays() {
        binding.startTimeText.setText(TimeUtils.formatDateTime(startCalendar.getTimeInMillis()));
        binding.endTimeText.setText(TimeUtils.formatDateTime(endCalendar.getTimeInMillis()));
    }

    private void showDateTimePicker(boolean isStartTime) {
        Calendar calendarToUpdate = isStartTime ? startCalendar : endCalendar;
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendarToUpdate.set(year, month, dayOfMonth);
            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                calendarToUpdate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarToUpdate.set(Calendar.MINUTE, minute);
                updateDateTimeDisplays();
                // After the time is changed, reload the slot list with the new availability
                updateDataForSelectedTime();
            }, calendarToUpdate.get(Calendar.HOUR_OF_DAY), calendarToUpdate.get(Calendar.MINUTE), false).show();
        }, calendarToUpdate.get(Calendar.YEAR), calendarToUpdate.get(Calendar.MONTH), calendarToUpdate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void proceedToReview() {
        if (selectedSlot == null) {
            Toast.makeText(getContext(), "Please select an available parking slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        long startTime = startCalendar.getTimeInMillis();
        long endTime = endCalendar.getTimeInMillis();

        // Validation checks before proceeding
        if (startTime >= endTime) {
            Toast.makeText(getContext(), "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startTime < System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Booking start time cannot be in the past.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endTime - startTime < TimeUnit.MINUTES.toMillis(30)) {
            Toast.makeText(getContext(), "Minimum booking duration is 30 minutes.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to the review screen with all the necessary data
        SlotSelectionFragmentDirections.ActionSlotSelectionFragmentToBookingReviewFragment action =
                SlotSelectionFragmentDirections.actionSlotSelectionFragmentToBookingReviewFragment(
                        currentLot, selectedSlot, startTime, endTime
                );
        Navigation.findNavController(requireView()).navigate((NavDirections) action);
    }
}