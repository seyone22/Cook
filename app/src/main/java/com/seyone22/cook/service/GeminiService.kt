package com.seyone22.cook.service

import android.util.Log
import com.seyone22.cook.data.model.GenerativeAiResult
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.helper.GeminiClient
import com.seyone22.cook.helper.GenerativeAiClient
import com.seyone22.cook.helper.OpenAiGeminiClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class ParsedIngredient(
    val ingredient: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val notes: String? = null
)


fun ParsedIngredient.toRecipeIngredient(
    recipeId: UUID = UUID.randomUUID(),
    foodDbId: String = "",
    name: String = this.ingredient
): RecipeIngredient {
    return RecipeIngredient(
        recipeId = recipeId,
        ingredientId = UUID.randomUUID(),
        foodDbId = foodDbId,
        name = name,
        quantity = this.quantity ?: 0.0,
        unit = this.unit ?: "pcs",
        notes = this.notes
    )
}

object GeminiService {

    // --- Configuration ---
    private const val TAG = "GeminiService"
    private const val API_KEY = "AIzaSyBgMUdyO2Wcpy8qgmRpCzXE4zF0uXykbA8"
    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val PROVIDER = "OpenAI-Gemini"

    // --- Client Initialization ---
    private val client: GenerativeAiClient? by lazy {
        when (PROVIDER) {
            "Gemini" -> GeminiClient(API_KEY, MODEL_NAME)
            "OpenAI-Gemini" -> OpenAiGeminiClient(API_KEY, MODEL_NAME)
            else -> null
        }
    }

    // --- Utilities ---
    private fun cleanAiResponse(raw: String): String =
        raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

    private inline fun <reified T> parseJsonSafe(json: String): T? = try {
        Json { ignoreUnknownKeys = true }.decodeFromString(json)
    } catch (e: Exception) {
        Log.e(TAG, "JSON parse error: ${e.message}")
        null
    }

    // --- 1️⃣ Structured Recipe Generation ---
    suspend fun generateStructuredRecipe(rawText: String): String? {
        val prompt = """
            Convert the following raw recipe text into a valid JSON-LD Recipe object.
            Follow the schema at https://schema.org/Recipe strictly.

            Requirements:
            - Output must be a single valid JSON object.
            - Do not include explanations, commentary, markdown, or code fences.
            - Do NOT reword, correct, or alter any ingredient quantities or instructions.
            - Include at least: "@context", "@type", "name", "description", "recipeIngredient", "recipeInstructions".
            - Include "prepTime", "cookTime", "totalTime", and "recipeYield" if available or inferable.
            - Use ISO 8601 durations (e.g., "PT30M" for 30 minutes).

            Raw recipe text:
            $rawText
        """.trimIndent()

        val response = client?.generateContent(prompt)
        return when (response) {
            is GenerativeAiResult.Success -> cleanAiResponse(response.data)
            is GenerativeAiResult.Error -> {
                Log.e(TAG, "Structured recipe generation failed: ${response.message}")
                null
            }

            else -> null
        }
    }

    // --- 2️⃣ Ingredient Parsing ---
    suspend fun parseIngredients(ingredientList: List<String>): List<ParsedIngredient> {
        if (ingredientList.isEmpty()) return emptyList()

        val prompt = """
            Parse the following list of recipe ingredients into structured JSON.
            
            For each ingredient, extract:
            - "ingredient": canonical name (no quantity or unit)
            - "quantity": number (may be fractional or range; pick the lower value)
            - "unit": normalized to the singular, short form (see below)
            - "notes": preparation, size, or other descriptors (optional)
            
            Use these standard short units:
            - teaspoon / teaspoons / tsp / tsps → tsp
            - tablespoon / tablespoons / tbsp / tbsps / tbl / tbls → tbsp
            - cup / cups → cup
            - gram / grams / g → g
            - kilogram / kilograms / kg / kgs → kg
            - milliliter / milliliters / ml / mls → ml
            - liter / liters / l / ls → l
            - ounce / ounces / oz / ozs → oz
            - pound / pounds / lb / lbs → lb
            - pinch / pinches → pinch
            - clove / cloves → clove
            - slice / slices → slice
            - piece / pieces → piece
            
            If no unit is present, leave it as null.
            
            Output **only** a valid JSON array of objects with these fields:
            `ingredient`, `quantity`, `unit`, `notes`.
            
            Ingredients:
            ${ingredientList.joinToString("\n")}
            """.trimIndent()


        val response = client?.generateContent(prompt)
        return when (response) {
            is GenerativeAiResult.Success -> {
                val cleaned = cleanAiResponse(response.data)
                Log.d(TAG, "parseIngredients: $cleaned")
                parseJsonSafe<List<ParsedIngredient>>(cleaned) ?: emptyList()
            }

            is GenerativeAiResult.Error -> {
                Log.e(TAG, "Ingredient parsing failed: ${response.message}")
                emptyList()
            }

            else -> emptyList()
        }
    }
}
