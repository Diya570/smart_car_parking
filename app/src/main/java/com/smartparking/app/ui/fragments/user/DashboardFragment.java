package com.smartparking.app.ui.fragments.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.ParkingLotWithAvailability;
import com.smartparking.app.databinding.FragmentDashboardBinding;
import com.smartparking.app.ui.adapters.RecentLotsAdapter;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.TimeUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardFragment extends BaseFragment<FragmentDashboardBinding, DashboardViewModel> {

    private RecentLotsAdapter recentLotsAdapter;

    @NonNull
    @Override
    protected FragmentDashboardBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDashboardBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<DashboardViewModel> getViewModelClass() {
        return DashboardViewModel.class;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Data fetching is now handled automatically by the ViewModel's constructor
    }

    @Override
    protected void setupViews() {
        // Set up the RecyclerView for the "Book Again" list
        recentLotsAdapter = new RecentLotsAdapter(lot -> {
            DashboardFragmentDirections.ActionDashboardFragmentToLotDetailsFragment action =
                    DashboardFragmentDirections.actionDashboardFragmentToLotDetailsFragment(lot);
            Navigation.findNavController(requireView()).navigate((NavDirections) action);
        });
        binding.recyclerRecentLots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerRecentLots.setAdapter(recentLotsAdapter);
    }

    @Override
    protected void observeData() {
        // --- OBSERVERS FOR NEW SMART FEATURES ---

        // Observes the rush hour status
        viewModel.getIsRushHour().observe(getViewLifecycleOwner(), isRush -> {
            binding.cardRushHour.setVisibility(isRush ? View.VISIBLE : View.GONE);
        });

        // Observes the user's favorite lot
        viewModel.getFavoriteLot().observe(getViewLifecycleOwner(), favoriteLot -> {
            if (favoriteLot != null) {
                binding.cardFavoriteSpot.setVisibility(View.VISIBLE);
                binding.favoriteLotName.setText(favoriteLot.getName());
                binding.buttonBookFavorite.setOnClickListener(v -> {
                    DashboardFragmentDirections.ActionDashboardFragmentToLotDetailsFragment action =
                            DashboardFragmentDirections.actionDashboardFragmentToLotDetailsFragment(favoriteLot);
                    Navigation.findNavController(v).navigate((NavDirections) action);
                });
            } else {
                binding.cardFavoriteSpot.setVisibility(View.GONE);
            }
        });

        // Observes the live availability ticker data
        viewModel.getAvailabilityTicker().observe(getViewLifecycleOwner(), tickerItems -> {
            binding.tickerLayout.removeAllViews(); // Clear old ticker items
            if (tickerItems != null && !tickerItems.isEmpty()) {
                binding.tickerLabel.setVisibility(View.VISIBLE);
                binding.tickerLayout.setVisibility(View.VISIBLE);
                // Sort the list for a consistent display order
                tickerItems.sort(Comparator.comparing(item -> item.getParkingLot().getName()));
                for (ParkingLotWithAvailability item : tickerItems) {
                    TextView tv = new TextView(getContext());
                    int available = item.getParkingLot().getTotalSlots() - item.getOccupiedSlots();
                    String availabilityText = available > 0 ? available + " spots left" : "FULL";
                    tv.setText(String.format(Locale.US, "• %s: %s", item.getParkingLot().getName(), availabilityText));
                    tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                    binding.tickerLayout.addView(tv);
                }
            } else {
                binding.tickerLabel.setVisibility(View.GONE);
                binding.tickerLayout.setVisibility(View.GONE);
            }
        });

        // Observes the user's next upcoming booking
        viewModel.getNextBooking().observe(getViewLifecycleOwner(), booking -> {
            if (booking != null) {
                binding.cardNextBooking.setVisibility(View.VISIBLE);
                binding.nextBookingTime.setText(TimeUtils.formatDateTime(booking.getStartTime()));
                String lotName = booking.getLotName() != null ? booking.getLotName() : "Lot ID: " + booking.getLotId();
                String slotLabel = booking.getSlotLabel() != null ? booking.getSlotLabel() : "Slot ID: " + booking.getSlotId();
                binding.nextBookingLotName.setText(lotName);
                binding.nextBookingSlot.setText("Slot: " + slotLabel);

                // Make the card clickable to navigate to the QR code screen
                binding.cardNextBooking.setOnClickListener(v -> {
                    DashboardFragmentDirections.ActionDashboardFragmentToBookingDetailsFragment action =
                            DashboardFragmentDirections.actionDashboardFragmentToBookingDetailsFragment(booking);
                    Navigation.findNavController(v).navigate(action);
                });
            } else {
                binding.cardNextBooking.setVisibility(View.GONE);
                binding.cardNextBooking.setOnClickListener(null); // Remove listener if no booking
            }
        });

        // This observer now just handles the "Book Again" list
        viewModel.getUserBookings().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.status == com.smartparking.app.data.source.Result.Status.SUCCESS && result.data != null) {
                List<String> recentLotIds = result.data.stream()
                        .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                        .map(Booking::getLotId)
                        .distinct()
                        .limit(3)
                        .collect(Collectors.toList());

                if (!recentLotIds.isEmpty()) {
                    viewModel.getAllLots().observe(getViewLifecycleOwner(), lotsResult -> {
                        if (lotsResult != null && lotsResult.status == com.smartparking.app.data.source.Result.Status.SUCCESS && lotsResult.data != null) {
                            List<ParkingLot> recentLots = lotsResult.data.stream()
                                    .filter(p -> p.getId() != null && recentLotIds.contains(p.getId()))
                                    .collect(Collectors.toList());
                            updateRecentLotsList(recentLots);
                        }
                    });
                } else {
                    updateRecentLotsList(new ArrayList<>());
                }
            }
        });
    }

    // This method is now only for the "Book Again" list
    private void updateRecentLotsList(List<ParkingLot> lots) {
        if (lots != null && !lots.isEmpty()) {
            binding.recentLotsLabel.setVisibility(View.VISIBLE);
            binding.recyclerRecentLots.setVisibility(View.VISIBLE);
            recentLotsAdapter.setLots(lots);
        } else {
            binding.recentLotsLabel.setVisibility(View.GONE);
            binding.recyclerRecentLots.setVisibility(View.GONE);
        }
    }
}