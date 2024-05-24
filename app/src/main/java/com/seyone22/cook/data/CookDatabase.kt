package com.seyone22.cook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.repository.ingredient.IngredientDao
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageDao
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantDao

@Database(
    entities = [Ingredient::class, IngredientVariant::class, IngredientImage::class],
    version = 1,
    exportSchema = true
)
abstract class CookDatabase: RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun ingredientVariantDao(): IngredientVariantDao
    abstract fun ingredientImageDao(): IngredientImageDao

    companion object {
        @Volatile
        private var Instance: CookDatabase? = null

        fun getDatabase(context: Context): CookDatabase {
            return Instance?: synchronized(this) {
                Room.databaseBuilder(context, CookDatabase::class.java, "cook_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}