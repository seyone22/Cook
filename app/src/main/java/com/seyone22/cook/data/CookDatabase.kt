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
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientDao
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageDao
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantDao
import com.seyone22.cook.data.repository.instruction.InstructionDao
import com.seyone22.cook.data.repository.measure.MeasureDao
import com.seyone22.cook.data.repository.recipe.RecipeDao
import com.seyone22.cook.data.repository.recipeImage.RecipeImageDao
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Ingredient::class, IngredientVariant::class, IngredientImage::class, RecipeImage::class, Measure::class, Recipe::class, Instruction::class, RecipeIngredient::class],
    version = 2,
    exportSchema = true
)
abstract class CookDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun ingredientVariantDao(): IngredientVariantDao
    abstract fun ingredientImageDao(): IngredientImageDao
    abstract fun recipeImageDao(): RecipeImageDao
    abstract fun measureDao(): MeasureDao
    abstract fun recipeDao(): RecipeDao
    abstract fun instructionDao(): InstructionDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao

    companion object {
        @Volatile
        private var Instance: CookDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CookDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CookDatabase::class.java, "cook_database")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
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
    }

    private class CookDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
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

            val initialUnits = listOf(
                Measure(id = 1, abbreviation = "g", name = "gram"),
                Measure(id = 2, abbreviation = "kg", name = "kilogram"),
                Measure(id = 3, abbreviation = "ml", name = "milliliter"),
                Measure(id = 4, abbreviation = "l", name = "liter"),
                Measure(id = 5, abbreviation = "tsp", name = "teaspoon"),
                Measure(id = 6, abbreviation = "tbsp", name = "tablespoon"),
                Measure(id = 7, abbreviation = "cup", name = "cup")
            )

            initialUnits.forEach {
                measureDao.insert(it)
                // Similarly, you can insert initial data for other tables
            }
        }
    }
}