package com.seyone22.cook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.MeasureConversion
import com.seyone22.cook.data.model.MeasureType
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.model.RecipeTag
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.repository.ingredient.IngredientDao
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageDao
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantDao
import com.seyone22.cook.data.repository.instruction.InstructionDao
import com.seyone22.cook.data.repository.measure.MeasureDao
import com.seyone22.cook.data.repository.measureConversion.MeasureConversionDao
import com.seyone22.cook.data.repository.recipe.RecipeDao
import com.seyone22.cook.data.repository.recipeImage.RecipeImageDao
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientDao
import com.seyone22.cook.data.repository.recipeTag.RecipeTagDao
import com.seyone22.cook.data.repository.shoppingList.ShoppingListDao
import com.seyone22.cook.data.repository.tag.TagDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Ingredient::class, IngredientVariant::class, IngredientImage::class, RecipeImage::class, Measure::class, MeasureConversion::class, Recipe::class, Instruction::class, RecipeIngredient::class, ShoppingList::class, ShoppingListItem::class, Tag::class, RecipeTag::class],
    version = 5,
    exportSchema = true
)
abstract class CookDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun ingredientVariantDao(): IngredientVariantDao
    abstract fun ingredientImageDao(): IngredientImageDao
    abstract fun recipeImageDao(): RecipeImageDao
    abstract fun measureDao(): MeasureDao
    abstract fun measureConversionDao(): MeasureConversionDao
    abstract fun recipeDao(): RecipeDao
    abstract fun instructionDao(): InstructionDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun tagDao(): TagDao
    abstract fun recipeTagDao() : RecipeTagDao

    companion object {
        @Volatile
        private var Instance: CookDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CookDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CookDatabase::class.java, "cook_database")
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_1_3)
                    .addMigrations(MIGRATION_3_4)
                    .addCallback(CookDatabaseCallback(scope))
                    .build().also { Instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the timesMade column to the recipes table
                db.execSQL("ALTER TABLE recipes ADD COLUMN timesMade INTEGER NOT NULL DEFAULT 0")

                // Add the stocked column to the ingredients table
                db.execSQL("ALTER TABLE ingredients ADD COLUMN stocked INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the timesMade column to the recipes table
                db.execSQL("ALTER TABLE recipes ADD COLUMN timesMade INTEGER NOT NULL DEFAULT 0")

                // Add the stocked column to the ingredients table
                db.execSQL("ALTER TABLE ingredients ADD COLUMN stocked INTEGER NOT NULL DEFAULT 0")

                // Add the type column to the measures table
                db.execSQL("ALTER TABLE measures ADD COLUMN type VARCHAR NOT NULL DEFAULT ''")

                // Add new conversions table
                db.execSQL(
                    "CREATE TABLE conversions (" + "id INTEGER PRIMARY KEY NOT NULL," + "fromUnitId INTEGER NOT NULL," + "toUnitId INTEGER NOT NULL," + "conversionFactor REAL NOT NULL)"
                )

                // Create the shopping_lists table
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS shopping_lists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                dateCreated TEXT NOT NULL,
                dateModified TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
                )

                // Create the shopping_list_items table
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS shopping_list_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                shoppingListId INTEGER NOT NULL,
                ingredientId INTEGER NOT NULL,
                quantity REAL NOT NULL,
                measureId INTEGER NOT NULL,
                checked INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (shoppingListId) REFERENCES shopping_lists(id) ON DELETE CASCADE
            )
        """.trimIndent()
                )
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the type column to the measures table
                db.execSQL("ALTER TABLE measures ADD COLUMN type VARCHAR NOT NULL DEFAULT ''")

                // Add new conversions table
                db.execSQL(
                    "CREATE TABLE conversions (" + "id INTEGER PRIMARY KEY NOT NULL," + "fromUnitId INTEGER NOT NULL," + "toUnitId INTEGER NOT NULL," + "conversionFactor REAL NOT NULL)"
                )

                // Create the shopping_lists table
                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS shopping_lists (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            dateCreated TEXT NOT NULL,
                            dateModified TEXT NOT NULL,
                            completed INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent()
                )

                // Create the shopping_list_items table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shopping_list_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        shoppingListId INTEGER NOT NULL,
                        ingredientId INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        measureId INTEGER NOT NULL,
                        checked INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (shoppingListId) REFERENCES shopping_lists(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the tags table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                // Create the recipe_tags table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recipe_tags (
                        recipeId BLOB NOT NULL,
                        tagId INTEGER NOT NULL,
                        PRIMARY KEY (recipeId, tagId),
                        FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE,
                        FOREIGN KEY (tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Insert initial tags using raw SQL
                val initialTags = listOf(
                    // Meal Types
                    Pair("Breakfast", "Meals"), Pair("Brunch", "Meals"), Pair("Lunch", "Meals"),
                    Pair("Dinner", "Meals"), Pair("Snack", "Meals"), Pair("Appetizer", "Meals"),
                    Pair("Dessert", "Meals"), Pair("Side Dish", "Meals"), Pair("Main Course", "Meals"),
                    Pair("Salad", "Meals"), Pair("Soup", "Meals"), Pair("Beverage", "Meals"),

                    // Cuisines
                    Pair("American", "Cuisines"), Pair("Italian", "Cuisines"), Pair("Mexican", "Cuisines"),
                    Pair("Chinese", "Cuisines"), Pair("Indian", "Cuisines"), Pair("French", "Cuisines"),
                    Pair("Thai", "Cuisines"), Pair("Japanese", "Cuisines"), Pair("Mediterranean", "Cuisines"),
                    Pair("Greek", "Cuisines"), Pair("Middle Eastern", "Cuisines"), Pair("African", "Cuisines"),
                    Pair("Spanish", "Cuisines"), Pair("Vietnamese", "Cuisines"), Pair("Korean", "Cuisines"),
                    Pair("Caribbean", "Cuisines"), Pair("Latin American", "Cuisines"),

                    // Dietary
                    Pair("Vegetarian", "Dietary"), Pair("Vegan", "Dietary"), Pair("Gluten-Free", "Dietary"),
                    Pair("Dairy-Free", "Dietary"), Pair("Nut-Free", "Dietary"), Pair("Low-Carb", "Dietary"),
                    Pair("Keto", "Dietary"), Pair("Paleo", "Dietary"), Pair("Pescatarian", "Dietary"),
                    Pair("Whole30", "Dietary"), Pair("Sugar-Free", "Dietary"), Pair("Low-Fat", "Dietary"),
                    Pair("High-Protein", "Dietary"), Pair("Low-Sodium", "Dietary"),

                    // Cooking Time
                    Pair("15 Minutes or Less", "Time"), Pair("30 Minutes or Less", "Time"),
                    Pair("Under 1 Hour", "Time"), Pair("Slow Cooker", "Time"), Pair("Instant Pot", "Time"),
                    Pair("Quick & Easy", "Time"), Pair("Make-Ahead", "Time"), Pair("5 Ingredients or Less", "Time"),

                    // Cooking Methods
                    Pair("Baking", "Methods"), Pair("Grilling", "Methods"), Pair("Roasting", "Methods"),
                    Pair("Stir-Frying", "Methods"), Pair("Sautéing", "Methods"), Pair("Steaming", "Methods"),
                    Pair("Boiling", "Methods"), Pair("Broiling", "Methods"), Pair("Pressure Cooking", "Methods"),
                    Pair("Slow Cooking", "Methods"), Pair("Air Fryer", "Methods"), Pair("Sous Vide", "Methods"),
                    Pair("One-Pot", "Methods"), Pair("No-Cook", "Methods"),

                    // Seasonal & Occasions
                    Pair("Spring", "Seasonal"), Pair("Summer", "Seasonal"), Pair("Fall", "Seasonal"),
                    Pair("Winter", "Seasonal"), Pair("Thanksgiving", "Seasonal"), Pair("Christmas", "Seasonal"),
                    Pair("New Year’s Eve", "Seasonal"), Pair("Easter", "Seasonal"), Pair("Halloween", "Seasonal"),
                    Pair("Valentine's Day", "Seasonal"), Pair("Fourth of July", "Seasonal"), Pair("Birthday", "Seasonal"),
                    Pair("Party", "Seasonal"), Pair("Picnic", "Seasonal"), Pair("BBQ", "Seasonal"),
                    Pair("Potluck", "Seasonal"), Pair("Holiday Special", "Seasonal"), Pair("Weeknight Dinner", "Seasonal"),
                    Pair("Comfort Food", "Seasonal"),

                    // Allergies
                    Pair("Egg-Free", "Allergies"), Pair("Soy-Free", "Allergies"), Pair("Peanut-Free", "Allergies"),
                    Pair("Shellfish-Free", "Allergies"), Pair("Low FODMAP", "Allergies"), Pair("Halal", "Allergies"),
                    Pair("Kosher", "Allergies"),

                    // Skill Levels
                    Pair("Beginner", "Skill Level"), Pair("Intermediate", "Skill Level"), Pair("Advanced", "Skill Level"),
                    Pair("Kid-Friendly", "Skill Level"),

                    // Health Goals
                    Pair("Weight Loss", "Health"), Pair("Muscle Gain", "Health"), Pair("Heart-Healthy", "Health"),
                    Pair("Diabetic-Friendly", "Health"), Pair("High Fiber", "Health"), Pair("Detox", "Health"),
                    Pair("Low Cholesterol", "Health"),

                    // Meal Planning
                    Pair("Meal Prep", "Planning"), Pair("Freezer-Friendly", "Planning"), Pair("Leftovers", "Planning"),
                    Pair("Family-Friendly", "Planning"), Pair("Budget-Friendly", "Planning"), Pair("Date Night", "Planning"),
                    Pair("Kids’ Lunchbox", "Planning"), Pair("Work-from-Home Lunch", "Planning"),
                    Pair("Outdoor Cooking", "Planning")
                )

                // Inserting each tag into the database
                initialTags.forEach { (name, category) ->
                    db.execSQL(
                        "INSERT INTO tags (name, category) VALUES (?, ?)",
                        arrayOf(name, category)
                    )
                }
            }
        }

    }

    private class CookDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // This will be executed on the background thread
            Instance?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: CookDatabase) {
            // Insert your initial data here
            val measureDao = db.measureDao()
            val measureConversionDao = db.measureConversionDao()
            val tagDao = db.tagDao()

            val initialUnits = listOf(
                Measure(id = 1, abbreviation = "g", name = "gram", type = MeasureType.WEIGHT.name),
                Measure(
                    id = 2, abbreviation = "kg", name = "kilogram", type = MeasureType.WEIGHT.name
                ),
                Measure(
                    id = 3, abbreviation = "ml", name = "milliliter", type = MeasureType.VOLUME.name
                ),
                Measure(id = 4, abbreviation = "l", name = "liter", type = MeasureType.VOLUME.name),
                Measure(
                    id = 5, abbreviation = "tsp", name = "teaspoon", type = MeasureType.VOLUME.name
                ),
                Measure(
                    id = 6,
                    abbreviation = "tbsp",
                    name = "tablespoon",
                    type = MeasureType.VOLUME.name
                ),
                Measure(id = 7, abbreviation = "cup", name = "cup", type = MeasureType.VOLUME.name),
            )
            initialUnits.forEach {
                measureDao.insert(it)
            }


            val initialConversions = listOf(
                MeasureConversion(fromUnitId = 2, toUnitId = 1, conversionFactor = 1000.0),
                MeasureConversion(fromUnitId = 1, toUnitId = 2, conversionFactor = 0.001),
                MeasureConversion(fromUnitId = 4, toUnitId = 3, conversionFactor = 1000.0),
                MeasureConversion(fromUnitId = 3, toUnitId = 4, conversionFactor = 0.001),
                MeasureConversion(fromUnitId = 5, toUnitId = 4, conversionFactor = 0.00492892),
                MeasureConversion(fromUnitId = 6, toUnitId = 4, conversionFactor = 0.0147868),
                MeasureConversion(fromUnitId = 7, toUnitId = 4, conversionFactor = 0.25),
                MeasureConversion(fromUnitId = 4, toUnitId = 7, conversionFactor = 4.0),
                MeasureConversion(fromUnitId = 4, toUnitId = 6, conversionFactor = 67.628),
                MeasureConversion(fromUnitId = 4, toUnitId = 5, conversionFactor = 202.884),
            )
            initialConversions.forEach {
                measureConversionDao.insert(it)
            }

            val initialTags = listOf(
                // Meal Types
                Tag(name = "Breakfast", category = "Meals"),
                Tag(name = "Brunch", category = "Meals"),
                Tag(name = "Lunch", category = "Meals"),
                Tag(name = "Dinner", category = "Meals"),
                Tag(name = "Snack", category = "Meals"),
                Tag(name = "Appetizer", category = "Meals"),
                Tag(name = "Dessert", category = "Meals"),
                Tag(name = "Side Dish", category = "Meals"),
                Tag(name = "Main Course", category = "Meals"),
                Tag(name = "Salad", category = "Meals"),
                Tag(name = "Soup", category = "Meals"),
                Tag(name = "Beverage", category = "Meals"),

                // Cuisines
                Tag(name = "American", category = "Cuisines"),
                Tag(name = "Italian", category = "Cuisines"),
                Tag(name = "Mexican", category = "Cuisines"),
                Tag(name = "Chinese", category = "Cuisines"),
                Tag(name = "Indian", category = "Cuisines"),
                Tag(name = "French", category = "Cuisines"),
                Tag(name = "Thai", category = "Cuisines"),
                Tag(name = "Japanese", category = "Cuisines"),
                Tag(name = "Mediterranean", category = "Cuisines"),
                Tag(name = "Greek", category = "Cuisines"),
                Tag(name = "Middle Eastern", category = "Cuisines"),
                Tag(name = "African", category = "Cuisines"),
                Tag(name = "Spanish", category = "Cuisines"),
                Tag(name = "Vietnamese", category = "Cuisines"),
                Tag(name = "Korean", category = "Cuisines"),
                Tag(name = "Caribbean", category = "Cuisines"),
                Tag(name = "Latin American", category = "Cuisines"),

                // Dietary
                Tag(name = "Vegetarian", category = "Dietary"),
                Tag(name = "Vegan", category = "Dietary"),
                Tag(name = "Gluten-Free", category = "Dietary"),
                Tag(name = "Dairy-Free", category = "Dietary"),
                Tag(name = "Nut-Free", category = "Dietary"),
                Tag(name = "Low-Carb", category = "Dietary"),
                Tag(name = "Keto", category = "Dietary"),
                Tag(name = "Paleo", category = "Dietary"),
                Tag(name = "Pescatarian", category = "Dietary"),
                Tag(name = "Whole30", category = "Dietary"),
                Tag(name = "Sugar-Free", category = "Dietary"),
                Tag(name = "Low-Fat", category = "Dietary"),
                Tag(name = "High-Protein", category = "Dietary"),
                Tag(name = "Low-Sodium", category = "Dietary"),

                // Cooking Time
                Tag(name = "15 Minutes or Less", category = "Time"),
                Tag(name = "30 Minutes or Less", category = "Time"),
                Tag(name = "Under 1 Hour", category = "Time"),
                Tag(name = "Slow Cooker", category = "Time"),
                Tag(name = "Instant Pot", category = "Time"),
                Tag(name = "Quick & Easy", category = "Time"),
                Tag(name = "Make-Ahead", category = "Time"),
                Tag(name = "5 Ingredients or Less", category = "Time"),

                // Cooking Methods
                Tag(name = "Baking", category = "Methods"),
                Tag(name = "Grilling", category = "Methods"),
                Tag(name = "Roasting", category = "Methods"),
                Tag(name = "Stir-Frying", category = "Methods"),
                Tag(name = "Sautéing", category = "Methods"),
                Tag(name = "Steaming", category = "Methods"),
                Tag(name = "Boiling", category = "Methods"),
                Tag(name = "Broiling", category = "Methods"),
                Tag(name = "Pressure Cooking", category = "Methods"),
                Tag(name = "Slow Cooking", category = "Methods"),
                Tag(name = "Air Fryer", category = "Methods"),
                Tag(name = "Sous Vide", category = "Methods"),
                Tag(name = "One-Pot", category = "Methods"),
                Tag(name = "No-Cook", category = "Methods"),

                // Seasonal & Occasions
                Tag(name = "Spring", category = "Seasonal"),
                Tag(name = "Summer", category = "Seasonal"),
                Tag(name = "Fall", category = "Seasonal"),
                Tag(name = "Winter", category = "Seasonal"),
                Tag(name = "Thanksgiving", category = "Seasonal"),
                Tag(name = "Christmas", category = "Seasonal"),
                Tag(name = "New Year’s Eve", category = "Seasonal"),
                Tag(name = "Easter", category = "Seasonal"),
                Tag(name = "Halloween", category = "Seasonal"),
                Tag(name = "Valentine's Day", category = "Seasonal"),
                Tag(name = "Fourth of July", category = "Seasonal"),
                Tag(name = "Birthday", category = "Seasonal"),
                Tag(name = "Party", category = "Seasonal"),
                Tag(name = "Picnic", category = "Seasonal"),
                Tag(name = "BBQ", category = "Seasonal"),
                Tag(name = "Potluck", category = "Seasonal"),
                Tag(name = "Holiday Special", category = "Seasonal"),
                Tag(name = "Weeknight Dinner", category = "Seasonal"),
                Tag(name = "Comfort Food", category = "Seasonal"),

                // Allergies
                Tag(name = "Egg-Free", category = "Allergies"),
                Tag(name = "Soy-Free", category = "Allergies"),
                Tag(name = "Peanut-Free", category = "Allergies"),
                Tag(name = "Shellfish-Free", category = "Allergies"),
                Tag(name = "Low FODMAP", category = "Allergies"),
                Tag(name = "Halal", category = "Allergies"),
                Tag(name = "Kosher", category = "Allergies"),

                // Skill Levels
                Tag(name = "Beginner", category = "Skill Level"),
                Tag(name = "Intermediate", category = "Skill Level"),
                Tag(name = "Advanced", category = "Skill Level"),
                Tag(name = "Kid-Friendly", category = "Skill Level"),

                // Health Goals
                Tag(name = "Weight Loss", category = "Health"),
                Tag(name = "Muscle Gain", category = "Health"),
                Tag(name = "Heart-Healthy", category = "Health"),
                Tag(name = "Diabetic-Friendly", category = "Health"),
                Tag(name = "High Fiber", category = "Health"),
                Tag(name = "Detox", category = "Health"),
                Tag(name = "Low Cholesterol", category = "Health"),

                // Meal Planning
                Tag(name = "Meal Prep", category = "Planning"),
                Tag(name = "Freezer-Friendly", category = "Planning"),
                Tag(name = "Leftovers", category = "Planning"),
                Tag(name = "Family-Friendly", category = "Planning"),
                Tag(name = "Budget-Friendly", category = "Planning"),
                Tag(name = "Date Night", category = "Planning"),
                Tag(name = "Kids’ Lunchbox", category = "Planning"),
                Tag(name = "Work-from-Home Lunch", category = "Planning"),
                Tag(name = "Outdoor Cooking", category = "Planning")
            )
            initialTags.forEach {
                tagDao.insert(it)
            }

        }
    }
}