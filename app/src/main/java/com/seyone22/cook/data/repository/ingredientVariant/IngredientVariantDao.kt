package com.seyone22.cook.data.repository.ingredientVariant

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.IngredientVariant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface IngredientVariantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ingredientVariant: IngredientVariant)

    @Update
    suspend fun update(ingredientVariant: IngredientVariant)

    @Delete
    suspend fun delete(ingredientVariant: IngredientVariant)

    @Query(
        "SELECT * FROM ingredient_variants" +
                "   WHERE id = :id" +
                "   ORDER BY variantName ASC"
    )
    fun getVariantById(id: Int): Flow<IngredientVariant>

    @Query(
        "SELECT * FROM ingredient_variants" +
                "   WHERE ingredientId = :ingredientId" +
                "   ORDER BY variantName ASC"
    )
    fun getVariantsForIngredient(ingredientId: UUID): Flow<List<IngredientVariant>>

    @Query(
        "SELECT * FROM ingredient_variants" +
                "   ORDER BY variantName ASC"
    )
    fun getAllIngredientVariants(): Flow<List<IngredientVariant>>
}