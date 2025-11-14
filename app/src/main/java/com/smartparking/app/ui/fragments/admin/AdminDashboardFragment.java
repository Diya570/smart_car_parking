package com.smartparking.app.ui.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.R;
import com.smartparking.app.databinding.FragmentAdminDashboardBinding;
import com.smartparking.app.databinding.ItemAdminActionContentBinding;
import com.smartparking.app.ui.base.BaseFragment;
import java.util.Locale;

public class AdminDashboardFragment extends BaseFragment<FragmentAdminDashboardBinding, AdminDashboardViewModel> {

    @NonNull
    @Override
    protected FragmentAdminDashboardBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAdminDashboardBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<AdminDashboardViewModel> getViewModelClass() {
        return AdminDashboardViewModel.class;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Trigger the data fetch when the view is created
        viewModel.fetchDashboardStats();
    }

    @Override
    protected void setupViews() {
        // Programmatically set up the content and icon for each quick action card
        setupActionCard(binding.cardManageLots, "Manage Lots", "Add, edit, or remove lots", R.drawable.ic_menu_lots_admin);
        setupActionCard(binding.cardViewBookings, "View Bookings", "See current & past bookings", R.drawable.ic_menu_bookings_admin);
        setupActionCard(binding.cardManageUsers, "User Management", "Manage user accounts", R.drawable.ic_menu_users_admin);
        setupActionCard(binding.cardReports, "Send Announcements", "Notify all users", R.drawable.ic_menu_announcement);
        setupActionCard(binding.cardScanQr, "Scan for Check-in", "Verify a user's booking", R.drawable.ic_menu_qr_scanner);

        // Set the click listeners to navigate to the correct destination
        binding.cardManageLots.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_manageLotsFragment));

        binding.cardViewBookings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_adminBookingsFragment));

        binding.cardManageUsers.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_usersFragment));

        binding.cardReports.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_announcementsFragment));

        binding.cardScanQr.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_adminDashboardFragment_to_scanQrFragment));
    }

    @Override
    protected void observeData() {
            // Observe Lot Count
            viewModel.getLotCount().observe(getViewLifecycleOwner(), result -> {
                if (result == null) return;
                switch (result.status) {
                    case LOADING:
                        binding.totalLotsText.setText("...");
                        break;
                    case SUCCESS:
                        binding.totalLotsText.setText(String.valueOf(result.data));
                        break;
                    case ERROR:
                        binding.totalLotsText.setText("Error");
                        break;
                }
            });

            // Observe User Count
            viewModel.getUserCount().observe(getViewLifecycleOwner(), result -> {
                if (result == null) return;
                switch (result.status) {
                    case LOADING:
                        binding.totalUsersText.setText("...");
                        break;
                    case SUCCESS:
                        binding.totalUsersText.setText(String.valueOf(result.data));
                        break;
                    case ERROR:
                        binding.totalUsersText.setText("Error");
                        break;
                }
            });

            // Observe Active Booking Count
            viewModel.getActiveBookingCount().observe(getViewLifecycleOwner(), result -> {
                if (result == null) return;
                switch (result.status) {
                    case LOADING:
                        binding.activeBookingsText.setText("...");
                        break;
                    case SUCCESS:
                        binding.activeBookingsText.setText(String.valueOf(result.data));
                        break;
                    case ERROR:
                        binding.activeBookingsText.setText("Error");
                        break;
                }
            });

            // Observe Total Revenue
            viewModel.getTotalRevenue().observe(getViewLifecycleOwner(), result -> {
                if (result == null) return;
                switch (result.status) {
                    case LOADING:
                        binding.revenueText.setText("...");
                        break;
                    case SUCCESS:
                        binding.revenueText.setText(String.format(new Locale("en", "IN"), "₹%,.0f", result.data));
                        break;
                    case ERROR:
                        binding.revenueText.setText("Error");
                        break;
                }
            });
        }
    private void setupActionCard(ViewGroup parent, String title, String subtitle, int iconRes) {
        ItemAdminActionContentBinding contentBinding = ItemAdminActionContentBinding.inflate(getLayoutInflater(), parent, true);
        contentBinding.titleText.setText(title);
        contentBinding.subtitleText.setText(subtitle);
        contentBinding.icon.setImageResource(iconRes);
    }
}