package com.seyone22.cook.data.repository.ingredientImage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.IngredientImage
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientImageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ingredientImage: IngredientImage)
    @Update
    suspend fun update(ingredientImage: IngredientImage)
    @Delete
    suspend fun delete(ingredientImage: IngredientImage)

    @Query("SELECT * FROM ingredient_images" +
            "   WHERE id = :id" +
            "   ORDER BY id ASC")
    fun getImageById(id: Int): Flow<IngredientImage>
    @Query("SELECT * FROM ingredient_images" +
            "   WHERE ingredientId = :ingredientId" +
            "   ORDER BY id ASC")
    fun getImagesForIngredient(ingredientId: Int): Flow<List<IngredientImage>>
    @Query("SELECT * FROM ingredient_images" +
            "   ORDER BY id ASC")
    fun getAllIngredientImages(): Flow<List<IngredientImage>>
}