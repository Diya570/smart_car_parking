package com.smartparking.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.databinding.ItemSlotBinding;
import java.util.ArrayList;
import java.util.List;

public class SlotsAdapter extends RecyclerView.Adapter<SlotsAdapter.SlotViewHolder> {

    private final List<Slot> slots = new ArrayList<>();
    private final OnSlotClickListener clickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnSlotClickListener {
        void onSlotClicked(Slot slot);
    }

    public SlotsAdapter(OnSlotClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setSlots(List<Slot> newSlots) {
        this.slots.clear();
        this.slots.addAll(newSlots);
        notifyDataSetChanged();
    }

    public void setSelectedSlot(Slot slot) {
        int oldPosition = selectedPosition;
        selectedPosition = slots.indexOf(slot);
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSlotBinding binding = ItemSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        Slot slot = slots.get(position);
        holder.bind(slot, position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            clickListener.onSlotClicked(slot);
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemSlotBinding binding;

        public SlotViewHolder(ItemSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Slot slot, boolean isSelected) {
            binding.slotLabelText.setText(slot.getLabel());

            // Change background based on selection state
            if (isSelected) {
                binding.getRoot().setCardBackgroundColor(
                        // THE FIX IS HERE: Removed "_light" from the color name
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_primaryContainer));
            } else {
                binding.getRoot().setCardBackgroundColor(
                        // THE FIX IS HERE: Removed "_light" from the color name
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_surfaceVariant));
            }
        }
    }
}