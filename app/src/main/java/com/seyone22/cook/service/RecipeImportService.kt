package com.seyone22.cook.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import recipeimporter.RecipeImporter.importFromUrl
import recipeimporter.model.Recipe

class RecipeImportService {

    // Use Android engine instead of CIO
    suspend fun getRecipeObject(url: String): Recipe? = withContext(Dispatchers.IO) {
        try {
            importFromUrl(url)
        } catch (e: Exception) {
            Log.e("RecipeImportService", "Failed to import recipe from URL", e)
            null
        }
    }

    fun close() {

    }
}
