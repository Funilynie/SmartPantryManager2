package com.example.smartpantry.model;

import java.util.List;

/** A cookable recipe with its required ingredients and preparation steps. */
public class Recipe {
    private long id;
    private String name;
    private String description;
    private String steps;    // preparation steps, newline separated
    private List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String description, String steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSteps() { return steps; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
