package com.seyone22.cook.data.repository.recipe

import com.seyone22.cook.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.util.UUID

class OfflineRecipeRepository(private val recipeDao: RecipeDao) : RecipeRepository {

    override suspend fun insertRecipe(recipe: Recipe) = recipeDao.insert(recipe)

    override suspend fun deleteRecipe(recipe: Recipe) = recipeDao.delete(recipe)

    override suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateWithModified(recipe)

    override fun getRecipeById(recipeId: UUID): Flow<Recipe?> {
        return recipeDao.getRecipeByIdFlow(recipeId)
            .onEach { recipe ->
                recipe?.let {
                    // Update dateAccessed each time the recipe is emitted
                    recipeDao.update(it.copy(dateAccessed = Instant.now()))
                }
            }
    }

    override fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()

    override fun incrementTimesMade(recipeId: UUID) = recipeDao.incrementTimesMade(recipeId)
}