package com.smartparking.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.databinding.ItemAdminLotBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminLotsAdapter extends RecyclerView.Adapter<AdminLotsAdapter.AdminLotViewHolder> {

    private final List<ParkingLot> lots = new ArrayList<>();
    private final OnLotActionClickListener listener;

    // THE CHANGE IS HERE: Replaced MANAGE_SLOTS with DELETE
    public enum LotAction { EDIT, DELETE }

    public interface OnLotActionClickListener {
        void onActionClicked(ParkingLot lot, LotAction action);
    }

    public AdminLotsAdapter(OnLotActionClickListener listener) {
        this.listener = listener;
    }

    public void setLots(List<ParkingLot> newLots) {
        this.lots.clear();
        this.lots.addAll(newLots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminLotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminLotBinding binding = ItemAdminLotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminLotViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminLotViewHolder holder, int position) {
        holder.bind(lots.get(position));
    }

    @Override
    public int getItemCount() {
        return lots.size();
    }

    static class AdminLotViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminLotBinding binding;
        private final OnLotActionClickListener listener;

        public AdminLotViewHolder(ItemAdminLotBinding binding, OnLotActionClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(ParkingLot lot) {
            binding.lotNameText.setText(lot.getName());
            binding.lotAddressText.setText(lot.getAddress());
            binding.lotInfoText.setText(String.format("Total Slots: %d", lot.getTotalSlots()));

            binding.editButton.setOnClickListener(v -> listener.onActionClicked(lot, LotAction.EDIT));
            // THE CHANGE IS HERE: The new delete_button now triggers the DELETE action
            binding.deleteButton.setOnClickListener(v -> listener.onActionClicked(lot, LotAction.DELETE));
        }
    }
}