package com.example.smartpantry.model;

/** One ingredient required by a recipe. */
public class RecipeIngredient {
    private String name;     // e.g. "tomatoes"
    private double quantity; // e.g. 2
    private String unit;     // e.g. "pcs", "g", "ml"

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
