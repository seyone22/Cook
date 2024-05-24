package com.seyone22.cook.data.repository.recipeIngredient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipeIngredient: RecipeIngredient)
    @Update
    suspend fun update(recipeIngredient: RecipeIngredient)
    @Delete
    suspend fun delete(recipeIngredient: RecipeIngredient)

    @Query("SELECT * FROM recipe_ingredients" +
            "   WHERE id = :id" +
            "   ORDER BY id ASC")
    fun getRecipeIngredientById(id: Int): Flow<RecipeIngredient>
    @Query("SELECT * FROM recipe_ingredients" +
            "   WHERE recipeId = :recipeId" +
            "   ORDER BY id ASC")
    fun getRecipeIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>>
    @Query("SELECT * FROM recipe_ingredients" +
            "   ORDER BY id ASC")
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>>
}