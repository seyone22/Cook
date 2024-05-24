package com.seyone22.cook.data.repository.recipe

import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun updateRecipe(recipe: Recipe)

    suspend fun getRecipeById(recipeId: Int): Flow<Recipe?>
    suspend fun getAllRecipes(): Flow<List<Recipe?>>
}