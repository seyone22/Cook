package com.seyone22.cook.data.repository.recipeTag

import com.seyone22.cook.data.model.RecipeTag
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface RecipeTagRepository {
    suspend fun insertRecipeTag(recipeTag: RecipeTag)
    suspend fun updateRecipeTag(recipeTag: RecipeTag)
    suspend fun deleteRecipeTag(recipeTag: RecipeTag)
    fun getRecipeTagByRecipeId(recipeId: UUID): Flow<List<RecipeTag?>>
    fun getRecipeTagByTagId(tagId: Long): Flow<List<RecipeTag?>>
    fun getAllRecipeTags(): Flow<List<RecipeTag?>>
}