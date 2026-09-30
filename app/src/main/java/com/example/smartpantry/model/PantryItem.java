package com.example.smartpantry.model;

/** A single ingredient the user currently has at home. */
public class PantryItem {
    private long id;
    private String name;      // e.g. "Eggs"
    private double quantity;  // e.g. 6
    private String unit;      // e.g. "pcs", "g", "ml"
    private String expiryDate; // "yyyy-MM-dd" or null

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    public void setId(long id) { this.id = id; }
}
