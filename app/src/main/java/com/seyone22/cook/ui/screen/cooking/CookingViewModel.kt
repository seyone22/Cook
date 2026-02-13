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
import kotlinx.coroutines.flow.firstOrNull
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

    private val _cookingViewState = MutableStateFlow(ViewState())
    val cookingViewState: StateFlow<ViewState> = _cookingViewState.asStateFlow()

    private val _activeInstructions = MutableStateFlow<List<String>>(emptyList())
    val activeInstructions: StateFlow<List<String>> = _activeInstructions.asStateFlow()

    private val _checkedIngredients = MutableStateFlow<Set<Long>>(emptySet())
    val checkedIngredients: StateFlow<Set<Long>> = _checkedIngredients.asStateFlow()



    private val _isMetric = MutableStateFlow(true)
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

    // This scaleFactor should be passed from the RecipeDetailScreen
    // to represent the multiplier (e.g., 2.0 if doubling)
    private val _scaleFactor = MutableStateFlow(1.0)
    val scaleFactor: StateFlow<Double> = _scaleFactor.asStateFlow()

    /**
     * Call this when the screen opens to set the scale chosen in the Detail view.
     */
    fun setScaleFactor(factor: Double) {
        _scaleFactor.value = factor
    }

    fun toggleIngredient(ingredientId: Long) {
        val currentSet = _checkedIngredients.value
        _checkedIngredients.value = if (currentSet.contains(ingredientId)) {
            currentSet - ingredientId
        } else {
            currentSet + ingredientId
        }
    }

    fun toggleUnitSystem(useMetric: Boolean) {
        _isMetric.value = useMetric
    }

    fun fetchData() {
        viewModelScope.launch {
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
     * Loads instructions and applies scaling/metric conversion to the descriptions.
     */
    fun loadRecipeDetails(recipeId: UUID) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById(recipeId).first()


            val instructions = instructionRepository.getAllInstructions().first()
                .filterNotNull()
                .filter { it.recipeId == recipeId }
                .sortedBy { it.stepNumber }
                .map {
                    // Apply scale and metric conversion to instruction text
                    convertText(it.description, _isMetric.value, _scaleFactor.value.div(recipe?.servingSize?.toDouble() ?: 1.0))
                }

            _activeInstructions.value = instructions
        }
    }

    fun startCookingSession(context: Context, recipeId: UUID) {
        val currentState = _cookingViewState.value
        val currentRecipe = currentState.recipes.filterNotNull().find { it.id == recipeId } ?: return

        // Use the instructions that have already been converted/scaled
        val stepList = _activeInstructions.value

        if (stepList.isNotEmpty()) {
            CookingSessionService.start(
                context = context,
                recipeName = currentRecipe.name,
                instructions = stepList
            )
        }
    }

    /**
     * Robust conversion and scaling for text blocks.
     */
    fun convertText(text: String, toMetric: Boolean, scale: Double = 1.0): String {
        var processedText = text

        // 1. TEMPERATURE CONVERSION
        val tempRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:°|degrees?|degs?\.?\s*)?([FC])\b""", RegexOption.IGNORE_CASE)
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

        // 2. VOLUME & WEIGHT CONVERSION + SCALING
        val unitRegex = Regex("""(\d+(?:\.\d+)?)\s*(cup|cups|oz|ounce|ounces|g|gram|grams|ml|milliliter|milliliters|tsp|teaspoon|teaspoons|tbsp|tablespoon|tablespoons|lb|lbs|pound|pounds)""", RegexOption.IGNORE_CASE)

        processedText = unitRegex.replace(processedText) { match ->
            val originalValue = match.groupValues[1].toDouble()
            val scaledValue = originalValue * scale
            val unit = match.groupValues[2].lowercase()

            when {
                toMetric && unit.contains("cup") -> "${(scaledValue * 240).toInt()}ml"
                toMetric && (unit == "oz" || unit.contains("ounce")) -> "${(scaledValue * 28.35).toInt()}g"
                toMetric && (unit == "tsp" || unit.contains("teaspoon")) -> "${(scaledValue * 5).toInt()}ml"
                toMetric && (unit == "tbsp" || unit.contains("tablespoon")) -> "${(scaledValue * 15).toInt()}ml"
                toMetric && (unit == "lb" || unit.contains("pound")) -> "${(scaledValue * 453.59).toInt()}g"

                !toMetric && unit == "ml" && scaledValue >= 240 -> "${(scaledValue / 240).toInt()} cups"
                !toMetric && unit == "ml" && scaledValue < 15 -> "${(scaledValue / 5).toInt()} tsp"
                !toMetric && unit == "ml" && scaledValue < 240 -> "${(scaledValue / 15).toInt()} tbsp"
                !toMetric && (unit == "g" || unit.contains("gram")) && scaledValue >= 453 -> "${(scaledValue / 453.59).toInt()} lbs"
                !toMetric && (unit == "g" || unit.contains("gram")) -> "${(scaledValue / 28.35).toInt()} oz"

                else -> {
                    // If no metric conversion applies, still return the scaled number
                    val formatted = if (scaledValue % 1.0 == 0.0) scaledValue.toInt().toString() else "%.2f".format(scaledValue)
                    "$formatted $unit"
                }
            }
        }

        return processedText
    }

    /**
     * Formats an ingredient by fetching the recipe context on-demand.
     * This must be a suspend function because it performs a database lookup.
     */
    suspend fun formatIngredient(
        ingredient: RecipeIngredient,
        toMetric: Boolean,
        userScale: Double
    ): String {
        // 1. Fetch the recipe snapshot from the repository
        // We use .firstOrNull() to get the current value and move on
        val recipe = recipeRepository.getRecipeById(ingredient.recipeId).firstOrNull()

        // 2. Calculate the multiplier
        // (User's target servings / Recipe's base servings)
        val baseServings = recipe?.servingSize?.toDouble() ?: 1.0
        val multiplier = userScale / baseServings

        // 3. Apply the multiplier to the quantity
        val scaledQuantity = ingredient.quantity * multiplier
        val unit = ingredient.unit ?: ""
        val name = ingredient.name ?: ""

        // 4. Build the raw text string
        val qtyString = if (scaledQuantity % 1.0 == 0.0) {
            scaledQuantity.toInt().toString()
        } else {
            "%.2f".format(scaledQuantity)
        }

        val rawText = "$qtyString $unit $name"

        // 5. Run the final text through your metric converter
        // Pass 1.0 because we already applied the multiplier in step 3
        return convertText(rawText, toMetric, 1.0)
    }
}