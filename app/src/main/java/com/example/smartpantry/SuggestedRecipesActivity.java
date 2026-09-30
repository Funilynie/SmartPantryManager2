package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.ui.RecipeAdapter;
import com.example.smartpantry.util.IngredientMatcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Screen 3: SUGGESTED RECIPES.
 * Loads the full recipe collection and the current pantry from SQLite,
 * then applies the STRICT matching rule: a recipe appears ONLY if every
 * ingredient is in the pantry in at least the required quantity.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper db;
    private RecyclerView rvRecipes;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");

        db = new DatabaseHelper(this);
        rvRecipes = findViewById(R.id.rvRecipes);
        tvEmpty = findViewById(R.id.tvEmptyRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Recipe> all = db.getAllRecipes();       // READ recipes
        List<PantryItem> pantry = db.getAllPantryItems(); // READ pantry

        // ---- THE STRICT MATCHING RULE (core business logic) ----
        List<Recipe> suggested = IngredientMatcher.suggestRecipes(all, pantry);

        rvRecipes.setAdapter(new RecipeAdapter(suggested, this));
        // friendly feedback instead of a blank/broken screen when nothing matches
        tvEmpty.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(Recipe recipe) {
        // Intent passes the selected recipe's id to the detail screen
        Intent i = new Intent(this, RecipeDetailActivity.class);
        i.putExtra("recipeId", recipe.getId());
        startActivity(i);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_suggested);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                finish(); // keep back stack simple: pantry is "home"
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return true;
        });
    }
}
