package com.seyone22.cook.data.repository.recipe

import androidx.room.*
import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipe: Recipe)

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    // Single recipe as Flow
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeByIdFlow(recipeId: UUID): Flow<Recipe?>

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("UPDATE recipes SET timesMade = timesMade + 1 WHERE id = :recipeId")
    suspend fun incrementTimesMade(recipeId: UUID)

    /**
     * Update a recipe with automatic dateModified update.
     */
    @Transaction
    suspend fun updateWithModified(recipe: Recipe) {
        val updatedRecipe = recipe.copy(dateModified = Instant.now())
        update(updatedRecipe)
    }
}
