package com.seyone22.cook.data.repository.ingredientVariant

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.IngredientProduct
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface IngredientVariantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ingredientProduct: IngredientProduct)

    @Update
    suspend fun update(ingredientProduct: IngredientProduct)

    @Delete
    suspend fun delete(ingredientProduct: IngredientProduct)

    @Query(
        "SELECT * FROM ingredient_variants" +
                "   WHERE id = :id" +
                "   ORDER BY productName ASC"
    )
    fun getVariantById(id: Int): Flow<IngredientProduct>

    @Query(
        "SELECT * FROM ingredient_variants" +
                "   WHERE ingredientId = :ingredientId" +
                "   ORDER BY productName ASC"
    )
    fun getVariantsForIngredient(ingredientId: UUID): Flow<List<IngredientProduct>>

    @Query(
        "SELECT * FROM ingredient_variants" +
                "   ORDER BY productName ASC"
    )
    fun getAllIngredientVariants(): Flow<List<IngredientProduct>>


    @Query("SELECT * \n" +
            "FROM ingredient_variants \n" +
            "WHERE ingredientId = :ingredientId\n" +
            "  AND price IS NOT NULL\n" +
            "  AND price > 0\n" +
            "ORDER BY price ASC\n" +
            "LIMIT 1\n")
    fun getCheapestPriceForIngredient(ingredientId: String): Flow<IngredientProduct?>

    @Query("SELECT * FROM ingredient_variants WHERE uniqueId = :uniqueId LIMIT 1")
    fun getIngredientProductByUniqueId(uniqueId: String): Flow<IngredientProduct?>
}