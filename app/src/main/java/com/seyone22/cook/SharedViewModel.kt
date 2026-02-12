package com.seyone22.cook

import android.util.Log
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.provider.KtorClientProvider.client
import com.seyone22.cook.service.GeminiService
import com.seyone22.cook.service.ParsedIngredient
import com.seyone22.cook.service.resolveAndSaveIngredient
import com.seyone22.cook.service.toRecipeIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import recipeimporter.RecipeImporter.importFromUrl
import recipeimporter.model.Recipe
import java.util.UUID

class SharedViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientProductRepository: IngredientVariantRepository,
    private val geminiService: GeminiService // Injected Dependency
) : BaseViewModel() {
    private val _importedRecipe = MutableStateFlow<Recipe?>(null)
    val importedRecipe: StateFlow<Recipe?> = _importedRecipe.asStateFlow()

    private val _ingredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    val ingredients: StateFlow<List<RecipeIngredient>> = _ingredients

    private val _valueString = MutableStateFlow<String?>(null)
    val valueString: StateFlow<String?> = _valueString.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    fun clearImportedRecipe() {
        _importedRecipe.value = null
        _ingredients.value = emptyList()
    }

    suspend fun saveImportedRecipe(recipe: Recipe?) {
        recipe?.let {
            // 1. Parse ingredients (Raw Text -> Structured Object)
            val parsedIngredients: List<ParsedIngredient> =
                geminiService.parseIngredients(recipe.ingredients)

            // 2. Match OR Fallback (Structured Object -> Domain Model)
            // If matching fails (returns null), we create a fresh Ingredient from the parsed data.
            val processedIngredients = parsedIngredients.map { parsed ->
                resolveAndSaveIngredient(
                    parsed, client, ingredientRepository, ingredientProductRepository
                )
            }

            // 3. Update State ATOMICALY (or close to it) at the end
            // Updating ingredients first ensures they are ready when the UI sees the recipe
            _ingredients.value = processedIngredients
            _importedRecipe.value = it
        }
    }

    suspend fun importAndSaveRecipe(url: String): Boolean {
        _isLoading.value = true
        // Run IO operation
        val recipeJson: Recipe? = importRecipeFromUrl(url)

        return if (recipeJson != null) {
            saveImportedRecipe(recipeJson)
            _isLoading.value = false
            true
        } else {
            _isLoading.value = false
            false
        }
    }

    suspend fun saveImportedRecipeByCamera(rawText: String) {
        setLoading(true)
        // Use injected service
        val ldJson = geminiService.generateStructuredRecipe(rawText) ?: return setLoading(false)

        try {
            val recipe = recipeimporter.RecipeImporter.parseLdJson(ldJson)

            if (recipe?.ingredients == null) {
                Log.e("SharedViewModel", "Failed to parse structured recipe")
                setLoading(false)
                return  // exit early
            }

            // --- Parse ingredients using injected service ---
            val parsedIngredients: List<ParsedIngredient> =
                geminiService.parseIngredients(recipe.ingredients)

            val recipeIngredients: List<RecipeIngredient> = parsedIngredients.map { parsed ->
                parsed.toRecipeIngredient(recipeId = UUID.randomUUID())
            }

            // --- Match to canonical ingredients (Commented out in original, kept consistent) ---
            // val matchedIngredients: List<RecipeIngredient> =
            //     recipeIngredients.mapNotNull { parsed: RecipeIngredient ->
            //         matchCanonicalIngredient(parsed, client)
            //     }

            // --- Update state ---
            _ingredients.value = recipeIngredients

            Log.d("SharedViewModel", "Imported recipe: $recipe")
        } catch (e: Exception) {
            Log.e("SharedViewModel", "Failed to parse: ${e.localizedMessage}")
        }

        setLoading(false)
    }

    suspend fun importRecipeFromUrl(url: String): Recipe? {
        return try {
            importFromUrl(url)
        } catch (e: Exception) {
            Log.e("SharedViewModel", "Import failed", e)
            null
        }
    }
}