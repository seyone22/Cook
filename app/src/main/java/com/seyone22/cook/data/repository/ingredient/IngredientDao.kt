package com.seyone22.cook.data.repository.ingredient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Ingredient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ingredient: Ingredient)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query(
        "SELECT * FROM ingredients" +
                "   WHERE id = :ingredientId" +
                "   ORDER BY name ASC"
    )
    fun getIngredientById(ingredientId: UUID): Flow<Ingredient?>

    @Query(
        "SELECT * FROM ingredients" +
                "   ORDER BY name ASC"
    )
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query(
        "SELECT * FROM ingredients" +
                "   WHERE name = :name" +
                "   ORDER BY name ASC"
    )
    fun getIngredientByName(name: String): Flow<Ingredient?>

    @Query(
        "SELECT * FROM ingredients" +
                "   WHERE foodDbId = :foodDbId" +
                "   ORDER BY name ASC"
    )
    fun getIngredientByFoodDbId(foodDbId: String): Flow<Ingredient?>
}
