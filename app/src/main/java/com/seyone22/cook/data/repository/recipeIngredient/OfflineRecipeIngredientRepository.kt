package com.seyone22.cook.data.repository.recipeIngredient

import com.seyone22.cook.data.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineRecipeIngredientRepository(private val recipeIngredientDao: RecipeIngredientDao) :
    RecipeIngredientRepository {
    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient) =
        recipeIngredientDao.insert(recipeIngredient)

    override suspend fun deleteRecipeIngredient(recipeIngredient: RecipeIngredient) =
        recipeIngredientDao.delete(recipeIngredient)

    override suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) =
        recipeIngredientDao.update(recipeIngredient)

    override fun getRecipeIngredientById(id: Int): Flow<RecipeIngredient?> =
        recipeIngredientDao.getRecipeIngredientById(id)

    override fun getRecipeIngredientsForRecipe(recipeId: UUID): Flow<List<RecipeIngredient?>> =
        recipeIngredientDao.getRecipeIngredientsForRecipe(recipeId)

    override fun getAllRecipeIngredients(): Flow<List<RecipeIngredient?>> =
        recipeIngredientDao.getAllRecipeIngredients()

    override suspend fun deleteIngredientsForRecipe(recipeId: UUID) =
        recipeIngredientDao.deleteIngredientsForRecipe(recipeId)

    override fun ingredientIsUsed(ingredientId: UUID): Int =
        recipeIngredientDao.ingredientIsUsed(ingredientId)
}