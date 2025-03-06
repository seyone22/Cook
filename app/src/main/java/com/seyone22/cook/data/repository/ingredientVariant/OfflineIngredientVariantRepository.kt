package com.seyone22.cook.data.repository.ingredientVariant

import com.seyone22.cook.data.model.IngredientVariant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineIngredientVariantRepository(private val ingredientVariantDao: IngredientVariantDao) :
    IngredientVariantRepository {
    override suspend fun insertIngredientVariant(ingredientVariant: IngredientVariant) =
        ingredientVariantDao.insert(ingredientVariant)

    override suspend fun deleteIngredientVariant(ingredientVariant: IngredientVariant) =
        ingredientVariantDao.delete(ingredientVariant)

    override suspend fun updateIngredientVariant(ingredientVariant: IngredientVariant) =
        ingredientVariantDao.update(ingredientVariant)

    override suspend fun getVariantById(id: Int): Flow<IngredientVariant?> =
        ingredientVariantDao.getVariantById(id)

    override suspend fun getVariantsForIngredient(ingredientId: UUID): Flow<List<IngredientVariant?>> =
        ingredientVariantDao.getVariantsForIngredient(ingredientId)

    override suspend fun getAllIngredientVariants(): Flow<List<IngredientVariant?>> =
        ingredientVariantDao.getAllIngredientVariants()
}