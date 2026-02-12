package com.seyone22.cook.data.repository.recipe

import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface RecipeRepository {
    suspend fun insertRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun updateRecipe(recipe: Recipe)

    fun getRecipeById(recipeId: UUID): Flow<Recipe?>
    fun getAllRecipes(): Flow<List<Recipe?>>
    fun incrementTimesMade(recipeId: UUID)
}