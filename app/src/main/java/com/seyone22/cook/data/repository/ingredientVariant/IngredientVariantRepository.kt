package com.seyone22.cook.data.repository.ingredientVariant

import com.seyone22.cook.data.model.IngredientProduct
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IngredientVariantRepository {
    suspend fun insertIngredientVariant(ingredientProduct: IngredientProduct)
    suspend fun deleteIngredientVariant(ingredientProduct: IngredientProduct)
    suspend fun updateIngredientVariant(ingredientProduct: IngredientProduct)

    suspend fun getVariantById(id: Int): Flow<IngredientProduct?>
    suspend fun getVariantsForIngredient(ingredientId: UUID): Flow<List<IngredientProduct?>>
    suspend fun getAllIngredientVariants(): Flow<List<IngredientProduct?>>

    suspend fun getCheapestPriceForIngredient(ingredientId: String): Flow<IngredientProduct?>
    suspend fun getIngredientProductByUniqueId(uniqueId: String): Flow<IngredientProduct?>
}