package com.smartparking.app.data.model;

import java.io.Serializable;

public class Slot implements Serializable {
    private String id;
    private String label;
    private int level;
    private String type;

    // THE FIX IS HERE: The variable name now matches the database field name "active".
    private boolean active;

    public Slot() {}

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // The getter is now correctly named isActive() which is standard for booleans,
    // but Firestore will correctly map it to the 'active' field.
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}