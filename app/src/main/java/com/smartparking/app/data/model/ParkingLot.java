package com.smartparking.app.data.model;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ParkingLot implements Serializable {
    private String id;
    private String name;
    private String address;
    private GeoPoint location;
    private String geohash;
    private String mapLink; // <-- ADD THIS FIELD
    private int totalSlots;
    private Pricing pricing;
    private List<String> amenities;
    private List<String> images;
    @ServerTimestamp
    private Date createdAt;

    public ParkingLot() {}

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
    public String getGeohash() { return geohash; }
    public void setGeohash(String geohash) { this.geohash = geohash; }

    public String getMapLink() { return mapLink; } // <-- ADD THIS GETTER
    public void setMapLink(String mapLink) { this.mapLink = mapLink; } // <-- ADD THIS SETTER

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }
    public Pricing getPricing() { return pricing; }
    public void setPricing(Pricing pricing) { this.pricing = pricing; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}