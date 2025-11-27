package com.seyone22.cook.data.repository.ingredientVariant

import com.seyone22.cook.data.model.IngredientProduct
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineIngredientVariantRepository(private val ingredientVariantDao: IngredientVariantDao) :
    IngredientVariantRepository {
    override suspend fun insertIngredientVariant(ingredientProduct: IngredientProduct) =
        ingredientVariantDao.insert(ingredientProduct)

    override suspend fun deleteIngredientVariant(ingredientProduct: IngredientProduct) =
        ingredientVariantDao.delete(ingredientProduct)

    override suspend fun updateIngredientVariant(ingredientProduct: IngredientProduct) =
        ingredientVariantDao.update(ingredientProduct)

    override suspend fun getVariantById(id: Int): Flow<IngredientProduct?> =
        ingredientVariantDao.getVariantById(id)

    override suspend fun getVariantsForIngredient(ingredientId: UUID): Flow<List<IngredientProduct?>> =
        ingredientVariantDao.getVariantsForIngredient(ingredientId)

    override suspend fun getAllIngredientVariants(): Flow<List<IngredientProduct?>> =
        ingredientVariantDao.getAllIngredientVariants()

    override suspend fun getCheapestPriceForIngredient(ingredientId: String): Flow<IngredientProduct?> =
        ingredientVariantDao.getCheapestPriceForIngredient(ingredientId)

    override suspend fun getIngredientProductByUniqueId(uniqueId: String): Flow<IngredientProduct?> =
        ingredientVariantDao.getIngredientProductByUniqueId(uniqueId)
}