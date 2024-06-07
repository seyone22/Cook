package com.seyone22.cook.ui.screen.shoppingList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val measureRepository: MeasureRepository,
    private val ingredientRepository: IngredientRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    private val _shoppingListViewState = MutableStateFlow(ViewState())
    val shoppingListViewState: StateFlow<ViewState> get() = _shoppingListViewState

    fun fetchData() {
        viewModelScope.launch {
            val shoppingLists = shoppingListRepository.getAllShoppingLists().first()
            val shoppingListItems = shoppingListRepository.getAllItems().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            val measures = measureRepository.getAllMeasures().first()

            _shoppingListViewState.value = ViewState(
                shoppingLists = shoppingLists,
                shoppingListItems = shoppingListItems,
                ingredients = ingredients,
                measures = measures
            )
        }
    }

    fun addShoppingList(shoppingList: ShoppingList) {
        viewModelScope.launch {
            shoppingListRepository.insertList(shoppingList)
        }
    }

    fun addToShoppingList(shoppingListItem: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.insertItem(shoppingListItem)
        }
    }
}

