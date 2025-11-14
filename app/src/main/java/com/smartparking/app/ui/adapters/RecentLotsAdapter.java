package com.smartparking.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.databinding.ItemRecentLotBinding;
import java.util.ArrayList;
import java.util.List;

public class RecentLotsAdapter extends RecyclerView.Adapter<RecentLotsAdapter.RecentLotViewHolder> {

    private final List<ParkingLot> lots = new ArrayList<>();
    private final OnLotClickListener clickListener;

    public interface OnLotClickListener {
        void onLotClicked(ParkingLot lot);
    }

    public RecentLotsAdapter(OnLotClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLots(List<ParkingLot> newLots) {
        this.lots.clear();
        this.lots.addAll(newLots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentLotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentLotBinding binding = ItemRecentLotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentLotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentLotViewHolder holder, int position) {
        ParkingLot lot = lots.get(position);
        holder.bind(lot);
        holder.itemView.setOnClickListener(v -> clickListener.onLotClicked(lot));
    }

    @Override
    public int getItemCount() {
        return lots.size();
    }

    static class RecentLotViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecentLotBinding binding;

        public RecentLotViewHolder(ItemRecentLotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ParkingLot lot) {
            binding.lotNameText.setText(lot.getName());
            binding.lotAddressText.setText(lot.getAddress());
        }
    }
}