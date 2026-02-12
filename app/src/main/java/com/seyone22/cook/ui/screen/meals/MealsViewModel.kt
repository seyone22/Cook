package com.seyone22.cook.ui.screen.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.BaseViewModel
import com.seyone22.cook.data.model.MealEntry
import com.seyone22.cook.data.model.MealEntryWithDetails
import com.seyone22.cook.data.repository.mealEntry.MealEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MealsViewModel(
    private val mealEntryRepository: MealEntryRepository,
) : BaseViewModel() {
    // Expose a Flow of MealEntryWithDetails
    private val _mealEntryWithDetails = MutableStateFlow<List<MealEntryWithDetails?>?>(null)
    val mealEntryWithDetails: StateFlow<List<MealEntryWithDetails?>?> = _mealEntryWithDetails

    init {
        // Collect all MealEntries with details
        viewModelScope.launch {
            _mealEntryWithDetails.value = mealEntryRepository.getAllMealEntriesWithDetails()
        }
    }


    fun addImageMealEntry(imageUri: String, onInserted: (Long) -> Unit) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val mealEntry = MealEntry(
                id = 0,
                entryDate = now.toLocalDate(),
                lastUpdated = now,
                imageUri = imageUri,
                notes = "",
                recipeId = null,
            )
            val id = mealEntryRepository.insertMealEntry(mealEntry)
            onInserted(id)
        }
    }

    fun updateMealNotes(id: Long, newNotes: String) {
        viewModelScope.launch {
            mealEntryRepository.updateNotes(id, newNotes)
        }
    }

    fun navigateToRecipe(recipeId: Long) {
        // your nav logic
    }

}
