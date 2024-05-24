package com.seyone22.cook.data.repository.ingredientImage

import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.repository.recipeImage.RecipeImageDao
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import kotlinx.coroutines.flow.Flow

    class OfflineIngredientImageRepository(private val ingredientImageDao: IngredientImageDao):
    IngredientImageRepository {
    override suspend fun insertIngredientImage(ingredientImage: IngredientImage) = ingredientImageDao.insert(ingredientImage)
    override suspend fun deleteIngredientImage(ingredientImage: IngredientImage) = ingredientImageDao.delete(ingredientImage)
    override suspend fun updateIngredientImage(ingredientImage: IngredientImage) = ingredientImageDao.update(ingredientImage)

    override suspend fun getImageById(id: Int): Flow<IngredientImage?> = ingredientImageDao.getImageById(id)
    override suspend fun getImagesForIngredient(ingredientId: Int): Flow<List<IngredientImage?>> = ingredientImageDao.getImagesForIngredient(ingredientId)
    override suspend fun getAllIngredientImages(): Flow<List<IngredientImage?>> = ingredientImageDao.getAllIngredientImages()
}