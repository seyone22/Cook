package com.seyone22.cook.service

import android.util.Log
import com.seyone22.atproto_auth2.network.KtorClient.client
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
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
        client.close()
    }
}
