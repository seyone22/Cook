package com.seyone22.cook.data.repository.ingredientVariant

import com.seyone22.cook.data.model.IngredientVariant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IngredientVariantRepository {
    suspend fun insertIngredientVariant(ingredientVariant: IngredientVariant)
    suspend fun deleteIngredientVariant(ingredientVariant: IngredientVariant)
    suspend fun updateIngredientVariant(ingredientVariant: IngredientVariant)

    suspend fun getVariantById(id: Int): Flow<IngredientVariant?>
    suspend fun getVariantsForIngredient(ingredientId: UUID): Flow<List<IngredientVariant?>>
    suspend fun getAllIngredientVariants(): Flow<List<IngredientVariant?>>
}