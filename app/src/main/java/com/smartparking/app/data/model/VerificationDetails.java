package com.smartparking.app.data.model;

public class VerificationDetails {
    private final Booking booking;
    private final User user;
    private final ParkingLot parkingLot;

    public VerificationDetails(Booking booking, User user, ParkingLot parkingLot) {
        this.booking = booking;
        this.user = user;
        this.parkingLot = parkingLot;
    }

    public Booking getBooking() {
        return booking;
    }

    public User getUser() {
        return user;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}