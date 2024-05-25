package com.seyone22.cook.data.repository.recipeIngredient

import com.seyone22.cook.data.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

interface RecipeIngredientRepository {
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)
    suspend fun deleteRecipeIngredient(recipeIngredient: RecipeIngredient)
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)

    suspend fun getRecipeIngredientById(id: Int): Flow<RecipeIngredient?>
    suspend fun getRecipeIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient?>>
    suspend fun getAllRecipeIngredients(): Flow<List<RecipeIngredient?>>
    suspend fun deleteIngredientsForRecipe(recipeId: Int)
    suspend fun ingredientIsUsed(ingredientId: Int): Int
}