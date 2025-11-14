package com.smartparking.app.ui.fragments.user;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.R;
import com.smartparking.app.data.source.LocationDataSource;
import com.smartparking.app.databinding.FragmentLotsListBinding;
import com.smartparking.app.ui.adapters.LotsListAdapter;
import com.smartparking.app.ui.base.BaseFragment;

public class LotsListFragment extends BaseFragment<FragmentLotsListBinding, LotsListViewModel> {

    private LotsListAdapter adapter;
    private LocationDataSource locationDataSource;
    private Location currentUserLocation;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // If permission is granted, try again to fetch nearby lots
                    fetchCurrentUserLocationAndLoadLots();
                } else {
                    Toast.makeText(getContext(), R.string.location_permission_required, Toast.LENGTH_LONG).show();
                    // If permission is denied, automatically switch to and check the "Show All" chip
                    binding.chipAll.setChecked(true);
                }
            });

    @NonNull
    @Override
    protected FragmentLotsListBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLotsListBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<LotsListViewModel> getViewModelClass() {
        return LotsListViewModel.class;
    }

    @Override
    protected void setupViews() {
        adapter = new LotsListAdapter(lot -> {
            LotsListFragmentDirections.ActionLotsListFragmentToLotDetailsFragment action =
                    LotsListFragmentDirections.actionLotsListFragmentToLotDetailsFragment(lot);
            Navigation.findNavController(requireView()).navigate(action);
        });
        binding.recyclerViewLots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewLots.setAdapter(adapter);

        locationDataSource = new LocationDataSource(requireContext());

        // Set up the listener for the "Nearby" / "All" toggle
        binding.chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_nearby) {
                checkLocationPermission();
            } else if (checkedId == R.id.chip_all) {
                // When "All" is selected, no location is needed.
                adapter.setCurrentUserLocation(null); // Clear location to prevent distance calculation
                viewModel.loadParkingLots(false, null);
            }
        });

        // Trigger the initial data load when the screen first opens.
        // It will default to "Nearby" because that chip is checked in the XML.
        checkLocationPermission();
    }

    @Override
    protected void observeData() {
        viewModel.getLotsResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.recyclerViewLots.setVisibility(View.GONE);
                    binding.textEmptyState.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerViewLots.setVisibility(View.VISIBLE);
                    if (result.data != null && !result.data.isEmpty()) {
                        adapter.setLots(result.data);
                        binding.textEmptyState.setVisibility(View.GONE);
                    } else {
                        binding.textEmptyState.setText(R.string.no_lots_found);
                        binding.textEmptyState.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textEmptyState.setText("Error: " + result.message);
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    /**
     * Checks if location permission is granted. If so, fetches location. If not, requests permission.
     */
    private void checkLocationPermission() {
        if (getContext() != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentUserLocationAndLoadLots();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Uses the LocationDataSource to get a single location update and then triggers the ViewModel to load nearby lots.
     */
    private void fetchCurrentUserLocationAndLoadLots() {
        locationDataSource.getCurrentLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                this.currentUserLocation = location;
                adapter.setCurrentUserLocation(location);
                // Only load nearby lots if the "Nearby" chip is the one currently selected.
                if (binding.chipNearby.isChecked()) {
                    viewModel.loadParkingLots(true, this.currentUserLocation);
                }
                // Stop observing to prevent re-fetching on configuration changes (like screen rotation).
                locationDataSource.getCurrentLocation().removeObservers(getViewLifecycleOwner());
            }
        });
    }
}