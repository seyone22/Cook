package com.seyone22.cook.data.repository.recipeTag

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.RecipeTag
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RecipeTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipeTag: RecipeTag)

    @Update
    suspend fun update(recipeTag: RecipeTag)

    @Delete
    suspend fun delete(recipeTag: RecipeTag)

    @Query("SELECT * FROM recipe_tags WHERE recipeId = :recipeId")
    fun getRecipeTagByRecipeId(recipeId: UUID): Flow<List<RecipeTag>>

    @Query("SELECT * FROM recipe_tags WHERE tagId = :tagId")
    fun getRecipeTagsByTagId(tagId: Long): Flow<List<RecipeTag>>

    @Query("SELECT * FROM recipe_tags")
    fun getAllRecipeTags(): Flow<List<RecipeTag>>
}