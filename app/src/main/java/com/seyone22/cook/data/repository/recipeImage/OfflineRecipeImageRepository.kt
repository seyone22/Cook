package com.seyone22.cook.data.repository.recipeImage

import com.seyone22.cook.data.model.RecipeImage
import kotlinx.coroutines.flow.Flow

class OfflineRecipeImageRepository(private val recipeImageDao: RecipeImageDao):
    RecipeImageRepository {
    override suspend fun insertRecipeImage(recipeImage: RecipeImage) = recipeImageDao.insert(recipeImage)
    override suspend fun deleteRecipeImage(recipeImage: RecipeImage) = recipeImageDao.delete(recipeImage)
    override suspend fun updateRecipeImage(recipeImage: RecipeImage) = recipeImageDao.update(recipeImage)

    override suspend fun getImageById(id: Int): Flow<RecipeImage?> = recipeImageDao.getImageById(id)
    override suspend fun getImagesForRecipe(recipeId: Int): Flow<List<RecipeImage?>> = recipeImageDao.getImagesForRecipe(recipeId)
    override suspend fun getAllRecipeImages(): Flow<List<RecipeImage?>> = recipeImageDao.getAllRecipeImages()
}