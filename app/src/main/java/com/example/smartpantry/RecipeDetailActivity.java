package com.example.smartpantry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.util.IngredientMatcher;

import java.util.List;

/**
 * Screen 4: RECIPE DETAIL.
 * Shows the full ingredient list (with what you have vs what is needed)
 * and the preparation steps. "I cooked this!" deducts the ingredients
 * from the pantry to keep the app data honest.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra("recipeId", -1);
        DatabaseHelper db = new DatabaseHelper(this);

        // Re-read the full recipe (with ingredients) from SQLite
        Recipe found = null;
        for (Recipe r : db.getAllRecipes()) {
            if (r.getId() == recipeId) { found = r; break; }
        }
        if (found == null) { finish(); return; }
        final Recipe recipe = found;

        setTitle(recipe.getName());
        ((TextView) findViewById(R.id.tvDetailDesc)).setText(recipe.getDescription());
        ((TextView) findViewById(R.id.tvSteps)).setText(recipe.getSteps());

        List<PantryItem> pantry = db.getAllPantryItems();
        StringBuilder ing = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            ing.append("• ").append(ri.getName()).append(" — ")
               .append(ri.getQuantity()).append(" ").append(ri.getUnit());
            if (IngredientMatcher.hasEnough(pantry, ri)) {
                ing.append("   ✓ in pantry");
            }
            ing.append("\n");
        }
        ((TextView) findViewById(R.id.tvDetailIngredients)).setText(ing.toString().trim());

        Button btnCooked = findViewById(R.id.btnCooked);
        btnCooked.setOnClickListener(v -> {
            // deduct each ingredient from the pantry
            for (RecipeIngredient ri : recipe.getIngredients()) {
                for (PantryItem p : pantry) {
                    if (IngredientMatcher.normalizeName(p.getName())
                            .equals(IngredientMatcher.normalizeName(ri.getName()))) {
                        double newQty = p.getQuantity() - ri.getQuantity();
                        if (newQty <= 0) {
                            db.deletePantryItem(p.getId());
                        } else {
                            db.updatePantryItem(p.getId(), p.getName(), newQty, p.getUnit(), p.getExpiryDate());
                        }
                    }
                }
            }
            Toast.makeText(this, "Ingredients used — pantry updated!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
