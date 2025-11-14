package com.smartparking.app.ui.fragments.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.databinding.DialogAddEditSlotBinding;
import com.smartparking.app.databinding.FragmentManageSlotsBinding;
import com.smartparking.app.ui.adapters.AdminSlotsAdapter;
import com.smartparking.app.ui.base.BaseFragment;
import java.util.Locale;

public class ManageSlotsFragment extends BaseFragment<FragmentManageSlotsBinding, ManageSlotsViewModel> {

    private AdminSlotsAdapter adapter;
    private String lotId;
    private String lotName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lotId = ManageSlotsFragmentArgs.fromBundle(getArguments()).getLotId();
            lotName = ManageSlotsFragmentArgs.fromBundle(getArguments()).getLotName();
        }
    }

    @NonNull
    @Override
    protected FragmentManageSlotsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentManageSlotsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<ManageSlotsViewModel> getViewModelClass() {
        return ManageSlotsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setTitle("Slots for " + lotName);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Setup the adapter with listeners for Edit and Delete actions
        adapter = new AdminSlotsAdapter((slot, action) -> {
            if (action == AdminSlotsAdapter.SlotAction.EDIT) {
                showAddEditSlotDialog(slot);
            } else if (action == AdminSlotsAdapter.SlotAction.DELETE) {
                showDeleteConfirmationDialog(slot);
            }
        });
        binding.recyclerViewSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewSlots.setAdapter(adapter);

        // Set the listener for the floating action button to add a new slot
        binding.fabAddSlot.setOnClickListener(v -> showAddEditSlotDialog(null));

        // Initial data fetch
        viewModel.fetchSlotsForLot(lotId);
    }

    @Override
    protected void observeData() {
        // Observes the list of slots for the current lot
        viewModel.getSlotsResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.data != null) {
                        adapter.setSlots(result.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // Observes the result of any Create, Update, or Delete operation on a slot
        viewModel.getCudResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    // You could show a loading dialog/spinner here for CUD operations
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Operation successful!", Toast.LENGTH_SHORT).show();
                    viewModel.fetchSlotsForLot(lotId); // Refresh the list after a successful operation
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Operation failed: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showDeleteConfirmationDialog(Slot slot) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Slot")
                .setMessage("Are you sure you want to delete slot " + slot.getLabel() + "? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSlot(lotId, slot.getId());
                })
                .show();
    }

    /**
     * Shows a dialog to either add a new slot or edit an existing one.
     * @param slotToEdit The slot to edit, or null if adding a new slot.
     */
    private void showAddEditSlotDialog(@Nullable Slot slotToEdit) {
        DialogAddEditSlotBinding dialogBinding = DialogAddEditSlotBinding.inflate(LayoutInflater.from(getContext()));
        boolean isEditing = slotToEdit != null;

        // If editing, pre-populate the dialog fields with the existing slot's data
        if (isEditing) {
            dialogBinding.slotLabelEditText.setText(slotToEdit.getLabel());
            dialogBinding.slotLevelEditText.setText(String.valueOf(slotToEdit.getLevel()));
            dialogBinding.isActiveSwitch.setChecked(slotToEdit.isActive());
            switch (slotToEdit.getType().toLowerCase(Locale.ROOT)) {
                case "ev":
                    dialogBinding.radioEv.setChecked(true);
                    break;
                case "handicap":
                    dialogBinding.radioHandicap.setChecked(true);
                    break;
                default:
                    dialogBinding.radioRegular.setChecked(true);
                    break;
            }
        } else {
            dialogBinding.radioRegular.setChecked(true); // Default to "Regular" for new slots
        }

        // Create and show the dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEditing ? "Edit Slot" : "Add New Slot")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Read data from the dialog's input fields
                    String label = dialogBinding.slotLabelEditText.getText().toString().trim();
                    String levelStr = dialogBinding.slotLevelEditText.getText().toString().trim();

                    if (TextUtils.isEmpty(label) || TextUtils.isEmpty(levelStr)) {
                        Toast.makeText(getContext(), "Label and Level cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create or update the slot object
                    Slot slot = isEditing ? slotToEdit : new Slot();
                    slot.setLabel(label);
                    slot.setLevel(Integer.parseInt(levelStr));
                    slot.setActive(dialogBinding.isActiveSwitch.isChecked());

                    int selectedRadioId = dialogBinding.slotTypeRadioGroup.getCheckedRadioButtonId();
                    if (selectedRadioId == R.id.radio_ev) {
                        slot.setType("ev");
                    } else if (selectedRadioId == R.id.radio_handicap) {
                        slot.setType("handicap");
                    } else {
                        slot.setType("regular");
                    }

                    // Call the appropriate ViewModel method
                    if (isEditing) {
                        viewModel.updateSlot(lotId, slot);
                    } else {
                        viewModel.addSlot(lotId, slot);
                    }
                })
                .show();
    }
}