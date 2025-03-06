package com.seyone22.cook.data.repository.ingredientImage

import com.seyone22.cook.data.model.IngredientImage
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineIngredientImageRepository(private val ingredientImageDao: IngredientImageDao) :
    IngredientImageRepository {
    override suspend fun insertIngredientImage(ingredientImage: IngredientImage) =
        ingredientImageDao.insert(ingredientImage)

    override suspend fun deleteIngredientImage(ingredientImage: IngredientImage) =
        ingredientImageDao.delete(ingredientImage)

    override suspend fun updateIngredientImage(ingredientImage: IngredientImage) =
        ingredientImageDao.update(ingredientImage)

    override suspend fun getImageById(id: Int): Flow<IngredientImage?> =
        ingredientImageDao.getImageById(id)

    override suspend fun getImagesForIngredient(ingredientId: UUID): Flow<List<IngredientImage?>> =
        ingredientImageDao.getImagesForIngredient(ingredientId)

    override suspend fun getAllIngredientImages(): Flow<List<IngredientImage?>> =
        ingredientImageDao.getAllIngredientImages()
}