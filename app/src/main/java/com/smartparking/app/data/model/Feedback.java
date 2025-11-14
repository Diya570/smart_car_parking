package com.smartparking.app.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Feedback {
    private String id;
    private String userId;
    private String lotId;
    private float rating; // 1-5
    private String comment;
    @ServerTimestamp
    private Date createdAt;

    public Feedback() {}

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}