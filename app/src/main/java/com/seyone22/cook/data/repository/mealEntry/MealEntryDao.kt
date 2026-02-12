package com.seyone22.cook.data.repository.mealEntry

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.seyone22.cook.data.model.IngredientWithQuantity
import com.seyone22.cook.data.model.MealEntry
import com.seyone22.cook.data.model.MealEntryIngredientCrossRef
import com.seyone22.cook.data.model.MealEntryTagCrossRef
import com.seyone22.cook.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealEntry(entry: MealEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagCrossRefs(refs: List<MealEntryTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientCrossRefs(refs: List<MealEntryIngredientCrossRef>)

    @Query("SELECT * FROM meal_entries WHERE id = :id")
    suspend fun getMealEntryById(id: Long): MealEntry?

    @Transaction
    @Query("SELECT * FROM meal_entries")
    suspend fun getAllMealEntries(): List<MealEntry>


    @Transaction
    @Query("""
        SELECT t.*
        FROM tags t
        INNER JOIN meal_entry_tag_cross_ref mt ON t.id = mt.tagId
        WHERE mt.mealEntryId = :mealEntryId
    """)
    suspend fun getTagsForMealEntry(mealEntryId: Long): List<Tag>

    @Transaction
    @Query("""
        SELECT i.*, x.quantity, x.unit
        FROM ingredients i
        INNER JOIN meal_entry_ingredient_cross_ref x ON i.id = x.ingredientId
        WHERE x.mealEntryId = :mealEntryId
    """)
    suspend fun getIngredientsForMealEntry(mealEntryId: Long): List<IngredientWithQuantity>

    @Query("UPDATE meal_entries SET notes = :newNotes WHERE id = :entryId")
    suspend fun updateNotes(entryId: Long, newNotes: String)

    @Query("DELETE FROM meal_entries WHERE id = :mealEntryId")
    suspend fun deleteMealEntry(mealEntryId: Long)
}
