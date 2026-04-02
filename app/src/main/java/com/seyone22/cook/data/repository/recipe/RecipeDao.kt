package com.seyone22.cook.data.repository.recipe

import androidx.room.*
import com.seyone22.cook.data.model.FullRecipe
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteById(recipeId: UUID)

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeByIdFlow(recipeId: UUID): Flow<Recipe?>

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    /**
     * Used by the Sync Engine to get a one-time snapshot of the cookbook.
     */
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesSnapshot(): List<Recipe>

    @Query("UPDATE recipes SET timesMade = timesMade + 1 WHERE id = :recipeId")
    suspend fun incrementTimesMade(recipeId: UUID)

    @Transaction
    suspend fun updateWithModified(recipe: Recipe) {
        val updatedRecipe = recipe.copy(dateModified = Instant.now())
        update(updatedRecipe)
    }

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getFullRecipeSnapshot(recipeId: UUID): FullRecipe?

    /**
     * CLEAR AND REPLACE:
     * When downloading a recipe from the cloud, we want to wipe the local
     * version of its ingredients/instructions to ensure we don't have duplicates.
     */
    @Transaction
    suspend fun clearAndInsertFullRecipe(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<Instruction>,
        sections: List<InstructionSection>
    ) {
        // 1. Delete existing relations to avoid orphaned data
        // (Note: Requires adding these delete queries to their respective DAOs or here)
        deleteRelationsForRecipe(recipe.id)

        // 2. Insert the fresh data
        insert(recipe)
        // You would call the respective DAOs for these inserts:
        // ingredientDao.insertAll(ingredients)
        // instructionDao.insertAll(instructions)
    }

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: UUID)

    @Query("DELETE FROM instructions WHERE recipeId = :recipeId")
    suspend fun deleteInstructionsForRecipe(recipeId: UUID)

    @Query("DELETE FROM instruction_sections WHERE recipeId = :recipeId")
    suspend fun deleteSectionsForRecipe(recipeId: UUID)

    @Transaction
    suspend fun deleteFullRecipeData(recipeId: UUID) {
        deleteIngredientsForRecipe(recipeId)
        deleteInstructionsForRecipe(recipeId)
        deleteSectionsForRecipe(recipeId)
        deleteById(recipeId)
    }

    @Transaction
    suspend fun deleteRelationsForRecipe(recipeId: UUID) {
        deleteIngredientsForRecipe(recipeId)
        deleteInstructionsForRecipe(recipeId)
    }
}