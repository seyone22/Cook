package com.seyone22.cook.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.ui.screen.home.HomeViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
) : ViewModel() {
    private val _searchViewState = MutableStateFlow(SearchViewState())
    val searchViewState: StateFlow<SearchViewState> get() = _searchViewState

    fun fetchData() {
        viewModelScope.launch {
            val recipes = recipeRepository.getAllRecipes().first()
            val images = recipeImageRepository.getAllRecipeImages().first()

            _searchViewState.value = SearchViewState(recipes, images)
        }
    }
}

data class SearchViewState(
    val recipes: List<Recipe?> = emptyList(),
    val images: List<RecipeImage?> = emptyList(),
)