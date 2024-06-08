package com.seyone22.cook.data.repository.ingredient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ingredient: Ingredient) : Long
    @Update
    suspend fun update(ingredient: Ingredient)
    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients" +
            "   WHERE id = :ingredientId" +
            "   ORDER BY nameEn ASC")
    fun getIngredientById(ingredientId: Int): Flow<Ingredient>

    @Query("SELECT * FROM ingredients" +
            "   ORDER BY nameEn ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>
    @Query("SELECT * FROM ingredients" +
            "   WHERE nameEn = :nameEn" +
            "   ORDER BY nameEn ASC")
    fun getIngredientByName(nameEn: String): Flow<Ingredient?>
}
