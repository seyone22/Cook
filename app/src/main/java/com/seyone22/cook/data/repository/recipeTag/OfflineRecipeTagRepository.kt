package com.seyone22.cook.data.repository.recipeTag

import com.seyone22.cook.data.model.RecipeTag
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineRecipeTagRepository(private val recipeTagDao: RecipeTagDao) : RecipeTagRepository {

    override suspend fun insertRecipeTag(recipeTag: RecipeTag) = recipeTagDao.insert(recipeTag)

    override suspend fun updateRecipeTag(recipeTag: RecipeTag) = recipeTagDao.update(recipeTag)

    override suspend fun deleteRecipeTag(recipeTag: RecipeTag) = recipeTagDao.delete(recipeTag)

    override fun getRecipeTagByRecipeId(recipeId: UUID): Flow<List<RecipeTag?>> =
        recipeTagDao.getRecipeTagByRecipeId(recipeId)

    override fun getRecipeTagByTagId(tagId: Long): Flow<List<RecipeTag?>> =
        recipeTagDao.getRecipeTagsByTagId(tagId)

    override fun getAllRecipeTags(): Flow<List<RecipeTag?>> = recipeTagDao.getAllRecipeTags()
}