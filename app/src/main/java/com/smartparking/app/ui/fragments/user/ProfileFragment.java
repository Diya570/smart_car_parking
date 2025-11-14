package com.smartparking.app.ui.fragments.user;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartparking.app.R;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.model.Vehicle;
import com.smartparking.app.databinding.DialogEditProfileBinding;
import com.smartparking.app.databinding.FragmentProfileBinding;
import com.smartparking.app.ui.base.BaseFragment;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding, ProfileViewModel> {

    private User currentUser;

    @NonNull
    @Override
    protected FragmentProfileBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<ProfileViewModel> getViewModelClass() {
        return ProfileViewModel.class;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the notification switch state every time the user returns to this screen
        updateNotificationSwitchState();
    }

    @Override
    protected void setupViews() {
        // --- Click Listeners ---
        binding.logoutButton.setOnClickListener(v -> {
            viewModel.logout();
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.auth_flow);
        });

        binding.editProfileButton.setOnClickListener(v -> {
            if (currentUser != null) {
                showEditProfileDialog();
            } else {
                Toast.makeText(getContext(), "User data not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.notificationButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_notificationsFragment);
        });

        // --- Dark Mode Switch Logic ---
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        binding.darkModeSwitch.setChecked(currentNightMode == Configuration.UI_MODE_NIGHT_YES);
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // --- Notification Switch Logic ---
        updateNotificationSwitchState();
        binding.notificationSwitch.setOnClickListener(v -> openNotificationSettings());
    }

    @Override
    protected void observeData() {
        // Observer for fetching the user's profile data
        viewModel.getUserDetails().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.data != null) {
                        currentUser = result.data;
                        updateUiWithUserData(currentUser);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // Observer for the result of an update operation
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    // Can show a loading spinner in the dialog if desired
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    viewModel.fetchUserDetails(); // Refresh the profile data to show the changes
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Update failed: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void updateUiWithUserData(User user) {
        binding.nameText.setText(user.getDisplayName());
        binding.emailText.setText(user.getEmail());


            // Fallback to placeholder API if no photo URL is set
            Glide.with(this)
                    .load("https://i.pravatar.cc/150?u=" + user.getEmail())
                    .placeholder(R.drawable.ic_menu_profile)
                    .into(binding.profileImage);


        Vehicle vehicle = user.getVehicle();
        if (vehicle != null) {
            binding.vehicleModelText.setText(vehicle.getModel() != null && !vehicle.getModel().isEmpty() ? vehicle.getModel() : "Not specified");
            binding.vehiclePlateText.setText("License Plate: " + (vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "N/A"));
        } else {
            binding.vehicleModelText.setText("No vehicle added");
            binding.vehiclePlateText.setText("Please add your vehicle details");
        }
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(getContext()));
        dialogBinding.nameEditText.setText(currentUser.getDisplayName());
        if (currentUser.getVehicle() != null) {
            dialogBinding.vehicleModelEditText.setText(currentUser.getVehicle().getModel());
            dialogBinding.vehiclePlateEditText.setText(currentUser.getVehicle().getPlateNumber());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = dialogBinding.nameEditText.getText().toString().trim();
                    String newModel = dialogBinding.vehicleModelEditText.getText().toString().trim();
                    String newPlate = dialogBinding.vehiclePlateEditText.getText().toString().trim();

                    if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPlate)) {
                        Toast.makeText(getContext(), "Name and Plate Number cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.updateProfile(newName, newModel, newPlate);
                })
                .show();
    }

    private void updateNotificationSwitchState() {
        if (getContext() != null) {
            boolean areNotificationsEnabled = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
            binding.notificationSwitch.setChecked(areNotificationsEnabled);
        }
    }

    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        }
        startActivity(intent);
    }
}