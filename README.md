# Smart Pantry Manager

An Android application (written in **Java**) that helps users cut food waste by
tracking the ingredients they actually have at home (their *pantry*) and
suggesting recipes they can cook using **strictly** those leftover ingredients —
no shopping trip required.

A recipe is only suggested when **every single ingredient** it requires is
present in the pantry in **at least** the required quantity. One missing
ingredient disqualifies the recipe.

## Features

- **Pantry management** — add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- **Pantry List screen** — RecyclerView with a custom adapter, bound to SQLite
- **Recipe collection** — 20 recipes pre-loaded (seeded) into the database on first run
- **Suggested Recipes screen** — runs the strict-matching rule against the current pantry; shows only recipes you can make right now
- **Recipe Detail screen** — full ingredient list (with in-pantry ticks) and preparation steps; "I cooked this!" deducts used ingredients
- **Settings screen** — expiring-soon alert toggle, units preference, clear-pantry option
- Friendly empty-state messages instead of blank/broken screens
- Data persists between app restarts (SQLite on-device)

## Database choice: SQLite (SQLiteOpenHelper)

SQLite was chosen because:
1. The data is small, personal and **offline-only** — no server or account needed.
2. It is covered directly in the module's persistent-data chapter.
3. Zero external dependencies or network latency; the database file lives on-device and survives app restarts automatically.
4. Full CRUD is straightforward with `SQLiteOpenHelper`, `ContentValues` and `Cursor`.

**Schema**
- `pantry_items(_id, name, quantity, unit, expiry_date)` — user data (CRUD)
- `recipes(_id, name, description, steps)` — seeded recipe collection
- `recipe_ingredients(_id, recipe_id, name, quantity, unit)` — ingredients per recipe

## The strict-matching rule

Implemented in `util/IngredientMatcher.java`:
- Ingredient names are normalised (lowercase, trimmed, simple singularisation:
  "tomatoes" → "tomato", "eggs" → "egg") so trivial messiness does not break matching.
- Quantities are converted to a base unit before comparison
  (kg → g, L → ml), so 1 kg of flour satisfies a recipe needing 500 g.
- A recipe qualifies only if `hasEnough()` returns true for **every** ingredient.

## Tech stack

- Language: **Java** (no Kotlin)
- IDE: Android Studio
- Min SDK 24, Target/Compile SDK 34
- Libraries: AppCompat, Material Components, RecyclerView

## Setup & run instructions

1. Open Android Studio → **Open** → select the `SmartPantryManager` project folder.
2. Let Gradle sync (accept the prompt to use the bundled Gradle if offered).
3. Create a run configuration: select an emulator (e.g. Pixel 6, API 30+) or a
   physical device with USB debugging enabled.
4. Press **Run ▶**. The database is created and seeded automatically on first launch.

No API keys, accounts or internet connection are required.

## Project structure

```
app/src/main/java/com/example/smartpantry/
├── MainActivity.java              # Pantry list (home screen)
├── AddEditIngredientActivity.java # Add / edit pantry item (with validation)
├── SuggestedRecipesActivity.java  # Strict-matched recipe suggestions
├── RecipeDetailActivity.java      # Recipe ingredients + method
├── SettingsActivity.java          # Preferences (SharedPreferences)
├── data/
│   └── DatabaseHelper.java        # SQLite schema, CRUD, recipe seeding
├── model/
│   ├── PantryItem.java
│   ├── Recipe.java
│   └── RecipeIngredient.java
├── ui/
│   ├── PantryAdapter.java         # RecyclerView adapter for pantry
│   └── RecipeAdapter.java         # RecyclerView adapter for recipes
└── util/
    └── IngredientMatcher.java     # STRICT matching rule (core logic)
```
