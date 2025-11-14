package com.smartparking.app.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String displayName;
    private String email;
    private String role; // "user" or "admin"
    private Vehicle vehicle;
    private List<String> fcmTokens;
    @ServerTimestamp
    private Date createdAt;

    public User() {}

    // --- Getters and Setters ---
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public List<String> getFcmTokens() { return fcmTokens; }
    public void setFcmTokens(List<String> fcmTokens) { this.fcmTokens = fcmTokens; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}