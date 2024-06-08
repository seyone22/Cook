package com.seyone22.cook.data.repository.recipeImage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.RecipeImage
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RecipeImageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipeImage: RecipeImage)
    @Update
    suspend fun update(recipeImage: RecipeImage)
    @Delete
    suspend fun delete(recipeImage: RecipeImage)

    @Query("SELECT * FROM recipe_images" +
            "   WHERE id = :id" +
            "   ORDER BY id ASC")
    fun getImageById(id: Int): Flow<RecipeImage>
    @Query("SELECT * FROM recipe_images" +
            "   WHERE recipeId = :recipeId" +
            "   ORDER BY id ASC")
    fun getImagesForRecipe(recipeId: UUID): Flow<List<RecipeImage>>

    @Query("SELECT * FROM recipe_images" +
            "   ORDER BY id ASC")
    fun getAllRecipeImages(): Flow<List<RecipeImage>>
    @Query("DELETE FROM recipe_images WHERE recipeId = :recipeId")
    suspend fun deleteImagesForRecipe(recipeId: UUID)
}