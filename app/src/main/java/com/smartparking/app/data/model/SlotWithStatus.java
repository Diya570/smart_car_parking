package com.smartparking.app.data.model;

public class SlotWithStatus {
    private final Slot slot;
    private String status; // "Available", "Booked"

    public SlotWithStatus(Slot slot, String status) {
        this.slot = slot;
        this.status = status;
    }

    public Slot getSlot() {
        return slot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}