package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database for Smart Pantry Manager.
 *
 * Two tables:
 *   pantry_items        - what the user currently has at home (user CRUD data)
 *   recipes             - pre-loaded recipe collection
 *   recipe_ingredients  - ingredients required by each recipe
 *
 * Chosen option: SQLite (SQLiteOpenHelper) because the data is small,
 * personal, offline-only, and the module covers SQLite directly. No
 * network/server needed, and it persists on-device between app restarts.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_P_ID = "_id";
    public static final String COL_P_NAME = "name";
    public static final String COL_P_QTY = "quantity";
    public static final String COL_P_UNIT = "unit";
    public static final String COL_P_EXPIRY = "expiry_date";

    // recipe tables
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_R_ID = "_id";
    public static final String COL_R_NAME = "name";
    public static final String COL_R_DESC = "description";
    public static final String COL_R_STEPS = "steps";

    public static final String TABLE_RECIPE_ING = "recipe_ingredients";
    public static final String COL_I_ID = "_id";
    public static final String COL_I_RECIPE = "recipe_id";
    public static final String COL_I_NAME = "name";
    public static final String COL_I_QTY = "quantity";
    public static final String COL_I_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_QTY + " REAL NOT NULL, " +
                COL_P_UNIT + " TEXT NOT NULL, " +
                COL_P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_NAME + " TEXT NOT NULL, " +
                COL_R_DESC + " TEXT, " +
                COL_R_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " (" +
                COL_I_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_I_RECIPE + " INTEGER NOT NULL, " +
                COL_I_NAME + " TEXT NOT NULL, " +
                COL_I_QTY + " REAL NOT NULL, " +
                COL_I_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_I_RECIPE + ") REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + "))");

        seedRecipes(db); // pre-load the recipe collection on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple dev strategy: recreate (recipe data is seeded anyway)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ===================== PANTRY CRUD =====================

    /** CREATE */
    public long addPantryItem(String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, name);
        cv.put(COL_P_QTY, qty);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        return db.insert(TABLE_PANTRY, null, cv);
    }

    /** READ */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COL_P_NAME, null);
        if (c.moveToFirst()) {
            do {
                list.add(new PantryItem(
                        c.getLong(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getString(3),
                        c.isNull(4) ? null : c.getString(4)));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    /** UPDATE */
    public int updatePantryItem(long id, String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, name);
        cv.put(COL_P_QTY, qty);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        return db.update(TABLE_PANTRY, cv, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** DELETE */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void clearPantry() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, null, null);
    }

    // ===================== RECIPES (seeded, read-only at runtime) =====================

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_RECIPES + " ORDER BY " + COL_R_NAME, null);
        if (c.moveToFirst()) {
            do {
                long id = c.getLong(0);
                list.add(new Recipe(id, c.getString(1), c.getString(2), c.getString(3),
                        getIngredientsForRecipe(id)));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_I_NAME + ", " + COL_I_QTY + ", " + COL_I_UNIT +
                " FROM " + TABLE_RECIPE_ING + " WHERE " + COL_I_RECIPE + "=?",
                new String[]{String.valueOf(recipeId)});
        if (c.moveToFirst()) {
            do {
                list.add(new RecipeIngredient(c.getString(0), c.getDouble(1), c.getString(2)));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // ===================== SEED DATA (first run only) =====================

    private void seedRecipes(SQLiteDatabase db) {
        Object[][] recipes = {
            // name, description, steps, {ingredient name, qty, unit}, ...
            {"Scrambled Eggs", "Soft, fluffy scrambled eggs.",
             "1. Crack eggs into a bowl and whisk with the milk.\n2. Melt butter in a pan on medium heat.\n3. Pour in eggs and stir gently until just set.\n4. Season and serve.",
             new Object[][]{{"eggs", 2, "pcs"}, {"butter", 10, "g"}, {"milk", 30, "ml"}}},
            {"Tomato Pasta", "Simple fresh tomato pasta.",
             "1. Boil pasta in salted water until al dente.\n2. Fry garlic in olive oil for 1 minute.\n3. Add chopped tomatoes and simmer 10 minutes.\n4. Toss with drained pasta and serve.",
             new Object[][]{{"pasta", 100, "g"}, {"tomatoes", 2, "pcs"}, {"garlic", 1, "pcs"}, {"olive oil", 15, "ml"}}},
            {"Garlic Butter Rice", "Fragrant buttery rice.",
             "1. Rinse the rice.\n2. Fry garlic in butter for 1 minute.\n3. Add rice and 200ml water, cover and simmer 15 minutes.\n4. Fluff with a fork.",
             new Object[][]{{"rice", 100, "g"}, {"butter", 15, "g"}, {"garlic", 1, "pcs"}}},
            {"Cheese Omelette", "Golden omelette with melted cheese.",
             "1. Whisk the eggs.\n2. Melt butter in a non-stick pan.\n3. Pour in eggs, cook 2 minutes.\n4. Sprinkle cheese, fold and serve.",
             new Object[][]{{"eggs", 2, "pcs"}, {"cheese", 30, "g"}, {"butter", 10, "g"}}},
            {"Grilled Cheese Sandwich", "Crispy, gooey classic.",
             "1. Butter one side of each bread slice.\n2. Place cheese between slices, buttered sides out.\n3. Grill in a pan 3 minutes per side until golden.",
             new Object[][]{{"bread", 4, "pcs"}, {"cheese", 40, "g"}, {"butter", 15, "g"}}},
            {"Pancakes", "Fluffy breakfast pancakes.",
             "1. Mix flour, eggs and milk into a batter.\n2. Melt butter into the batter.\n3. Cook ladles of batter 2 minutes per side.\n4. Serve stacked.",
             new Object[][]{{"flour", 100, "g"}, {"eggs", 2, "pcs"}, {"milk", 150, "ml"}, {"butter", 20, "g"}}},
            {"French Toast", "Sweet fried eggy bread.",
             "1. Whisk eggs with milk in a shallow bowl.\n2. Soak bread slices 20 seconds per side.\n3. Fry in butter until golden on both sides.",
             new Object[][]{{"bread", 4, "pcs"}, {"eggs", 2, "pcs"}, {"milk", 50, "ml"}, {"butter", 10, "g"}}},
            {"Tomato Soup", "Creamy comforting soup.",
             "1. Chop tomatoes and onion.\n2. Soften onion in butter for 5 minutes.\n3. Add tomatoes and 200ml water, simmer 15 minutes.\n4. Blend, stir in milk, season and serve.",
             new Object[][]{{"tomatoes", 4, "pcs"}, {"onion", 1, "pcs"}, {"butter", 15, "g"}, {"milk", 100, "ml"}}},
            {"Egg Fried Rice", "Better than takeaway.",
             "1. Cook rice and let it cool.\n2. Scramble the eggs in butter, set aside.\n3. Fry onion 3 minutes, add rice.\n4. Return eggs, season and serve.",
             new Object[][]{{"rice", 100, "g"}, {"eggs", 2, "pcs"}, {"onion", 1, "pcs"}, {"butter", 15, "g"}}},
            {"Baked Potato", "Loaded cheesy potato.",
             "1. Prick potatoes and microwave 8 minutes.\n2. Split, fluff the inside with a fork.\n3. Top with butter and cheese, melt 1 minute more.",
             new Object[][]{{"potatoes", 2, "pcs"}, {"cheese", 30, "g"}, {"butter", 10, "g"}}},
            {"Pasta Aglio e Olio", "Garlic, oil and cheese pasta.",
             "1. Boil pasta until al dente.\n2. Gently fry sliced garlic in olive oil 2 minutes.\n3. Toss pasta through the oil with cheese.\n4. Serve immediately.",
             new Object[][]{{"pasta", 100, "g"}, {"garlic", 2, "pcs"}, {"olive oil", 20, "ml"}, {"cheese", 20, "g"}}},
            {"Spanish Tortilla", "Simple potato and egg tortilla.",
             "1. Dice potatoes, microwave 5 minutes.\n2. Fry onion in butter 5 minutes.\n3. Add potatoes and beaten eggs, cook on low 8 minutes.\n4. Flip and cook 3 more minutes.",
             new Object[][]{{"eggs", 3, "pcs"}, {"potatoes", 2, "pcs"}, {"onion", 1, "pcs"}, {"butter", 20, "g"}}},
            {"Cheesy Scrambled Eggs", "Extra indulgent scrambled eggs.",
             "1. Whisk eggs with milk.\n2. Melt butter, cook eggs gently.\n3. Stir in cheese off the heat until melted.\n4. Serve.",
             new Object[][]{{"eggs", 3, "pcs"}, {"cheese", 25, "g"}, {"milk", 30, "ml"}, {"butter", 10, "g"}}},
            {"Tomato & Onion Omelette", "Veggie-packed omelette.",
             "1. Whisk eggs.\n2. Soften chopped tomato and onion in butter.\n3. Pour in eggs, cook until set.\n4. Fold and serve.",
             new Object[][]{{"eggs", 2, "pcs"}, {"tomatoes", 1, "pcs"}, {"onion", 1, "pcs"}, {"butter", 10, "g"}}},
            {"Garlic Bread", "Crispy buttery garlic bread.",
             "1. Mix butter with crushed garlic.\n2. Spread over bread slices.\n3. Grill 3 minutes until golden and crisp.",
             new Object[][]{{"bread", 4, "pcs"}, {"butter", 30, "g"}, {"garlic", 2, "pcs"}}},
            {"Rice Pudding", "Creamy old-fashioned dessert.",
             "1. Simmer rice with milk and sugar 25 minutes, stirring often.\n2. Stir in butter.\n3. Serve warm or chilled.",
             new Object[][]{{"rice", 80, "g"}, {"milk", 300, "ml"}, {"butter", 10, "g"}, {"sugar", 20, "g"}}},
            {"Potato & Egg Hash", "Hearty one-pan breakfast.",
             "1. Dice potatoes, microwave 4 minutes.\n2. Fry onion and potatoes in butter 8 minutes.\n3. Make wells, crack in eggs, cover and cook 4 minutes.",
             new Object[][]{{"potatoes", 2, "pcs"}, {"eggs", 2, "pcs"}, {"onion", 1, "pcs"}, {"butter", 15, "g"}}},
            {"Chicken Pasta", "Simple garlic chicken pasta.",
             "1. Boil pasta until al dente.\n2. Fry chicken pieces in butter 6 minutes.\n3. Add garlic, fry 1 minute more.\n4. Toss with pasta and serve.",
             new Object[][]{{"pasta", 100, "g"}, {"chicken", 120, "g"}, {"garlic", 1, "pcs"}, {"butter", 15, "g"}}},
            {"Mac & Cheese", "Stovetop cheesy pasta.",
             "1. Boil pasta.\n2. Melt butter, whisk in flour 1 minute.\n3. Gradually add milk to make a sauce.\n4. Stir in cheese and drained pasta.",
             new Object[][]{{"pasta", 100, "g"}, {"cheese", 50, "g"}, {"milk", 150, "ml"}, {"butter", 20, "g"}, {"flour", 15, "g"}}},
            {"Chicken & Rice Bowl", "Quick satisfying bowl.",
             "1. Cook rice.\n2. Fry chicken with onion and garlic in butter 8 minutes.\n3. Serve chicken over rice.",
             new Object[][]{{"rice", 100, "g"}, {"chicken", 150, "g"}, {"onion", 1, "pcs"}, {"garlic", 2, "pcs"}, {"butter", 15, "g"}}},
        };

        for (Object[] r : recipes) {
            ContentValues rv = new ContentValues();
            rv.put(COL_R_NAME, (String) r[0]);
            rv.put(COL_R_DESC, (String) r[1]);
            rv.put(COL_R_STEPS, (String) r[2]);
            long recipeId = db.insert(TABLE_RECIPES, null, rv);

            Object[][] ings = (Object[][]) r[3];
            for (Object[] ing : ings) {
                ContentValues iv = new ContentValues();
                iv.put(COL_I_RECIPE, recipeId);
                iv.put(COL_I_NAME, (String) ing[0]);
                iv.put(COL_I_QTY, ((Number) ing[1]).doubleValue());
                iv.put(COL_I_UNIT, (String) ing[2]);
                db.insert(TABLE_RECIPE_ING, null, iv);
            }
        }
    }
}
