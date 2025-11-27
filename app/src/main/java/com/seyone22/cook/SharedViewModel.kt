package com.seyone22.cook

import android.util.Log
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.provider.KtorClientProvider.client
import com.seyone22.cook.service.GeminiService
import com.seyone22.cook.service.ParsedIngredient
import com.seyone22.cook.service.matchCanonicalIngredient
import com.seyone22.cook.service.toRecipeIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import recipeimporter.RecipeImporter.importFromUrl
import recipeimporter.model.Recipe
import java.util.UUID

class SharedViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientProductRepository: IngredientVariantRepository
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

    /** Clear imported recipe data (optional, if user cancels import) */
    fun clearImportedRecipe() {
        _importedRecipe.value = null
    }

    /** Save imported recipe (stub, integrate with your database logic) */
    suspend fun saveImportedRecipe(recipe: Recipe?) {
        recipe?.let {
            _importedRecipe.value = it
            Log.d("SharedViewModel", "Imported recipe: ${_importedRecipe.value}")

            // --- Parse ingredients separately ---
            val parsedIngredients: List<ParsedIngredient> =
                GeminiService.parseIngredients(recipe.ingredients)

            Log.d("TAG", "saveImportedRecipe: $parsedIngredients")

            // --- Match to canonical ingredients ---
            val matchedIngredients = mutableListOf<RecipeIngredient>()
            for (parsed in parsedIngredients) {
                matchCanonicalIngredient(
                    parsed, client, ingredientRepository, ingredientProductRepository
                )?.let { matchedIngredients.add(it) }
            }

            Log.d("TAG", "saveImportedRecipe: $matchedIngredients")

            // --- Update state ---
            _ingredients.value = matchedIngredients
        }
    }

    /**
     * Helper function to import a recipe from a URL and immediately save it
     * Returns true if successful, false otherwise
     */
    suspend fun importAndSaveRecipe(url: String): Boolean {
        _isLoading.value = true
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
        val ldJson = GeminiService.generateStructuredRecipe(rawText) ?: return setLoading(false)

        try {
            val recipe = recipeimporter.RecipeImporter.parseLdJson(ldJson)

            if (recipe?.ingredients == null) {
                Log.e("SharedViewModel", "Failed to parse structured recipe")
                setLoading(false)
                return  // exit early
            }

            // --- Parse ingredients separately ---
            val parsedIngredients: List<ParsedIngredient> =
                GeminiService.parseIngredients(recipe.ingredients)

            val recipeIngredients: List<RecipeIngredient> = parsedIngredients.map { parsed ->
                parsed.toRecipeIngredient(recipeId = UUID.randomUUID())
            }


            // --- Match to canonical ingredients ---
//            val matchedIngredients: List<RecipeIngredient> =
//                recipeIngredients.mapNotNull { parsed: RecipeIngredient ->
//                    matchCanonicalIngredient(parsed, client)
//                }

            // --- Update state ---
            _ingredients.value = recipeIngredients

            Log.d("SharedViewModel", "Imported recipe: $recipe")
        } catch (e: Exception) {
            Log.e("SharedViewModel", "Failed to parse: ${e.localizedMessage}")
        }

        setLoading(false)
    }

    suspend fun importRecipeFromUrl(url: String): Recipe? {
        return importFromUrl(url)
    }


}
