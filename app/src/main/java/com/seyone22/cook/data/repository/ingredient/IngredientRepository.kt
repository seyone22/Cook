package com.seyone22.cook.data.repository.ingredient

import com.seyone22.cook.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    suspend fun insertIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)

    suspend fun getIngredientById(ingredientId: Int): Flow<Ingredient?>
    suspend fun getAllIngredients(): Flow<List<Ingredient?>>
}