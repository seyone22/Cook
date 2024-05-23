package com.seyone22.cook.ui.screen.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientsViewModel : ViewModel() {

    // Define your data repository here
    private val repository = IngredientsRepository() // You'll need to create this class

    // Define StateFlow variable to hold the list of ingredients
    private val _ingredientList = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredientList: StateFlow<List<Ingredient>> get() = _ingredientList

    // Function to fetch the list of ingredients from the repository
    fun getIngredients() {
        viewModelScope.launch {
            repository.getIngredients().collect { ingredients ->
                // Update StateFlow with the fetched ingredients
                _ingredientList.value = ingredients
            }
        }
    }

    // Add other functions for adding/editing/deleting ingredients as needed
    // Example:
    // fun addIngredient(ingredient: Ingredient) { /* Implement logic */ }
    // fun editIngredient(ingredient: Ingredient) { /* Implement logic */ }
    // fun deleteIngredient(ingredientId: Long) { /* Implement logic */ }
}