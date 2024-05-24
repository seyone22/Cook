package com.seyone22.cook.ui.screen.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IngredientsViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val ingredientImageRepository: IngredientImageRepository
) : ViewModel() {
    // Create a StateFlow to emit the combined data
    private val _ingredientsViewState = MutableStateFlow(IngredientsViewState())
    val ingredientsViewState: StateFlow<IngredientsViewState> get() = _ingredientsViewState


    // Function to fetch both ingredients and images
    fun fetchIngredientsAndImages() {
        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllIngredients().first()
            val variants = ingredientVariantRepository.getAllIngredientVariants().first()
            val images = ingredientImageRepository.getAllIngredientImages().first()
            _ingredientsViewState.value = IngredientsViewState(ingredients, images, variants)
        }
    }
}



// Define a data class to hold both the list of ingredients and images
data class IngredientsViewState(
    val ingredients: List<Ingredient?> = emptyList(),
    val images: List<IngredientImage?> = emptyList(),
    val variants: List<IngredientVariant?> = emptyList()
)