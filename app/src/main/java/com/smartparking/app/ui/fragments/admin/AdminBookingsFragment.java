package com.smartparking.app.ui.fragments.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.databinding.FragmentAdminBookingsBinding;
import com.smartparking.app.ui.adapters.AdminBookingsAdapter;
import com.smartparking.app.ui.base.BaseFragment;

public class AdminBookingsFragment extends BaseFragment<FragmentAdminBookingsBinding, AdminBookingsViewModel> {

    private AdminBookingsAdapter adapter;

    @NonNull
    @Override
    protected FragmentAdminBookingsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAdminBookingsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<AdminBookingsViewModel> getViewModelClass() {
        return AdminBookingsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // --- UPDATE THE ADAPTER INITIALIZATION ---
        adapter = new AdminBookingsAdapter(booking -> {
            // Navigate to the details screen
            AdminBookingsFragmentDirections.ActionAdminBookingsFragmentToAdminBookingDetailsFragment action =
                    AdminBookingsFragmentDirections.actionAdminBookingsFragmentToAdminBookingDetailsFragment(booking);
            Navigation.findNavController(requireView()).navigate(action);
        });

        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBookings.setAdapter(adapter);
        viewModel.fetchAllBookings();
    }

    @Override
    protected void observeData() {
        viewModel.getAllBookingsResult().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.data != null) {
                        adapter.setBookings(result.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}