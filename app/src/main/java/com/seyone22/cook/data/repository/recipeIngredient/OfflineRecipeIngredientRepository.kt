package com.seyone22.cook.data.repository.recipeIngredient

import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientDao
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import kotlinx.coroutines.flow.Flow

class OfflineRecipeIngredientRepository(private val recipeIngredientDao: RecipeIngredientDao):
    RecipeIngredientRepository {
    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient) = recipeIngredientDao.insert(recipeIngredient)
    override suspend fun deleteRecipeIngredient(recipeIngredient: RecipeIngredient) = recipeIngredientDao.delete(recipeIngredient)
    override suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) = recipeIngredientDao.update(recipeIngredient)

    override suspend fun getRecipeIngredientById(id: Int): Flow<RecipeIngredient?> = recipeIngredientDao.getRecipeIngredientById(id)
    override suspend fun getRecipeIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient?>> = recipeIngredientDao.getRecipeIngredientsForRecipe(recipeId)
    override suspend fun getAllRecipeIngredients(): Flow<List<RecipeIngredient?>> = recipeIngredientDao.getAllRecipeIngredients()
    override suspend fun deleteIngredientsForRecipe(recipeId: Int) = recipeIngredientDao.deleteIngredientsForRecipe(recipeId)
    override suspend fun ingredientIsUsed(ingredientId: Int): Int = recipeIngredientDao.ingredientIsUsed(ingredientId)
}