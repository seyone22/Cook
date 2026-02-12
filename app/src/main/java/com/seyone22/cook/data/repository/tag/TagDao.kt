package com.seyone22.cook.data.repository.tag

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag) : Long

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getTagById(id: Long): Flow<Tag>

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE name = :name")
    fun getTagByName(name: String): Flow<Tag?>
}