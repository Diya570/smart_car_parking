package com.smartparking.app.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Booking implements Serializable {
    private String bookingId;
    private String userId;
    private String lotId;
    private String slotId;
    private long startTime; // Epoch milliseconds UTC
    private long endTime; // Epoch milliseconds UTC
    private String status; // "confirmed", "checkedIn", "canceled", "completed"
    private List<String> occupiedHours;
    private double cost;
    private String entryToken; // Random UUID for QR code verification
    private String otp; // 6-digit random number for manual verification
    @ServerTimestamp
    private Date createdAt;
    private Date canceledAt;
    private Date checkedInAt;
    private String lotName;     // <-- ADD THIS FIELD
    private String slotLabel;

    public Booking() {}

    // --- Getters and Setters ---
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public String getEntryToken() { return entryToken; }
    public void setEntryToken(String entryToken) { this.entryToken = entryToken; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getCanceledAt() { return canceledAt; }
    public void setCanceledAt(Date canceledAt) { this.canceledAt = canceledAt; }
    public Date getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Date checkedInAt) { this.checkedInAt = checkedInAt; }

    public List<String> getOccupiedHours() { return occupiedHours; } // <-- ADD THIS GETTER
    public void setOccupiedHours(List<String> occupiedHours) { this.occupiedHours = occupiedHours; } // <-- ADD THIS SETTER

    public String getLotName() { return lotName; }         // <-- ADD THIS GETTER
    public void setLotName(String lotName) { this.lotName = lotName; } // <-- ADD THIS SETTER

    public String getSlotLabel() { return slotLabel; }     // <-- ADD THIS GETTER
    public void setSlotLabel(String slotLabel) { this.slotLabel = slotLabel; } // <-- ADD THIS SETTER
}