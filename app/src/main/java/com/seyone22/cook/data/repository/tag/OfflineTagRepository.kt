package com.seyone22.cook.data.repository.tag

import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.repository.tag.TagDao
import com.seyone22.cook.data.repository.tag.TagRepository
import kotlinx.coroutines.flow.Flow

class OfflineTagRepository(private val tagDao: TagDao) :
    TagRepository {

    override suspend fun insertTag(tag: Tag) =
        tagDao.insert(tag)

    override suspend fun updateTag(tag: Tag) =
        tagDao.update(tag)

    override suspend fun deleteTag(tag: Tag) =
        tagDao.delete(tag)

    override fun getTagById(id: Long): Flow<Tag?> =
        tagDao.getTagById(id)

    override fun getAllTags(): Flow<List<Tag?>> =
        tagDao.getAllTags()
}