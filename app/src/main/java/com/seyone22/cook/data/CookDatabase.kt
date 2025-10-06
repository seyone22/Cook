package com.seyone22.cook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.seyone22.cook.data.converters.RoomConverters
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.MealEntry
import com.seyone22.cook.data.model.MealEntryIngredientCrossRef
import com.seyone22.cook.data.model.MealEntryTagCrossRef
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
import com.seyone22.cook.data.model.TagType
import com.seyone22.cook.data.repository.ingredient.IngredientDao
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageDao
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantDao
import com.seyone22.cook.data.repository.instruction.InstructionDao
import com.seyone22.cook.data.repository.instructionsection.InstructionSectionDao
import com.seyone22.cook.data.repository.mealEntry.MealEntryDao
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
    entities = [Ingredient::class, IngredientVariant::class, IngredientImage::class, RecipeImage::class, Measure::class, MeasureConversion::class, Recipe::class, Instruction::class, InstructionSection::class, RecipeIngredient::class, ShoppingList::class, ShoppingListItem::class, Tag::class, RecipeTag::class, MealEntry::class, MealEntryTagCrossRef::class, MealEntryIngredientCrossRef::class],
    version = 7,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class CookDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun ingredientVariantDao(): IngredientVariantDao
    abstract fun ingredientImageDao(): IngredientImageDao
    abstract fun recipeImageDao(): RecipeImageDao
    abstract fun measureDao(): MeasureDao
    abstract fun measureConversionDao(): MeasureConversionDao
    abstract fun recipeDao(): RecipeDao
    abstract fun instructionDao(): InstructionDao
    abstract fun instructionSectionDao(): InstructionSectionDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun tagDao(): TagDao
    abstract fun recipeTagDao() : RecipeTagDao
    abstract fun mealEntryDao() : MealEntryDao

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
                    Pair("Breakfast", TagType.MEAL), Pair("Brunch", TagType.MEAL), Pair("Lunch", TagType.MEAL),
                    Pair("Dinner", TagType.MEAL), Pair("Snack", TagType.MEAL), Pair("Appetizer", TagType.MEAL),
                    Pair("Dessert", TagType.MEAL), Pair("Side Dish", TagType.MEAL), Pair("Main Course", TagType.MEAL),
                    Pair("Salad", TagType.MEAL), Pair("Soup", TagType.MEAL), Pair("Beverage", TagType.MEAL),

                    // Cuisines
                    Pair("American", TagType.CUISINE), Pair("Italian", TagType.CUISINE), Pair("Mexican", TagType.CUISINE),
                    Pair("Chinese", TagType.CUISINE), Pair("Indian", TagType.CUISINE), Pair("French", TagType.CUISINE),
                    Pair("Thai", TagType.CUISINE), Pair("Japanese", TagType.CUISINE), Pair("Mediterranean", TagType.CUISINE),
                    Pair("Greek", TagType.CUISINE), Pair("Middle Eastern", TagType.CUISINE), Pair("African", TagType.CUISINE),
                    Pair("Spanish", TagType.CUISINE), Pair("Vietnamese", TagType.CUISINE), Pair("Korean", TagType.CUISINE),
                    Pair("Caribbean", TagType.CUISINE), Pair("Latin American", TagType.CUISINE),

                    // Dietary
                    Pair("Vegetarian", TagType.DIETARY), Pair("Vegan", TagType.DIETARY), Pair("Gluten-Free", TagType.DIETARY),
                    Pair("Dairy-Free", TagType.DIETARY), Pair("Nut-Free", TagType.DIETARY), Pair("Low-Carb", TagType.DIETARY),
                    Pair("Keto", TagType.DIETARY), Pair("Paleo", TagType.DIETARY), Pair("Pescatarian", TagType.DIETARY),
                    Pair("Whole30", TagType.DIETARY), Pair("Sugar-Free", TagType.DIETARY), Pair("Low-Fat", TagType.DIETARY),
                    Pair("High-Protein", TagType.DIETARY), Pair("Low-Sodium", TagType.DIETARY),

                    // Cooking Time
                    Pair("15 Minutes or Less", TagType.TIME), Pair("30 Minutes or Less", TagType.TIME),
                    Pair("Under 1 Hour", TagType.TIME), Pair("Slow Cooker", TagType.TIME), Pair("Instant Pot", TagType.TIME),
                    Pair("Quick & Easy", TagType.TIME), Pair("Make-Ahead", TagType.TIME), Pair("5 Ingredients or Less", TagType.TIME),

                    // Cooking Methods
                    Pair("Baking", TagType.METHODS), Pair("Grilling", TagType.METHODS), Pair("Roasting", TagType.METHODS),
                    Pair("Stir-Frying", TagType.METHODS), Pair("Sautéing", TagType.METHODS), Pair("Steaming", TagType.METHODS),
                    Pair("Boiling", TagType.METHODS), Pair("Broiling", TagType.METHODS), Pair("Pressure Cooking", TagType.METHODS),
                    Pair("Slow Cooking", TagType.METHODS), Pair("Air Fryer", TagType.METHODS), Pair("Sous Vide", TagType.METHODS),
                    Pair("One-Pot", TagType.METHODS), Pair("No-Cook", TagType.METHODS),

                    // Seasonal & Occasions
                    Pair("Spring", TagType.SEASONAL), Pair("Summer", TagType.SEASONAL), Pair("Fall", TagType.SEASONAL),
                    Pair("Winter", TagType.SEASONAL), Pair("Thanksgiving", TagType.SEASONAL), Pair("Christmas", TagType.SEASONAL),
                    Pair("New Year’s Eve", TagType.SEASONAL), Pair("Easter", TagType.SEASONAL), Pair("Halloween", TagType.SEASONAL),
                    Pair("Valentine's Day", TagType.SEASONAL), Pair("Fourth of July", TagType.SEASONAL), Pair("Birthday", TagType.SEASONAL),
                    Pair("Party", TagType.SEASONAL), Pair("Picnic", TagType.SEASONAL), Pair("BBQ", TagType.SEASONAL),
                    Pair("Potluck", TagType.SEASONAL), Pair("Holiday Special", TagType.SEASONAL), Pair("Weeknight Dinner", TagType.SEASONAL),
                    Pair("Comfort Food", TagType.SEASONAL),

                    // Allergies
                    Pair("Egg-Free", TagType.ALLERGIES), Pair("Soy-Free", TagType.ALLERGIES), Pair("Peanut-Free", TagType.ALLERGIES),
                    Pair("Shellfish-Free", TagType.ALLERGIES), Pair("Low FODMAP", TagType.ALLERGIES), Pair("Halal", TagType.ALLERGIES),
                    Pair("Kosher", TagType.ALLERGIES),

                    // Skill Levels
                    Pair("Beginner", TagType.SKILL_LEVEL), Pair("Intermediate", TagType.SKILL_LEVEL), Pair("Advanced", TagType.SKILL_LEVEL),
                    Pair("Kid-Friendly", TagType.SKILL_LEVEL),

                    // HEALTH Goals
                    Pair("Weight Loss", TagType.HEALTH), Pair("Muscle Gain", TagType.HEALTH), Pair("Heart-HEALTHy", TagType.HEALTH),
                    Pair("Diabetic-Friendly", TagType.HEALTH), Pair("High Fiber", TagType.HEALTH), Pair("Detox", TagType.HEALTH),
                    Pair("Low Cholesterol", TagType.HEALTH),

                    // Meal Planning
                    Pair("Meal Prep", TagType.PLANNING), Pair("Freezer-Friendly", TagType.PLANNING), Pair("Leftovers", TagType.PLANNING),
                    Pair("Family-Friendly", TagType.PLANNING), Pair("Budget-Friendly", TagType.PLANNING), Pair("Date Night", TagType.PLANNING),
                    Pair("Kids’ Lunchbox", TagType.PLANNING), Pair("Work-from-Home Lunch", TagType.PLANNING),
                    Pair("Outdoor Cooking", TagType.PLANNING)
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

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add sectionId column to instructions
                db.execSQL("ALTER TABLE instructions ADD COLUMN sectionId INTEGER")

                // Create the instruction_sections table
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS instruction_sections (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                recipeId BLOB NOT NULL,
                name TEXT NOT NULL,
                FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE
            )
            """.trimIndent()
                )
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
                Tag(name = "Breakfast", category = TagType.MEAL),
                Tag(name = "Brunch", category = TagType.MEAL),
                Tag(name = "Lunch", category = TagType.MEAL),
                Tag(name = "Dinner", category = TagType.MEAL),
                Tag(name = "Snack", category = TagType.MEAL),
                Tag(name = "Appetizer", category = TagType.MEAL),
                Tag(name = "Dessert", category = TagType.MEAL),
                Tag(name = "Side Dish", category = TagType.MEAL),
                Tag(name = "Main Course", category = TagType.MEAL),
                Tag(name = "Salad", category = TagType.MEAL),
                Tag(name = "Soup", category = TagType.MEAL),
                Tag(name = "Beverage", category = TagType.MEAL),

                // Cuisines
                Tag(name = "American", category = TagType.CUISINE),
                Tag(name = "Italian", category = TagType.CUISINE),
                Tag(name = "Mexican", category = TagType.CUISINE),
                Tag(name = "Chinese", category = TagType.CUISINE),
                Tag(name = "Indian", category = TagType.CUISINE),
                Tag(name = "French", category = TagType.CUISINE),
                Tag(name = "Thai", category = TagType.CUISINE),
                Tag(name = "Japanese", category = TagType.CUISINE),
                Tag(name = "Mediterranean", category = TagType.CUISINE),
                Tag(name = "Greek", category = TagType.CUISINE),
                Tag(name = "Middle Eastern", category = TagType.CUISINE),
                Tag(name = "African", category = TagType.CUISINE),
                Tag(name = "Spanish", category = TagType.CUISINE),
                Tag(name = "Vietnamese", category = TagType.CUISINE),
                Tag(name = "Korean", category = TagType.CUISINE),
                Tag(name = "Caribbean", category = TagType.CUISINE),
                Tag(name = "Latin American", category = TagType.CUISINE),

                // Dietary
                Tag(name = "Vegetarian", category = TagType.DIETARY),
                Tag(name = "Vegan", category = TagType.DIETARY),
                Tag(name = "Gluten-Free", category = TagType.DIETARY),
                Tag(name = "Dairy-Free", category = TagType.DIETARY),
                Tag(name = "Nut-Free", category = TagType.DIETARY),
                Tag(name = "Low-Carb", category = TagType.DIETARY),
                Tag(name = "Keto", category = TagType.DIETARY),
                Tag(name = "Paleo", category = TagType.DIETARY),
                Tag(name = "Pescatarian", category = TagType.DIETARY),
                Tag(name = "Whole30", category = TagType.DIETARY),
                Tag(name = "Sugar-Free", category = TagType.DIETARY),
                Tag(name = "Low-Fat", category = TagType.DIETARY),
                Tag(name = "High-Protein", category = TagType.DIETARY),
                Tag(name = "Low-Sodium", category = TagType.DIETARY),

                // Cooking Time
                Tag(name = "15 Minutes or Less", category = TagType.TIME),
                Tag(name = "30 Minutes or Less", category = TagType.TIME),
                Tag(name = "Under 1 Hour", category = TagType.TIME),
                Tag(name = "Slow Cooker", category = TagType.TIME),
                Tag(name = "Instant Pot", category = TagType.TIME),
                Tag(name = "Quick & Easy", category = TagType.TIME),
                Tag(name = "Make-Ahead", category = TagType.TIME),
                Tag(name = "5 Ingredients or Less", category = TagType.TIME),

                // Cooking Methods
                Tag(name = "Baking", category = TagType.METHODS),
                Tag(name = "Grilling", category = TagType.METHODS),
                Tag(name = "Roasting", category = TagType.METHODS),
                Tag(name = "Stir-Frying", category = TagType.METHODS),
                Tag(name = "Sautéing", category = TagType.METHODS),
                Tag(name = "Steaming", category = TagType.METHODS),
                Tag(name = "Boiling", category = TagType.METHODS),
                Tag(name = "Broiling", category = TagType.METHODS),
                Tag(name = "Pressure Cooking", category = TagType.METHODS),
                Tag(name = "Slow Cooking", category = TagType.METHODS),
                Tag(name = "Air Fryer", category = TagType.METHODS),
                Tag(name = "Sous Vide", category = TagType.METHODS),
                Tag(name = "One-Pot", category = TagType.METHODS),
                Tag(name = "No-Cook", category = TagType.METHODS),

                // Seasonal & Occasions
                Tag(name = "Spring", category = TagType.SEASONAL),
                Tag(name = "Summer", category = TagType.SEASONAL),
                Tag(name = "Fall", category = TagType.SEASONAL),
                Tag(name = "Winter", category = TagType.SEASONAL),
                Tag(name = "Thanksgiving", category = TagType.SEASONAL),
                Tag(name = "Christmas", category = TagType.SEASONAL),
                Tag(name = "New Year’s Eve", category = TagType.SEASONAL),
                Tag(name = "Easter", category = TagType.SEASONAL),
                Tag(name = "Halloween", category = TagType.SEASONAL),
                Tag(name = "Valentine's Day", category = TagType.SEASONAL),
                Tag(name = "Fourth of July", category = TagType.SEASONAL),
                Tag(name = "Birthday", category = TagType.SEASONAL),
                Tag(name = "Party", category = TagType.SEASONAL),
                Tag(name = "Picnic", category = TagType.SEASONAL),
                Tag(name = "BBQ", category = TagType.SEASONAL),
                Tag(name = "Potluck", category = TagType.SEASONAL),
                Tag(name = "Holiday Special", category = TagType.SEASONAL),
                Tag(name = "Weeknight Dinner", category = TagType.SEASONAL),
                Tag(name = "Comfort Food", category = TagType.SEASONAL),

                // Allergies
                Tag(name = "Egg-Free", category = TagType.ALLERGIES),
                Tag(name = "Soy-Free", category = TagType.ALLERGIES),
                Tag(name = "Peanut-Free", category = TagType.ALLERGIES),
                Tag(name = "Shellfish-Free", category = TagType.ALLERGIES),
                Tag(name = "Low FODMAP", category = TagType.ALLERGIES),
                Tag(name = "Halal", category = TagType.ALLERGIES),
                Tag(name = "Kosher", category = TagType.ALLERGIES),

                // Skill Levels
                Tag(name = "Beginner", category = TagType.SKILL_LEVEL),
                Tag(name = "Intermediate", category = TagType.SKILL_LEVEL),
                Tag(name = "Advanced", category = TagType.SKILL_LEVEL),
                Tag(name = "Kid-Friendly", category = TagType.SKILL_LEVEL),

                // HEALTH Goals
                Tag(name = "Weight Loss", category = TagType.HEALTH),
                Tag(name = "Muscle Gain", category = TagType.HEALTH),
                Tag(name = "Heart-Healthy", category = TagType.HEALTH),
                Tag(name = "Diabetic-Friendly", category = TagType.HEALTH),
                Tag(name = "High Fiber", category = TagType.HEALTH),
                Tag(name = "Detox", category = TagType.HEALTH),
                Tag(name = "Low Cholesterol", category = TagType.HEALTH),

                // Meal Planning
                Tag(name = "Meal Prep", category = TagType.PLANNING),
                Tag(name = "Freezer-Friendly", category = TagType.PLANNING),
                Tag(name = "Leftovers", category = TagType.PLANNING),
                Tag(name = "Family-Friendly", category = TagType.PLANNING),
                Tag(name = "Budget-Friendly", category = TagType.PLANNING),
                Tag(name = "Date Night", category = TagType.PLANNING),
                Tag(name = "Kids’ Lunchbox", category = TagType.PLANNING),
                Tag(name = "Work-from-Home Lunch", category = TagType.PLANNING),
                Tag(name = "Outdoor Cooking", category = TagType.PLANNING)
            )
            initialTags.forEach {
                tagDao.insert(it)
            }
        }
    }
}