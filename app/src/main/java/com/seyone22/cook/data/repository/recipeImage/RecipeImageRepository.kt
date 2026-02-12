package com.seyone22.cook.data.repository.recipeImage

import com.seyone22.cook.data.model.RecipeImage
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface RecipeImageRepository {
    suspend fun insertRecipeImage(recipeImage: RecipeImage)
    suspend fun deleteRecipeImage(recipeImage: RecipeImage)
    suspend fun updateRecipeImage(recipeImage: RecipeImage)

    fun getImageById(id: Int): Flow<RecipeImage?>
    fun getImagesForRecipe(recipeId: UUID): Flow<List<RecipeImage?>>
    fun getAllRecipeImages(): Flow<List<RecipeImage?>>
    fun deleteImagesForRecipe(recipeId: UUID)
}