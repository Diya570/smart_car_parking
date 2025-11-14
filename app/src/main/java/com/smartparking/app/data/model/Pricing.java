package com.smartparking.app.data.model;

import java.io.Serializable;

public class Pricing implements Serializable {
    private String currency; // e.g., "USD", "INR"
    private double perHour;
    private int minBillableMinutes;
    private int roundingToMinutes;

    public Pricing() {}

    // --- Getters and Setters ---
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public double getPerHour() { return perHour; }
    public void setPerHour(double perHour) { this.perHour = perHour; }
    public int getMinBillableMinutes() { return minBillableMinutes; }
    public void setMinBillableMinutes(int minBillableMinutes) { this.minBillableMinutes = minBillableMinutes; }
    public int getRoundingToMinutes() { return roundingToMinutes; }
    public void setRoundingToMinutes(int roundingToMinutes) { this.roundingToMinutes = roundingToMinutes; }
}