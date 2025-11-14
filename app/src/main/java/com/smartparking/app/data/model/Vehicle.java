package com.smartparking.app.data.model;

import java.io.Serializable;

public class Vehicle implements Serializable {
    private String plateNumber;
    private String model; // <-- ADD THIS FIELD

    public Vehicle() {}

    public Vehicle(String plateNumber, String model) {
        this.plateNumber = plateNumber;
        this.model = model;
    }

    // --- Getters and Setters ---
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getModel() { return model; } // <-- ADD THIS GETTER
    public void setModel(String model) { this.model = model; } // <-- ADD THIS SETTER
}