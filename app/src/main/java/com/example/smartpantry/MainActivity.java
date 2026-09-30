package com.example.smartpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.ui.PantryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Screen 1: PANTRY LIST.
 * Shows all ingredients stored in SQLite via a RecyclerView + custom adapter.
 * Full CRUD entry point: add (FAB), edit, delete (buttons on each row).
 * Data persists because it is read from SQLite every time onResume runs.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private DatabaseHelper db;
    private RecyclerView rvPantry;
    private TextView tvEmpty;
    private List<PantryItem> pantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmptyPantry);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry(); // reload from SQLite so list is always fresh (e.g. after add/edit)
    }

    private void loadPantry() {
        pantry = db.getAllPantryItems();
        boolean highlightExpiry = getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("expiry_alerts", true);
        rvPantry.setAdapter(new PantryAdapter(pantry, this));
        tvEmpty.setVisibility(pantry.isEmpty() ? View.VISIBLE : View.GONE);

        if (highlightExpiry) warnExpiringSoon();
    }

    /** Optional feedback feature: toast for items expiring within 3 days. */
    private void warnExpiringSoon() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return;
        long soon = 0;
        for (PantryItem p : pantry) {
            if (p.getExpiryDate() != null) {
                try {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(p.getExpiryDate()));
                    if (days >= 0 && days <= 3) soon++;
                } catch (Exception ignored) { }
            }
        }
        if (soon > 0) {
            Toast.makeText(this, soon + " item(s) expiring within 3 days!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        // Intent carries the existing record to the Add/Edit screen (UPDATE flow)
        Intent i = new Intent(this, AddEditIngredientActivity.class);
        i.putExtra("id", item.getId());
        i.putExtra("name", item.getName());
        i.putExtra("qty", item.getQuantity());
        i.putExtra("unit", item.getUnit());
        i.putExtra("expiry", item.getExpiryDate());
        startActivity(i);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete " + item.getName() + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deletePantryItem(item.getId()); // DELETE
                    loadPantry();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Bottom navigation shared across the three main screens. */
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_pantry);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return true; // already on pantry
        });
    }
}
