package com.seyone22.cook.ui.screen.home.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.instructionsection.InstructionSectionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val images: List<RecipeImage> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val baseIngredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList(),
    val instructionSections: List<InstructionSection> = emptyList(),
    val measures: List<Measure> = emptyList(),
    val shoppingLists: List<ShoppingList> = emptyList(),
    val scaleFactor: Double = 1.0,
    val totalCost: Double = 0.0,
    val isLoading: Boolean = true,
    val ingredientPrices: Map<String, Double> = emptyMap()
)

class RecipeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val instructionSectionRepository: InstructionSectionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientRepository: IngredientRepository,
    private val measureRepository: MeasureRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val ingredientVariantRepository: IngredientVariantRepository
) : ViewModel() {

    private val recipeIdString: String = checkNotNull(savedStateHandle["recipeId"])
    private val recipeId: UUID = UUID.fromString(recipeIdString)

    private val _scaleFactor = MutableStateFlow(1.0)

    // Helper data class to group first batch of flows
    private data class RecipeData(
        val recipe: Recipe?,
        val images: List<RecipeImage>,
        val ingredients: List<RecipeIngredient>,
        val instructions: List<Instruction>
    )

    // Helper data class to group second batch of flows
    private data class AuxData(
        val measures: List<Measure>,
        val shoppingLists: List<ShoppingList>,
        val scale: Double
    )

    // 1. Combine core recipe data
    private val recipeDataFlow = combine(
        recipeRepository.getRecipeById(recipeId),
        recipeImageRepository.getImagesForRecipe(recipeId), // Ensure this method exists or use getAll().filter...
        recipeIngredientRepository.getRecipeIngredientsForRecipe(recipeId),
        instructionRepository.getInstructionsForRecipe(recipeId)
    ) { recipe, images, ingredients, instructions ->
        RecipeData(
            recipe = recipe,
            images = images.filterNotNull(),           // Converts List<T?> to List<T>
            ingredients = ingredients.filterNotNull(), // Converts List<T?> to List<T>
            instructions = instructions.filterNotNull()
        )
    }

    // 2. Combine auxiliary data
    private val auxDataFlow = combine(
        measureRepository.getAllMeasures(),
        shoppingListRepository.getAllShoppingLists(),
        _scaleFactor
    ) { measures, shoppingLists, scale ->
        AuxData(measures.filterNotNull(), shoppingLists.filterNotNull(), scale)
    }

    // 3. Merge everything into UiState
    val uiState: StateFlow<RecipeDetailUiState> = combine(
        recipeDataFlow,
        auxDataFlow
    ) { rData, aData ->
        // Fetch remaining one-off data (Sections, Base Ingredients, Costs)
        // Note: For strict correctness these should be flows too, but fetching on data change is acceptable here
        val sections = (instructionSectionRepository.getSectionsForRecipe(recipeId).firstOrNull() ?: emptyList()).filterNotNull()

        val baseIngredients = rData.ingredients.mapNotNull { ri ->
            ingredientRepository.getIngredientById(ri.ingredientId).firstOrNull()
        }

        // Calculate Cost AND build Price Map
        val priceMap = mutableMapOf<String, Double>()
        var cost = 0.0

        rData.ingredients.forEach { ri ->
            val price = ingredientVariantRepository.getCheapestPriceForIngredient(ri.foodDbId)
                .firstOrNull()?.price ?: 0.0

            priceMap[ri.foodDbId] = price
            cost += price
        }

        RecipeDetailUiState(
            recipe = rData.recipe,
            images = rData.images,
            ingredients = rData.ingredients,
            baseIngredients = baseIngredients,
            instructions = rData.instructions,
            instructionSections = sections,
            measures = aData.measures,
            shoppingLists = aData.shoppingLists,
            scaleFactor = aData.scale,
            totalCost = cost * aData.scale,
            isLoading = false,
            ingredientPrices = priceMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailUiState(isLoading = true)
    )

    fun updateScaleFactor(newFactor: Double) {
        _scaleFactor.value = newFactor
    }

    fun incrementMakeCounter() {
        viewModelScope.launch {
            recipeRepository.incrementTimesMade(recipeId)
        }
    }

    fun deleteRecipe() {
        viewModelScope.launch {
            val recipe = uiState.value.recipe ?: return@launch
            recipeRepository.deleteRecipe(recipe)
        }
    }

    fun addAllToShoppingList(listId: Long) {
        viewModelScope.launch {
            val currentIngredients = uiState.value.ingredients
            currentIngredients.forEach { ri ->
                shoppingListRepository.insertItem(
                    ShoppingListItem(
                        ingredientId = ri.ingredientId,
                        quantity = ri.quantity * _scaleFactor.value,
                        measureId = 0,
                        shoppingListId = listId
                    )
                )
            }
        }
    }
}