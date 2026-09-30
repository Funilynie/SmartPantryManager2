package com.example.smartpantry.util;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Core business logic of Smart Pantry Manager: the STRICT matching rule.
 *
 * A recipe may only be suggested if EVERY ingredient it requires is present
 * in the pantry in AT LEAST the required quantity. One missing ingredient
 * (even partially) disqualifies the whole recipe.
 *
 * To survive "real world messiness" we normalise names and units before
 * comparing:
 *   - "Tomatoes" vs "tomato", "Eggs" vs "eggs"  -> singular, lowercase
 *   - 1 kg vs 1000 g, 1 L vs 1000 ml           -> converted to a base unit
 */
public class IngredientMatcher {

    /** Normalise an ingredient name: lowercase, trim, singularise. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String n = raw.trim().toLowerCase(Locale.ROOT);
        // drop simple plural endings (not a full NLP solution, just robust enough)
        if (n.endsWith("ies") && n.length() > 3) {
            n = n.substring(0, n.length() - 3) + "y";   // strawberries -> strawberry
        } else if (n.endsWith("es") && n.length() > 2) {
            n = n.substring(0, n.length() - 2);         // tomatoes -> tomato, potatoes -> potato
        } else if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 1) {
            n = n.substring(0, n.length() - 1);         // eggs -> egg
        }
        return n;
    }

    /** Convert a quantity into a canonical base unit so units can be compared. */
    public static double toBaseQty(double qty, String unit) {
        if (unit == null) return qty;
        String u = unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "kg":  return qty * 1000;  // -> grams
            case "l":   return qty * 1000;  // -> millilitres
            case "g":
            case "ml":
            case "pcs":
            case "":
                return qty;
            default:
                return qty; // unknown unit: compare as-is
        }
    }

    /** Does the pantry contain at least `required` of this ingredient? */
    public static boolean hasEnough(List<PantryItem> pantry, RecipeIngredient required) {
        String want = normalizeName(required.getName());
        double wantQty = toBaseQty(required.getQuantity(), required.getUnit());
        for (PantryItem item : pantry) {
            if (normalizeName(item.getName()).equals(want)) {
                double haveQty = toBaseQty(item.getQuantity(), item.getUnit());
                if (haveQty >= wantQty) {
                    return true; // found, and we have enough
                }
            }
        }
        return false; // missing entirely, or not enough
    }

    /**
     * STRICT suggestion: keep only recipes where EVERY ingredient is satisfied.
     * A recipe missing even one ingredient is excluded from this list.
     */
    public static List<Recipe> suggestRecipes(List<Recipe> all, List<PantryItem> pantry) {
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe r : all) {
            boolean canMake = true;
            for (RecipeIngredient ing : r.getIngredients()) {
                if (!hasEnough(pantry, ing)) {
                    canMake = false;
                    break; // one missing ingredient -> not suggested. Strict!
                }
            }
            if (canMake) suggested.add(r);
        }
        return suggested;
    }
}
