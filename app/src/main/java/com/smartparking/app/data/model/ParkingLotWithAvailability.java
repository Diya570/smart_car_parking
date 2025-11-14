package com.smartparking.app.data.model;

/**
 * A helper data class to hold a ParkingLot object along with its real-time availability.
 * This is needed because the list of lots and their occupied counts are fetched separately.
 */
public class ParkingLotWithAvailability {
    private final ParkingLot parkingLot;
    private final int occupiedSlots;

    public ParkingLotWithAvailability(ParkingLot parkingLot, int occupiedSlots) {
        this.parkingLot = parkingLot;
        this.occupiedSlots = occupiedSlots;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }
}