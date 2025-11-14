package com.smartparking.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.smartparking.app.R;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.data.model.SlotWithStatus;
import com.smartparking.app.databinding.ItemSlotTypeBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlotTypeAdapter extends RecyclerView.Adapter<SlotTypeAdapter.SlotTypeViewHolder> {

    private final List<SlotWithStatus> slots = new ArrayList<>();
    private final OnSlotClickListener clickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnSlotClickListener {
        void onSlotClicked(Slot slot);
    }

    public SlotTypeAdapter(OnSlotClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setSlots(List<SlotWithStatus> newSlots) {
        this.slots.clear();
        this.slots.addAll(newSlots);
        this.selectedPosition = RecyclerView.NO_POSITION; // Reset selection when new data is loaded
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSlotTypeBinding binding = ItemSlotTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SlotTypeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotTypeViewHolder holder, int position) {
        SlotWithStatus slotWithStatus = slots.get(position);
        holder.bind(slotWithStatus, position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            // Only allow clicks if the slot is not already booked
            if ("Available".equals(slotWithStatus.getStatus())) {
                clickListener.onSlotClicked(slotWithStatus.getSlot());
                int previousPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                // Update the UI for both the previously selected item and the new one
                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class SlotTypeViewHolder extends RecyclerView.ViewHolder {
        private final ItemSlotTypeBinding binding;

        public SlotTypeViewHolder(ItemSlotTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SlotWithStatus slotWithStatus, boolean isSelected) {
            Slot slot = slotWithStatus.getSlot();
            Context context = itemView.getContext();
            binding.slotLabelText.setText("Lot " + slot.getLabel());

            // Set icon and text based on slot type, matching the UI design [cite: 28]
            String type = slot.getType().toLowerCase(Locale.ROOT);
            switch (type) {
                case "ev":
                case "ev charging":
                    binding.slotTypeText.setText("EV Charging");
                    binding.slotIcon.setImageResource(R.drawable.ic_menu_ev_charging);
                    break;
                case "handicap":
                    binding.slotTypeText.setText("Handicap");
                    binding.slotIcon.setImageResource(R.drawable.ic_menu_handicap);
                    break;
                default:
                    binding.slotTypeText.setText("Regular");
                    binding.slotIcon.setImageResource(R.drawable.ic_menu_car);
                    break;
            }

            // Update UI based on the slot's status ("Booked", "Selected", or "Available")
            if ("Booked".equals(slotWithStatus.getStatus())) {
                binding.statusText.setText("Booked");
                binding.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                binding.statusText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.brand_red));
                itemView.setEnabled(false);
                binding.getRoot().setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_gray_bg));
                binding.getRoot().setChecked(false);
            } else {
                itemView.setEnabled(true);
                if (isSelected) {
                    binding.getRoot().setChecked(true);
                    binding.statusText.setText("Selected");
                    binding.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    binding.statusText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.brand_blue));
                } else {
                    binding.getRoot().setChecked(false);
                    binding.statusText.setText("Available");
                    binding.statusText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    binding.statusText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.brand_green));
                }
            }

        }
    }
}