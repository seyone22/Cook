package com.seyone22.cook.data.repository.recipe

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipe: Recipe)
    @Update
    suspend fun update(recipe: Recipe)
    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("SELECT * FROM recipes" +
            "   WHERE id = :recipeId" +
            "   ORDER BY name ASC")
    fun getRecipeById(recipeId: UUID): Flow<Recipe>
    @Query("SELECT * FROM recipes" +
            "   ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>
    @Query("UPDATE recipes SET timesMade = timesMade + 1 WHERE id = :recipeId")
    suspend fun incrementTimesMade(recipeId: UUID)
}
