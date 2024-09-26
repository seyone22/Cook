package com.seyone22.cook.data

import android.content.Context
import android.util.Log
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
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.repository.ingredient.IngredientDao
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageDao
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantDao
import com.seyone22.cook.data.repository.instruction.InstructionDao
import com.seyone22.cook.data.repository.measure.MeasureDao
import com.seyone22.cook.data.repository.measureConversion.MeasureConversionDao
import com.seyone22.cook.data.repository.recipe.RecipeDao
import com.seyone22.cook.data.repository.recipeImage.RecipeImageDao
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientDao
import com.seyone22.cook.data.repository.shoppingList.ShoppingListDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(
    entities = [Ingredient::class, IngredientVariant::class, IngredientImage::class, RecipeImage::class, Measure::class, MeasureConversion::class, Recipe::class, Instruction::class, RecipeIngredient::class, ShoppingList::class, ShoppingListItem::class],
    version = 3,
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

    companion object {
        @Volatile
        private var Instance: CookDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CookDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CookDatabase::class.java, "cook_database")
                    .setQueryCallback({ sqlQuery, bindArgs ->
                        Log.d("SQL Query", "SQL: $sqlQuery, Args: $bindArgs ")
                    }, Executors.newSingleThreadExecutor())
                    .addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_1_3).fallbackToDestructiveMigration()
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
        }
    }
}

// TODO: Migration for converting id from long to UUID (text)