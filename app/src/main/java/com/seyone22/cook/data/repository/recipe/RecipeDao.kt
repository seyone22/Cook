package com.seyone22.cook.data.repository.recipe

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipe: Recipe) : Long
    @Update
    suspend fun update(recipe: Recipe)
    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("SELECT * FROM recipes" +
            "   WHERE id = :recipeId" +
            "   ORDER BY name ASC")
    fun getRecipeById(recipeId: Int): Flow<Recipe>

    @Query("SELECT * FROM recipes" +
            "   ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>
}
