package com.seyone22.cook.ui.screen.shoppingList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
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
import com.seyone22.cook.parser.parseItemString
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

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

    // --- LEGACY STATE (Keep for Main Screen) ---
    private val _shoppingListViewState = MutableStateFlow(ViewState())
    val shoppingListViewState: StateFlow<ViewState> get() = _shoppingListViewState

    // --- NEW SMART STATE (For Detail Screen) ---
    data class ShoppingListUiState(
        val listName: String = "",
        val shoppingList: ShoppingList? = null,
        val categories: Map<String, List<ShoppingItemDisplay>> = emptyMap(),
        val progress: Float = 0f,
        val isLoading: Boolean = true
    )

    data class ShoppingItemDisplay(
        val item: ShoppingListItem,
        val ingredientName: String,
        val measureName: String,
        val category: String
    )

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState

    // 1. Legacy Fetch (Keep for the 'My Lists' screen)
    fun fetchData() {
        viewModelScope.launch {
            val shoppingLists = shoppingListRepository.getAllShoppingLists().first()
            val shoppingListItems = shoppingListRepository.getAllItems().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            val measures = measureRepository.getAllMeasures().first()

            _shoppingListViewState.value = ViewState(
                measures = measures,
                ingredients = ingredients,
                shoppingLists = shoppingLists,
                shoppingListItems = shoppingListItems,
            )
        }
    }

    // 2. NEW: Reactive Detail Loader
    // 2. NEW: Reactive Detail Loader
    fun loadShoppingListDetails(listId: Long) {
        viewModelScope.launch {
            combine(
                shoppingListRepository.getAllShoppingLists(),
                shoppingListRepository.getAllItems(),
                ingredientRepository.getAllIngredients(),
                measureRepository.getAllMeasures()
            ) { lists, items, ingredients, measures ->
                val currentList = lists.find { it?.id == listId }

                // FIX: Add .filterNotNull() to ensure we don't pass nulls downstream
                val listItems = items.filterNotNull().filter { it.shoppingListId == listId }

                // Map DB objects to Display objects
                val displayItems = listItems.map { item ->
                    val ingredient = ingredients.find { it?.id == item.ingredientId }
                    val measure = measures.find { it?.id == item.measureId }

                    ShoppingItemDisplay(
                        item = item,
                        ingredientName = ingredient?.name ?: "Unknown Item",
                        measureName = measure?.name ?: "",
                        category = ingredient?.category ?: "Uncategorized"
                    )
                }

                // Sort & Group Logic
                val grouped = displayItems.groupBy {
                    if (it.item.checked) "Completed" else it.category
                }.toSortedMap { c1, c2 ->
                    when {
                        c1 == "Completed" -> 1
                        c2 == "Completed" -> -1
                        c1 == "Uncategorized" -> -1
                        c2 == "Uncategorized" -> 1
                        else -> c1.compareTo(c2)
                    }
                }

                val total = displayItems.size
                val checked = displayItems.count { it.item.checked }
                val progress = if (total > 0) checked.toFloat() / total else 0f

                ShoppingListUiState(
                    listName = currentList?.name ?: "",
                    shoppingList = currentList, // Populate this!
                    categories = grouped, // Now matches Map<String, List<ShoppingItemDisplay>>
                    progress = progress,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // 3. SMART ADD: Parse + Merge + Insert
    fun addSmartItem(listId: Long, rawText: String) {
        viewModelScope.launch {
            try {
                // A. Parse the natural language string
                // Returns Triple(quantity: Double, unit: String, name: String)
                val parsed = parseItemString(rawText)

                val qty = parsed.second
                val unitName = parsed.third
                val name = parsed.first

                // B. Find or Create Ingredient
                val allIngredients = ingredientRepository.getAllIngredients().first()
                var ingredient = allIngredients.find { it?.name.equals(name, ignoreCase = true) }

                if (ingredient == null) {
                    // Create new if not found
                    ingredient = Ingredient(
                        id = UUID.randomUUID(),
                        name = name,
                        category = "Uncategorized" // Default category
                    )
                    ingredientRepository.insertIngredient(ingredient)
                }

                // C. Resolve Measure (Unit)
                val allMeasures = measureRepository.getAllMeasures().first()
                // Simple matching strategy: find by name or abbreviation, default to 'pcs' (ID 1)
                val measure = allMeasures.find {
                    it?.name.equals(unitName, ignoreCase = true) ||
                            it?.abbreviation.equals(unitName, ignoreCase = true)
                }
                val measureId = measure?.id ?: 1L // Default to 1 if unit unknown

                // D. Merge Logic: Check if we already have this item in the list
                val existingItems = shoppingListRepository.getAllItems().first()
                    .filter { it?.shoppingListId == listId && it?.ingredientId == ingredient.id }

                if (existingItems.isNotEmpty()) {
                    // Update existing item (Merge quantities)
                    val existing = existingItems.first()
                    if (existing == null) throw Exception("Null existing item")

                    shoppingListRepository.updateItem(existing.copy(quantity = existing.quantity + qty))
                } else {
                    // Insert new item
                    shoppingListRepository.insertItem(
                        ShoppingListItem(
                            shoppingListId = listId,
                            ingredientId = ingredient.id,
                            quantity = qty.toDouble(),
                            measureId = measureId,
                            checked = false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("ShoppingVM", "Error adding smart item", e)
            }
        }
    }

    fun removeFromShoppingList(shoppingListItem: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.deleteItem(shoppingListItem)
        }
    }

    // --- STANDARD ACTIONS ---
    fun toggleItemCheck(item: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.updateItem(item.copy(checked = !item.checked))
        }
    }

    fun updateShoppingListItem(item: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.updateItem(item)
        }
    }

    // Existing helper methods
    fun addShoppingList(shoppingList: ShoppingList) = viewModelScope.launch { shoppingListRepository.insertList(shoppingList) }
    fun completeShoppingList(shoppingList: ShoppingList) = viewModelScope.launch { shoppingListRepository.updateList(shoppingList.copy(completed = !shoppingList.completed)) }
    fun renameShoppingList(shoppingList: ShoppingList) = viewModelScope.launch { shoppingListRepository.updateList(shoppingList) }
    fun deleteShoppingList(shoppingList: ShoppingList) = viewModelScope.launch { shoppingListRepository.deleteList(shoppingList) }
}