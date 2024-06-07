package com.seyone22.cook.ui.screen.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.IngredientVariantDetails
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.model.toIngredientVariant
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IngredientsViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val ingredientImageRepository: IngredientImageRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val measureRepository: MeasureRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    // Create a StateFlow to emit the combined data
    private val _ingredientsViewState = MutableStateFlow(ViewState())
    val ingredientsViewState: StateFlow<ViewState> get() = _ingredientsViewState


    // Function to fetch both ingredients and images
    fun fetchData() {
        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllIngredients().first()
            val variants = ingredientVariantRepository.getAllIngredientVariants().first()
            val images = ingredientImageRepository.getAllIngredientImages().first()
            val measures = measureRepository.getAllMeasures().first()
            val shoppingLists = shoppingListRepository.getAllShoppingLists().first()
            _ingredientsViewState.value =
                ViewState(
                    ingredients = ingredients,
                    variants = variants,
                    ingredientImages = images,
                    measures = measures,
                    shoppingLists = shoppingLists
                )
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
    fun addToShoppingList(it: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.insertItem(it)
        }
    }
}