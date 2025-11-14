package com.smartparking.app.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.GeoPoint;
import com.smartparking.app.R;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.ParkingLotWithAvailability;
import com.smartparking.app.databinding.ItemParkingLotBinding;
import com.smartparking.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LotsListAdapter extends RecyclerView.Adapter<LotsListAdapter.LotViewHolder> {

    private final List<ParkingLotWithAvailability> lots = new ArrayList<>();
    private final OnLotClickListener clickListener;
    private Location currentUserLocation;

    public interface OnLotClickListener {
        void onLotClicked(ParkingLot lot);
    }

    public LotsListAdapter(OnLotClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCurrentUserLocation(Location location) {
        this.currentUserLocation = location;
    }

    public void setLots(List<ParkingLotWithAvailability> newLots) {
        this.lots.clear();
        this.lots.addAll(newLots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemParkingLotBinding binding = ItemParkingLotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LotViewHolder holder, int position) {
        ParkingLotWithAvailability lotWithAvailability = lots.get(position);
        holder.bind(lotWithAvailability, currentUserLocation, clickListener);
    }

    @Override
    public int getItemCount() {
        return lots.size();
    }

    static class LotViewHolder extends RecyclerView.ViewHolder {
        private final ItemParkingLotBinding binding;

        public LotViewHolder(ItemParkingLotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ParkingLotWithAvailability lotWithAvailability, Location currentUserLocation, OnLotClickListener clickListener) {
            ParkingLot lot = lotWithAvailability.getParkingLot();
            Context context = itemView.getContext();

            // Bind all the text views for lot info
            binding.lotNameText.setText(lot.getName());
            binding.lotAddressText.setText(lot.getAddress());

            if (lot.getPricing() != null) {
                String priceString = CurrencyUtils.formatCurrency(lot.getPricing().getPerHour(), lot.getPricing().getCurrency()) + "/hr";
                binding.priceText.setText(priceString);
            }

            if (currentUserLocation != null && lot.getLocation() != null) {
                GeoPoint lotLocationPoint = lot.getLocation();
                Location lotLocation = new Location("");
                lotLocation.setLatitude(lotLocationPoint.getLatitude());
                lotLocation.setLongitude(lotLocationPoint.getLongitude());
                float distanceInMeters = currentUserLocation.distanceTo(lotLocation);
                float distanceInKm = distanceInMeters / 1000;
                binding.distanceText.setText(String.format(Locale.getDefault(), "%.1f km", distanceInKm));
            } else {
                binding.distanceText.setText("");
            }

            int availableSlots = lot.getTotalSlots() - lotWithAvailability.getOccupiedSlots();
            if (availableSlots <= 0) {
                binding.availabilityText.setText("FULL");
                setTagColor(context, R.color.status_red_text, R.color.status_red_bg);
            } else {
                binding.availabilityText.setText(String.format(Locale.getDefault(), "%d SPOTS AVAILABLE", availableSlots));
                if (availableSlots < 10) {
                    setTagColor(context, R.color.status_orange_text, R.color.status_orange_bg);
                } else {
                    setTagColor(context, R.color.status_green_text, R.color.status_green_bg);
                }
            }

            // Set click listener for the main info area to navigate to details
            binding.infoLayout.setOnClickListener(v -> clickListener.onLotClicked(lot));

            // Set click listener for the directions button
            if (lot.getMapLink() != null && !lot.getMapLink().isEmpty()) {
                binding.directionsButton.setVisibility(View.VISIBLE);
                binding.directionsButton.setOnClickListener(v -> {
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(lot.getMapLink()));
                    itemView.getContext().startActivity(mapIntent);
                });
            } else {
                binding.directionsButton.setVisibility(View.GONE);
            }
        }

        private void setTagColor(Context context, int textColorRes, int bgColorRes) {
            binding.availabilityText.setTextColor(ContextCompat.getColor(context, textColorRes));
            GradientDrawable background = (GradientDrawable) binding.availabilityText.getBackground();
            background.setColor(ContextCompat.getColor(context, bgColorRes));
        }
    }
}