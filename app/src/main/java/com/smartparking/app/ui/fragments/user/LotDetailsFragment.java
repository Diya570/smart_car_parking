package com.smartparking.app.ui.fragments.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.databinding.FragmentLotDetailsBinding;
import com.smartparking.app.ui.base.BaseFragment;
import java.util.Locale;
import timber.log.Timber;

public class LotDetailsFragment extends BaseFragment<FragmentLotDetailsBinding, LotDetailsViewModel> {

    private ParkingLot currentLot;
    private static final String DEBUG_TAG = "PARKING_APP_TRACE";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLot = LotDetailsFragmentArgs.fromBundle(getArguments()).getParkingLot();
        }
    }

    @NonNull
    @Override
    protected FragmentLotDetailsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLotDetailsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<LotDetailsViewModel> getViewModelClass() {
        return LotDetailsViewModel.class;
    }

    @Override
    protected void setupViews() {
        if (currentLot != null) {
            displayLotDetails();
            viewModel.fetchOccupiedSlotsCount(currentLot.getId());
        }

        binding.selectSlotButton.setOnClickListener(v -> {
            Timber.tag(DEBUG_TAG).d("LotDetailsFragment: 'Select Slot' button CLICKED.");
            if (currentLot != null) {
                LotDetailsFragmentDirections.ActionLotDetailsFragmentToSlotSelectionFragment action =
                        LotDetailsFragmentDirections.actionLotDetailsFragmentToSlotSelectionFragment(currentLot);
                Navigation.findNavController(v).navigate(action);
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    @Override
    protected void observeData() {
        viewModel.getOccupiedSlotsCount().observe(getViewLifecycleOwner(), result -> {
            if (result == null || currentLot == null) return;
            switch (result.status) {
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    int occupiedCount = result.data != null ? result.data : 0;
                    int availableCount = currentLot.getTotalSlots() - occupiedCount;
                    //binding.availabilityText.setText(String.format(Locale.US, "%d / %d Available", availableCount, currentLot.getTotalSlots()));
                    binding.selectSlotButton.setEnabled(availableCount > 0);
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error fetching availability: " + result.message, Toast.LENGTH_SHORT).show();
                    //binding.availabilityText.setText("Availability: Error");
                    binding.selectSlotButton.setEnabled(false);
                    break;
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    //binding.availabilityText.setText("Availability: Loading...");
                    binding.selectSlotButton.setEnabled(false);
                    break;
            }
        });
    }

    private void displayLotDetails() {
        if (currentLot != null) {
            binding.toolbar.setTitle(currentLot.getName());
            binding.lotAddressText.setText(currentLot.getAddress());
        }
    }
}