package com.seyone22.cook.data.repository.ingredientImage

import com.seyone22.cook.data.model.IngredientImage
import kotlinx.coroutines.flow.Flow

interface IngredientImageRepository {
    suspend fun insertIngredientImage(ingredientImage: IngredientImage)
    suspend fun deleteIngredientImage(ingredientImage: IngredientImage)
    suspend fun updateIngredientImage(ingredientImage: IngredientImage)

    suspend fun getImageById(id: Int): Flow<IngredientImage?>
    suspend fun getImagesForIngredient(ingredientId: Int): Flow<List<IngredientImage?>>
    suspend fun getAllIngredientImages(): Flow<List<IngredientImage?>>
}