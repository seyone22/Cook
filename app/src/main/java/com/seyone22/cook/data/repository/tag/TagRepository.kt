package com.seyone22.cook.data.repository.tag

import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    suspend fun insertTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
    fun getTagById(id: Long): Flow<Tag?>
    fun getAllTags(): Flow<List<Tag?>>
}