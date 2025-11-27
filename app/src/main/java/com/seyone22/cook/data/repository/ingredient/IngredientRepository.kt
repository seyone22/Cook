package com.seyone22.cook.data.repository.ingredient

import com.seyone22.cook.data.model.Ingredient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IngredientRepository {
    suspend fun insertIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)

    suspend fun getIngredientById(ingredientId: UUID): Flow<Ingredient?>
    suspend fun getAllIngredients(): Flow<List<Ingredient?>>
    suspend fun getIngredientByName(nameEn: String): Flow<Ingredient?>
    suspend fun getIngredientByFoodDbId(foodDbId: String): Flow<Ingredient?>
}