package com.seyone22.cook.data.repository.mealEntry

import com.seyone22.cook.data.model.MealEntry
import com.seyone22.cook.data.model.MealEntryWithDetails

interface MealEntryRepository {

    suspend fun insertMealEntry(entry: MealEntry): Long

    suspend fun insertTags(entryId: Long, tagIds: List<Long>)

    suspend fun insertIngredients(
        entryId: Long,
        ingredients: List<Triple<Long, Double, String>> // (ingredientId, quantity, unit)
    )

    suspend fun getMealEntryWithDetails(entryId: Long): MealEntryWithDetails?

    suspend fun getAllMealEntriesWithDetails(): List<MealEntryWithDetails>

    suspend fun updateNotes(entryId: Long, newNotes: String)

    suspend fun deleteMealEntry(entryId: Long)
}
