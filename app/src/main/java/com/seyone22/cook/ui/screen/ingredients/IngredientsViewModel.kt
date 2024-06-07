package com.seyone22.cook.ui.screen.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.IngredientVariantDetails
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.toIngredientVariant
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IngredientsViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val ingredientImageRepository: IngredientImageRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val measureRepository: MeasureRepository
) : ViewModel() {
    // Create a StateFlow to emit the combined data
    private val _ingredientsViewState = MutableStateFlow(IngredientsViewState())
    val ingredientsViewState: StateFlow<IngredientsViewState> get() = _ingredientsViewState


    // Function to fetch both ingredients and images
    fun fetchData() {
        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllIngredients().first()
            val variants = ingredientVariantRepository.getAllIngredientVariants().first()
            val images = ingredientImageRepository.getAllIngredientImages().first()
            val measures = measureRepository.getAllMeasures().first()
            _ingredientsViewState.value =
                IngredientsViewState(ingredients, images, variants, measures)
        }
    }

    suspend fun deleteIngredient(ingredient: Ingredient): Boolean {
        val usedCount = recipeIngredientRepository.ingredientIsUsed(ingredient.id.toInt())
        if (usedCount == 0) {
            ingredientRepository.deleteIngredient(ingredient)
            return true
        } else {
            return false
        }
    }

    fun updateStock(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientRepository.updateIngredient(ingredient)
        }
    }

    fun addVariant(ingredientId: Long, variant: IngredientVariantDetails) {
        viewModelScope.launch {
            ingredientVariantRepository.insertIngredientVariant(
                variant.toIngredientVariant().copy(ingredientId = ingredientId)
            )
        }
    }
}


// Define a data class to hold both the list of ingredients and images
data class IngredientsViewState(
    val ingredients: List<Ingredient?> = emptyList(),
    val images: List<IngredientImage?> = emptyList(),
    val variants: List<IngredientVariant?> = emptyList(),
    val measures: List<Measure?> = emptyList()
)