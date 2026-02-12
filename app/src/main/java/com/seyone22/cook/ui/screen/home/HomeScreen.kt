package com.seyone22.cook.ui.screen.home

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.BaseViewModel
import com.seyone22.cook.data.model.IngredientProduct
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.model.RecipeTag
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.recipeTag.RecipeTagRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import com.seyone22.cook.data.repository.tag.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// Define a clean UI state for the screen
data class HomeUiState(
    val filteredRecipes: List<Recipe> = emptyList(),
    val allRecipes: List<Recipe> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val recipeTags: List<RecipeTag> = emptyList(),
    val images: List<RecipeImage> = emptyList(),
    val selectedFilters: Set<Tag> = emptySet(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val tagRepository: TagRepository,
    private val recipeTagRepository: RecipeTagRepository,
    // Unused repositories removed for clarity, inject them back if needed for specific logic
) : BaseViewModel() {

    // Internal state for filters
    private val _selectedFilters = MutableStateFlow<Set<Tag>>(emptySet())

    // -------------------------------------------------------------------
    // Core Logic: Combine streams to produce the UI State automatically
    // -------------------------------------------------------------------
    val uiState: StateFlow<HomeUiState> = combine(
        recipeRepository.getAllRecipes(),
        tagRepository.getAllTags(),
        recipeTagRepository.getAllRecipeTags(),
        recipeImageRepository.getAllRecipeImages(),
        _selectedFilters
    ) { recipes, tags, recipeTags, images, filters ->

        // 1. Filter Logic (Moved from UI)
        val filteredList = if (filters.isEmpty()) {
            recipes.filterNotNull()
        } else {
            recipes.filterNotNull().filter { recipe ->
                // Get all tag IDs for this specific recipe
                val thisRecipeTagIds = recipeTags
                    .filter { it?.recipeId == recipe.id }
                    .map { it?.tagId }

                // Check if the recipe has ANY of the selected filter tags
                thisRecipeTagIds.any { tagId ->
                    filters.any { filterTag -> filterTag.id == tagId }
                }
            }
        }

        HomeUiState(
            filteredRecipes = filteredList,
            allRecipes = recipes.filterNotNull(),
            tags = tags.filterNotNull(),
            recipeTags = recipeTags.filterNotNull(),
            images = images.filterNotNull(),
            selectedFilters = filters,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    // Prices map: ingredientId -> price
    // Note: Kept as SnapshotStateMap for now, but ideally should be part of the flow above.
    val prices: SnapshotStateMap<String, IngredientProduct?> = mutableStateMapOf()

    init {
        // Trigger price fetching whenever the recipe list changes
        viewModelScope.launch {
            recipeIngredientRepository.getAllRecipeIngredients().collect { ingredients ->
                fetchAllPrices(ingredients)
            }
        }
    }

    fun toggleFilter(tag: Tag) {
        val current = _selectedFilters.value
        if (current.contains(tag)) {
            _selectedFilters.value = current - tag
        } else {
            _selectedFilters.value = current + tag
        }
    }

    private fun fetchAllPrices(recipeIngredients: List<RecipeIngredient?>) {
        viewModelScope.launch {
            recipeIngredients.forEach { ri ->
                val ingredientId = ri?.foodDbId ?: return@forEach
                // Avoid re-fetching if we already have it
                if (prices.containsKey(ingredientId)) return@forEach

                try {
                    val product = ingredientVariantRepository.getCheapestPriceForIngredient(ingredientId)
                    prices[ingredientId] = product.firstOrNull()
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Failed to fetch price for $ingredientId", e)
                }
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch { recipeRepository.deleteRecipe(recipe) }
    }

    fun addAllToShoppingList(ingredients: List<RecipeIngredient?>, shoppingListId: Long) {
        viewModelScope.launch {
            ingredients.forEach { ingredient ->
                shoppingListRepository.insertItem(
                    ShoppingListItem(
                        ingredientId = ingredient?.ingredientId ?: UUID.randomUUID(),
                        quantity = ingredient?.quantity ?: 0.0,
                        measureId = -1,
                        shoppingListId = shoppingListId
                    )
                )
            }
        }
    }
}