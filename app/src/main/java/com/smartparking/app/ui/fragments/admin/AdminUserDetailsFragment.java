package com.smartparking.app.ui.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.model.Vehicle;
import com.smartparking.app.databinding.FragmentAdminUserDetailsBinding;
import com.smartparking.app.ui.adapters.AdminBookingsAdapter;
import com.smartparking.app.ui.base.BaseFragment;

public class AdminUserDetailsFragment extends BaseFragment<FragmentAdminUserDetailsBinding, AdminUserDetailsViewModel> {

    private User user;
    private AdminBookingsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = AdminUserDetailsFragmentArgs.fromBundle(getArguments()).getUser();
        }
    }

    @NonNull
    @Override
    protected FragmentAdminUserDetailsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAdminUserDetailsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<AdminUserDetailsViewModel> getViewModelClass() {
        return AdminUserDetailsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        adapter = new AdminBookingsAdapter(booking -> {
            // Admin can click on a booking from this list to see even more details
            AdminUserDetailsFragmentDirections.ActionAdminUserDetailsFragmentToAdminBookingDetailsFragment action =
                    AdminUserDetailsFragmentDirections.actionAdminUserDetailsFragmentToAdminBookingDetailsFragment(booking);
            Navigation.findNavController(requireView()).navigate(action);
        });
        binding.recyclerViewUserBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewUserBookings.setAdapter(adapter);

        if (user != null) {
            binding.toolbar.setTitle(user.getDisplayName());
            binding.userNameText.setText(user.getDisplayName());
            binding.userEmailText.setText(user.getEmail());
            Vehicle v = user.getVehicle();
            if (v != null) {
                binding.vehicleText.setText("Vehicle: " + v.getModel() + " (" + v.getPlateNumber() + ")");
            } else {
                binding.vehicleText.setText("Vehicle: Not provided");
            }
            viewModel.fetchBookingsForUser(user.getUid());
        }
    }

    @Override
    protected void observeData() {
        viewModel.getUserBookings().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.status == com.smartparking.app.data.source.Result.Status.SUCCESS) {
                if (result.data != null) {
                    adapter.setBookings(result.data);
                }
            } else if (result.status == com.smartparking.app.data.source.Result.Status.ERROR) {
                Toast.makeText(getContext(), "Error fetching booking history: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}