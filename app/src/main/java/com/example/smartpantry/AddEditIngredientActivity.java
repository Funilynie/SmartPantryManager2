package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.data.DatabaseHelper;

import java.util.Calendar;

/**
 * Screen 2: ADD / EDIT INGREDIENT.
 * Reused for both CREATE (no extras) and UPDATE (id extra present).
 * Demonstrates input validation on a data entry form.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper db;
    private long editId = -1; // -1 = adding a new item

    private static final String[] UNITS = {"pcs", "g", "kg", "ml", "l"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        Button btnSave = findViewById(R.id.btnSave);

        // Spinner: main layout + separate DROPDOWN layout (the reliable way)
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);
        spUnit.setSelection(0);

        // Expiry date: display-only field -> open the calendar on touch.
        // OnTouchListener is used (not just OnClickListener) because a
        // focusable EditText can swallow plain click events on some devices.
        etExpiry.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showDatePicker();
            }
            return true; // consume: this field is tap-only, never editable
        });

        // If an id was passed via the Intent, we are editing an existing row
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("id")) {
            editId = extras.getLong("id");
            etName.setText(extras.getString("name"));
            etQty.setText(String.valueOf(extras.getDouble("qty")));
            String unit = extras.getString("unit");
            if (unit != null) {
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equals(unit)) spUnit.setSelection(i);
                }
            }
            String expiry = extras.getString("expiry");
            if (expiry != null) etExpiry.setText(expiry);
            setTitle("Edit Ingredient");
        } else {
            setTitle("Add Ingredient");
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        // pre-fill with an existing date if editing
        String current = etExpiry.getText().toString().trim();
        if (current.length() >= 10) {
            try {
                c.set(Integer.parseInt(current.substring(0, 4)),
                      Integer.parseInt(current.substring(5, 7)) - 1,
                      Integer.parseInt(current.substring(8, 10)));
            } catch (Exception ignored) { }
        }
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, y, m, d) ->
                        etExpiry.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void save() {
        // ---- INPUT VALIDATION ----
        String name = etName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Ingredient name is required");
            return;
        }
        if (name.length() < 2) {
            etName.setError("Name too short");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etQty.setError("Enter a valid number");
            return;
        }
        if (qty <= 0) {
            etQty.setError("Quantity must be greater than zero");
            return;
        }

        String unit = spUnit.getSelectedItem().toString();
        String expiry = etExpiry.getText().toString().trim();
        if (expiry.isEmpty()) expiry = null;

        if (editId == -1) {
            db.addPantryItem(name, qty, unit, expiry);            // CREATE
            Toast.makeText(this, "Added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(editId, name, qty, unit, expiry); // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // return to the pantry list (onResume there reloads the data)
    }
}
