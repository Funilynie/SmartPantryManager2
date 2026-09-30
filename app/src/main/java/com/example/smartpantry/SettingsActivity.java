package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.data.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Screen 5: SETTINGS.
 * Simple preferences persisted with SharedPreferences:
 *   - expiring-soon alerts toggle
 *   - units preference
 * Plus a "clear pantry" maintenance action.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        Switch swExpiry = findViewById(R.id.swExpiryAlerts);
        Spinner spUnits = findViewById(R.id.spUnits);
        Button btnClear = findViewById(R.id.btnClearPantry);

        swExpiry.setChecked(prefs.getBoolean("expiry_alerts", true));
        spUnits.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Metric (g, ml)", "Imperial (oz, cups)"}));
        spUnits.setSelection(prefs.getInt("units_sel", 0));

        swExpiry.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("expiry_alerts", checked).apply());
        spUnits.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                prefs.edit().putInt("units_sel", position).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnClear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clear entire pantry?")
                .setMessage("This deletes all your pantry items.")
                .setPositiveButton("Clear", (d, w) -> {
                    new DatabaseHelper(this).clearPantry();
                    Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_settings);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new android.content.Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new android.content.Intent(this, SuggestedRecipesActivity.class));
                finish();
                return true;
            }
            return true;
        });
    }
}
