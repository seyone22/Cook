package com.seyone22.cook.data.repository.recipeImage

import com.seyone22.cook.data.model.RecipeImage
import kotlinx.coroutines.flow.Flow

interface RecipeImageRepository {
    suspend fun insertRecipeImage(recipeImage: RecipeImage)
    suspend fun deleteRecipeImage(recipeImage: RecipeImage)
    suspend fun updateRecipeImage(recipeImage: RecipeImage)

    suspend fun getImageById(id: Int): Flow<RecipeImage?>
    suspend fun getImagesForRecipe(recipeId: Int): Flow<List<RecipeImage?>>
    suspend fun getAllRecipeImages(): Flow<List<RecipeImage?>>
    suspend fun deleteImagesForRecipe(recipeId: Int)
}