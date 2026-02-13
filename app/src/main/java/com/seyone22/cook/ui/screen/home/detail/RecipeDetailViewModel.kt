package com.seyone22.cook.ui.screen.home.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.*
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.instructionsection.InstructionSectionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import kotlinx.coroutines.flow.*
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
    val scaleFactor: Double = 1.0, // The multiplier (e.g., 0.5, 1.0, 2.0)
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

    // This represents the "People Count" the user wants.
    // Initialized to null so we can fall back to the recipe's default on load.
    private val _targetServings = MutableStateFlow<Double?>(null)

    private val recipeFlow = recipeRepository.getRecipeById(recipeId).filterNotNull()
    private val recipeIngredientsFlow = recipeIngredientRepository.getRecipeIngredientsForRecipe(recipeId).map { it.filterNotNull() }

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        recipeFlow,
        recipeIngredientsFlow,
        recipeImageRepository.getImagesForRecipe(recipeId).map { it.filterNotNull() },
        instructionRepository.getInstructionsForRecipe(recipeId).map { it.filterNotNull() },
        instructionSectionRepository.getSectionsForRecipe(recipeId).map { it.filterNotNull() },
        measureRepository.getAllMeasures().map { it.filterNotNull() },
        shoppingListRepository.getAllShoppingLists().map { it.filterNotNull() },
        _targetServings
    ) { params ->
        val recipe = params[0] as Recipe
        val ingredients = params[1] as List<RecipeIngredient>
        val images = params[2] as List<RecipeImage>
        val instructions = params[3] as List<Instruction>
        val sections = params[4] as List<InstructionSection>
        val measures = params[5] as List<Measure>
        val shoppingLists = params[6] as List<ShoppingList>
        val userTarget = params[7] as? Double

        // 1. Determine "Effective Servings" (Target)
        // If user hasn't touched the dial, use recipe default (e.g., 8)
        val baseServings = recipe.servingSize.toDouble()
        val effectiveServings = userTarget ?: baseServings

        // 2. Calculate the Ratio (Multiplier) for math
        // Example: Target 16 / Base 8 = 2.0x
        val multiplier = if (baseServings > 0) effectiveServings / baseServings else 1.0

        // 3. Fetch Base Ingredient details
        val baseIngredients = ingredients.mapNotNull { ri ->
            ingredientRepository.getIngredientById(ri.ingredientId).firstOrNull()
        }

        // 4. Calculate Costs using the Multiplier
        val priceMap = mutableMapOf<String, Double>()
        var baseTotalCost = 0.0
        ingredients.forEach { ri ->
            if (!ri.foodDbId.isNullOrBlank()) {
                val price = ingredientVariantRepository.getCheapestPriceForIngredient(ri.foodDbId)
                    .firstOrNull()?.price ?: 0.0
                priceMap[ri.foodDbId!!] = price
                baseTotalCost += price
            }
        }

        RecipeDetailUiState(
            recipe = recipe,
            images = images,
            ingredients = ingredients,
            baseIngredients = baseIngredients,
            instructions = instructions,
            instructionSections = sections,
            measures = measures,
            shoppingLists = shoppingLists,
            // 'scaleFactor' in state is now explicitly the PEOPLE COUNT (e.g., 16.0)
            scaleFactor = effectiveServings,
            totalCost = baseTotalCost * multiplier,
            isLoading = false,
            ingredientPrices = priceMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailUiState(isLoading = true)
    )

    /**
     * Updates the target serving count (People Count).
     */
    fun updateScaleFactor(newPeopleCount: Double) {
        _targetServings.value = newPeopleCount
    }

    fun addAllToShoppingList(listId: Long) {
        viewModelScope.launch {
            val currentState = uiState.value
            val recipe = currentState.recipe ?: return@launch

            // Calculate multiplier: Current People / Base People
            val multiplier = currentState.scaleFactor / recipe.servingSize.toDouble()

            currentState.ingredients.forEach { ri ->
                shoppingListRepository.insertItem(
                    ShoppingListItem(
                        ingredientId = ri.ingredientId,
                        quantity = ri.quantity * multiplier,
                        measureName =  ri.unit,
                        shoppingListId = listId
                    )
                )
            }
        }
    }

    // Other standard functions...
    fun incrementMakeCounter() { viewModelScope.launch { recipeRepository.incrementTimesMade(recipeId) } }
    fun deleteRecipe() { viewModelScope.launch { uiState.value.recipe?.let { recipeRepository.deleteRecipe(it) } } }
}