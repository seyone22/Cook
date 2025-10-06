package com.seyone22.cook

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import recipeimporter.RecipeImporter.importFromUrl
import recipeimporter.model.Recipe

class SharedViewModel : BaseViewModel() {

    private val _importedRecipe = MutableStateFlow<Recipe?>(null)
    val importedRecipe: StateFlow<Recipe?> = _importedRecipe.asStateFlow()

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
    fun saveImportedRecipe(recipe: Recipe?) {
        recipe?.let {
            _importedRecipe.value = it
            Log.d("SharedViewModel", "Imported recipe: ${_importedRecipe.value}")
        }
    }

    /**
     * Helper function to import a recipe from a URL and immediately save it
     * Returns true if successful, false otherwise
     */
    suspend fun importAndSaveRecipe(url: String): Boolean {
        val recipeJson: Recipe? = importRecipeFromUrl(url)
        return if (recipeJson != null) {
            saveImportedRecipe(recipeJson)
            true
        } else {
            false
        }
    }

    fun saveImportedRecipeByCamera(string: String) {
        _valueString.value = string
    }

    suspend fun importRecipeFromUrl(url: String): Recipe? {
        return importFromUrl(url)
    }
}
