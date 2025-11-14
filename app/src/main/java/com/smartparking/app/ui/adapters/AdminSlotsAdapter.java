package com.smartparking.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.databinding.ItemAdminSlotBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminSlotsAdapter extends RecyclerView.Adapter<AdminSlotsAdapter.AdminSlotViewHolder> {

    private final List<Slot> slots = new ArrayList<>();
    private final OnSlotActionClickListener listener;

    public enum SlotAction { EDIT, DELETE }

    public interface OnSlotActionClickListener {
        void onActionClicked(Slot slot, SlotAction action);
    }

    public AdminSlotsAdapter(OnSlotActionClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<Slot> newSlots) {
        this.slots.clear();
        this.slots.addAll(newSlots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminSlotBinding binding = ItemAdminSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminSlotViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminSlotViewHolder holder, int position) {
        holder.bind(slots.get(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class AdminSlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminSlotBinding binding;
        private final OnSlotActionClickListener listener;

        public AdminSlotViewHolder(ItemAdminSlotBinding binding, OnSlotActionClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Slot slot) {
            binding.slotLabelText.setText(slot.getLabel());
            binding.slotInfoText.setText(String.format("Level: %d, Type: %s", slot.getLevel(), slot.getType()));
            binding.statusText.setText(slot.isActive() ? "Active" : "Inactive");
            binding.editButton.setOnClickListener(v -> listener.onActionClicked(slot, SlotAction.EDIT));
            binding.deleteButton.setOnClickListener(v -> listener.onActionClicked(slot, SlotAction.DELETE));
        }
    }
}