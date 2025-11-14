package com.smartparking.app.ui.fragments.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.firebase.geofire.GeoFireUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.GeoPoint;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.Pricing;
import com.smartparking.app.databinding.DialogAddEditLotBinding;
import com.smartparking.app.databinding.FragmentManageLotsBinding;
import com.smartparking.app.ui.adapters.AdminLotsAdapter;
import com.smartparking.app.ui.base.BaseFragment;

public class ManageLotsFragment extends BaseFragment<FragmentManageLotsBinding, ManageLotViewModel> {

    private AdminLotsAdapter adapter;

    @NonNull
    @Override
    protected FragmentManageLotsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentManageLotsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<ManageLotViewModel> getViewModelClass() {
        return ManageLotViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        adapter = new AdminLotsAdapter((lot, action) -> {
            if (action == AdminLotsAdapter.LotAction.EDIT) {
                showAddEditLotDialog(lot);
            } else if (action == AdminLotsAdapter.LotAction.DELETE) {
                // Show a confirmation dialog before deleting
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Lot?")
                        .setMessage("Are you sure you want to delete '" + lot.getName() + "'? This will also delete all of its slots and cannot be undone.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteLot(lot))
                        .show();
            }
        });

        binding.recyclerViewLots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewLots.setAdapter(adapter);

        // Set the listener for the floating action button to add a new lot
        binding.fabAddLot.setOnClickListener(v -> showAddEditLotDialog(null));

        // Initial data fetch
        viewModel.fetchAllLots();
    }

    @Override
    protected void observeData() {
        // Observes the list of all parking lots
        viewModel.getAllLotsResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.data != null) {
                        adapter.setLots(result.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error fetching lots: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // Observes the result of any Create, Update, or Delete operation
        viewModel.getCudResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            // After any successful operation, show a toast and refresh the list
            if (result.status == com.smartparking.app.data.source.Result.Status.SUCCESS) {
                Toast.makeText(getContext(), "Operation Successful!", Toast.LENGTH_SHORT).show();
                viewModel.fetchAllLots();
            } else if (result.status == com.smartparking.app.data.source.Result.Status.ERROR) {
                Toast.makeText(getContext(), "Operation Failed: " + result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shows a dialog to either add a new lot or edit an existing one.
     * @param lotToEdit The lot to edit, or null if adding a new lot.
     */
    private void showAddEditLotDialog(@Nullable ParkingLot lotToEdit) {
        DialogAddEditLotBinding dialogBinding = DialogAddEditLotBinding.inflate(LayoutInflater.from(getContext()));
        boolean isEditing = lotToEdit != null;

        // If we are editing, pre-fill the dialog with the existing lot's data
        if (isEditing) {
            dialogBinding.lotNameEditText.setText(lotToEdit.getName());
            dialogBinding.lotAddressEditText.setText(lotToEdit.getAddress());
            dialogBinding.lotTotalSlotsEditText.setText(String.valueOf(lotToEdit.getTotalSlots()));
            if (lotToEdit.getLocation() != null) {
                dialogBinding.lotLatEditText.setText(String.valueOf(lotToEdit.getLocation().getLatitude()));
                dialogBinding.lotLngEditText.setText(String.valueOf(lotToEdit.getLocation().getLongitude()));
            }

            dialogBinding.lotMapLinkEditText.setText(lotToEdit.getMapLink());
            // Hide the total slots field when editing to prevent accidental slot deletion/creation
            dialogBinding.lotTotalSlotsEditText.setEnabled(!isEditing);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEditing ? "Edit Parking Lot" : "Add New Parking Lot")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Read all data from the dialog fields
                    String name = dialogBinding.lotNameEditText.getText().toString().trim();
                    String address = dialogBinding.lotAddressEditText.getText().toString().trim();
                    String totalSlotsStr = dialogBinding.lotTotalSlotsEditText.getText().toString().trim();
                    String latStr = dialogBinding.lotLatEditText.getText().toString().trim();
                    String lngStr = dialogBinding.lotLngEditText.getText().toString().trim();
                    String mapLink = dialogBinding.lotMapLinkEditText.getText().toString().trim();


                    // Validate that all fields are filled
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(address) || TextUtils.isEmpty(totalSlotsStr) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr)) {
                        Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);

                        if (lat < -90 || lat > 90) {
                            Toast.makeText(getContext(), "Invalid Latitude. Must be between -90 and 90.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (lng < -180 || lng > 180) {
                            Toast.makeText(getContext(), "Invalid Longitude. Must be between -180 and 180.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Create a new lot object or update the existing one
                        ParkingLot lot = isEditing ? lotToEdit : new ParkingLot();
                        lot.setName(name);
                        lot.setAddress(address);
                        lot.setTotalSlots(Integer.parseInt(totalSlotsStr));
                        lot.setLocation(new GeoPoint(lat, lng));
                        lot.setGeohash(GeoFireUtils.getGeoHashForLocation(new com.firebase.geofire.GeoLocation(lat, lng)));
                        lot.setMapLink(mapLink);
                        // If creating a new lot, set some default pricing

                        if (!isEditing) {
                            Pricing defaultPricing = new Pricing();
                            defaultPricing.setCurrency("INR");
                            defaultPricing.setPerHour(50.0);
                            defaultPricing.setMinBillableMinutes(30);
                            defaultPricing.setRoundingToMinutes(15);
                            lot.setPricing(defaultPricing);
                        }

                        // Call the appropriate ViewModel method
                        if (isEditing) {
                            viewModel.updateLot(lot);
                        } else {
                            viewModel.addLot(lot);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid number format for slots, latitude, or longitude.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}