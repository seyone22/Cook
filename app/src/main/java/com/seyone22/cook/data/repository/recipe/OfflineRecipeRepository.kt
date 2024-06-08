package com.seyone22.cook.data.repository.recipe

import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineRecipeRepository(private val recipeDao: RecipeDao): RecipeRepository {
    override suspend fun insertRecipe(recipe: Recipe) = recipeDao.insert(recipe)
    override suspend fun deleteRecipe(recipe: Recipe) = recipeDao.delete(recipe)
    override suspend fun updateRecipe(recipe: Recipe) = recipeDao.update(recipe)

    override suspend fun getRecipeById(recipeId: UUID): Flow<Recipe?> = recipeDao.getRecipeById(recipeId)
    override suspend fun getAllRecipes(): Flow<List<Recipe?>> = recipeDao.getAllRecipes()
    override suspend fun incrementTimesMade(recipeId: UUID) = recipeDao.incrementTimesMade(recipeId)
}