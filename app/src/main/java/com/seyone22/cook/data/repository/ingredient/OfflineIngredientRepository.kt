package com.seyone22.cook.data.repository.ingredient

import com.seyone22.cook.data.model.Ingredient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineIngredientRepository(private val ingredientDao: IngredientDao) : IngredientRepository {
    override suspend fun insertIngredient(ingredient: Ingredient) =
        ingredientDao.insert(ingredient)

    override suspend fun deleteIngredient(ingredient: Ingredient) = ingredientDao.delete(ingredient)
    override suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.update(ingredient)

    override suspend fun getIngredientById(ingredientId: UUID): Flow<Ingredient?> =
        ingredientDao.getIngredientById(ingredientId)

    override suspend fun getAllIngredients(): Flow<List<Ingredient?>> =
        ingredientDao.getAllIngredients()

    override suspend fun getIngredientByName(nameEn: String): Flow<Ingredient?> =
        ingredientDao.getIngredientByName(nameEn)
}