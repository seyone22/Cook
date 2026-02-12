package com.seyone22.cook.ui.screen.cooking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.service.CookingSessionService
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class CookingViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val measureRepository: MeasureRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    // --- UI STATE ---
    private val _cookingViewState = MutableStateFlow(ViewState())
    val cookingViewState: StateFlow<ViewState> = _cookingViewState.asStateFlow()

    private val _activeInstructions = MutableStateFlow<List<String>>(emptyList())
    val activeInstructions: StateFlow<List<String>> = _activeInstructions.asStateFlow()

    // Phase 2: Ingredient Checklist State
    private val _checkedIngredients = MutableStateFlow<Set<Long>>(emptySet())
    val checkedIngredients: StateFlow<Set<Long>> = _checkedIngredients.asStateFlow()

    // Phase 2: Unit Preferences (Metric = true, Imperial = false)
    private val _isMetric = MutableStateFlow(true)
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

    private val _scaleFactor = MutableStateFlow(1.0)
    val scaleFactor: StateFlow<Double> = _scaleFactor.asStateFlow()

    fun setScaleFactor(factor: Double) {
        _scaleFactor.value = factor
    }

    /**
     * Toggles the 'Checked' status of an ingredient in the dashboard list.
     */
    fun toggleIngredient(ingredientId: Long) {
        val currentSet = _checkedIngredients.value
        _checkedIngredients.value = if (currentSet.contains(ingredientId)) {
            currentSet - ingredientId
        } else {
            currentSet + ingredientId
        }
    }

    /**
     * Toggles between Metric and Imperial measurement systems.
     */
    fun toggleUnitSystem(useMetric: Boolean) {
        _isMetric.value = useMetric
    }

    /**
     * Initial data fetch. Loads all relevant recipe data into the view state.
     */
    fun fetchData() {
        viewModelScope.launch {
            // Fetching data using .first() to get the current snapshot from Room
            val recipes = recipeRepository.getAllRecipes().first()
            val images = recipeImageRepository.getAllRecipeImages().first()
            val instructions = instructionRepository.getAllInstructions().first()
            val recipeIngredients = recipeIngredientRepository.getAllRecipeIngredients().first()
            val measures = measureRepository.getAllMeasures().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            val variants = ingredientVariantRepository.getAllIngredientVariants().first()

            _cookingViewState.value = ViewState(
                recipes = recipes,
                images = images,
                instructions = instructions,
                recipeIngredients = recipeIngredients,
                measures = measures,
                ingredients = ingredients,
                variants = variants,
            )
        }
    }

    /**
     * Filters and sorts instructions for a specific recipe to populate the pager.
     */
    fun loadRecipeDetails(recipeId: UUID) {
        viewModelScope.launch {
            // We pull the instructions directly from the repo to ensure we have the latest
            val instructions = instructionRepository.getAllInstructions().first()
                .filterNotNull()
                .filter { it.recipeId == recipeId }
                .sortedBy { it.stepNumber }
                .map { it.description }

            _activeInstructions.value = instructions
        }
    }

    /**
     * Starts the Cooking Mode Foreground Service (Status Bar Chip / Live Update).
     */
    fun startCookingSession(context: Context, recipeId: UUID) {
        val currentState = _cookingViewState.value

        val currentRecipe = currentState.recipes.filterNotNull().find { it.id == recipeId } ?: return

        val currentInstructions = currentState.instructions
            .filterNotNull()
            .filter { it.recipeId == recipeId }
            .sortedBy { it.stepNumber }

        val stepList = currentInstructions.map { it.description }

        if (stepList.isNotEmpty()) {
            CookingSessionService.start(
                context = context,
                recipeName = currentRecipe.name,
                instructions = stepList
            )
        }
    }

    fun convertText(text: String, toMetric: Boolean, scale: Double = 1.0): String {
        var processedText = text

        // 1. ROBUST TEMPERATURE CONVERSION
        // Pattern: Number + (optional space) + (degree symbol/degrees/deg/degs) + (F/C)
        // Matches: 350F, 350 F, 350 degrees F, 350 deg. C, 350°C, 350 degs F
        val tempRegex = Regex(
            """(\d+(?:\.\d+)?)\s*(?:°|degrees?|degs?\.?\s*)?([FC])\b""",
            RegexOption.IGNORE_CASE
        )

        processedText = tempRegex.replace(processedText) { match ->
            val value = match.groupValues[1].toDouble()
            val unit = match.groupValues[2].uppercase()

            if (toMetric && unit == "F") {
                val celsius = (value - 32) * 5 / 9
                "${celsius.toInt()}°C"
            } else if (!toMetric && unit == "C") {
                val fahrenheit = (value * 9 / 5) + 32
                "${fahrenheit.toInt()}°F"
            } else {
                match.value
            }
        }

        // 2. ROBUST VOLUME & WEIGHT CONVERSION
        // Pattern: Number + unit name/shorthand
        // Matches: 2 cups, 1.5 tsp, 500ml, 4 oz, 1 tablespoon, 2.5 lbs
        val unitRegex = Regex(
            """(\d+(?:\.\d+)?)\s*(cup|cups|oz|ounce|ounces|g|gram|grams|ml|milliliter|milliliters|tsp|teaspoon|teaspoons|tbsp|tablespoon|tablespoons|lb|lbs|pound|pounds)""",
            RegexOption.IGNORE_CASE
        )

        processedText = unitRegex.replace(processedText) { match ->
            // Multiply the captured value by our scale factor
            val originalValue = match.groupValues[1].toDouble()
            val value = originalValue * scale
            val unit = match.groupValues[2].lowercase()

            when {
                // --- To Metric ---
                toMetric && unit.contains("cup") -> "${(value * 240).toInt()}ml"
                toMetric && (unit == "oz" || unit.contains("ounce")) -> "${(value * 28.35).toInt()}g"
                toMetric && (unit == "tsp" || unit.contains("teaspoon")) -> "${(value * 5).toInt()}ml"
                toMetric && (unit == "tbsp" || unit.contains("tablespoon")) -> "${(value * 15).toInt()}ml"
                toMetric && (unit == "lb" || unit.contains("pound")) -> "${(value * 453.59).toInt()}g"

                // --- To Imperial ---
                !toMetric && unit == "ml" && value >= 240 -> "${(value / 240).toInt()} cups"
                !toMetric && unit == "ml" && value < 15 -> "${(value / 5).toInt()} tsp"
                !toMetric && unit == "ml" && value < 240 -> "${(value / 15).toInt()} tbsp"
                !toMetric && (unit == "g" || unit.contains("gram")) && value >= 453 -> "${(value / 453.59).toInt()} lbs"
                !toMetric && (unit == "g" || unit.contains("gram")) -> "${(value / 28.35).toInt()} oz"

                else -> match.value
            }
        }

        return processedText
    }

    fun formatIngredient(ingredient: RecipeIngredient, toMetric: Boolean, scale: Double): String {
        // Combine quantity and unit into a string, applying the scale to the quantity first
        val scaledQuantity = ingredient.quantity * scale
        val rawText = "${if (scaledQuantity % 1.0 == 0.0) scaledQuantity.toInt() else scaledQuantity} ${ingredient.unit} ${ingredient.name}"

        // Then run the metric conversion on that scaled text
        return convertText(rawText, toMetric)
    }
}