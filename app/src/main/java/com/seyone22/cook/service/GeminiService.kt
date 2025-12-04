package com.seyone22.cook.service

import android.util.Log
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.provider.KtorClientProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

// --- Data Models ---

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

@Serializable
private data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
private data class Content(
    val role: String = "user",
    val parts: List<Part>
)

@Serializable
private data class Part(
    val text: String
)

@Serializable
private data class GenerationConfig(
    val responseMimeType: String? = null
)

@Serializable
private data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null // Added Error field to catch failures
)

@Serializable
private data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
private data class Candidate(
    val content: Content?,
    val finishReason: String? = null
)

// --- Service ---

class GeminiService(private val apiKey: String) {

    private val TAG = "GeminiService"

    // JSON Parser with lenient settings
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // Endpoint: https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    suspend fun generateStructuredRecipe(rawText: String): String? {
        val prompt = """
            You are a recipe parsing assistant. Convert the raw text below into a valid JSON-LD Recipe object (https://schema.org/Recipe).
            Strict Requirements:
            - Use ISO 8601 durations for times (e.g., "PT30M").
            - Do not include explanations or markdown.
            - Output raw JSON only.
            
            Raw Text:
            $rawText
        """.trimIndent()

        return callGeminiApi(prompt, jsonMode = true)
    }

    suspend fun parseIngredients(ingredientList: List<String>): List<ParsedIngredient> {
        if (ingredientList.isEmpty()) return emptyList()

        val prompt = """
            Extract structured data from this ingredient list. Return a JSON Array of objects.
            Fields: "ingredient" (string), "quantity" (number, 0.0 if missing), "unit" (string, null if missing), "notes" (string, null if missing).
            
            Ingredients:
            ${ingredientList.joinToString("\n")}
        """.trimIndent()

        val jsonString = callGeminiApi(prompt, jsonMode = true) ?: return emptyList()

        return try {
            jsonParser.decodeFromString<List<ParsedIngredient>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse final ingredient list JSON: ${e.message}")
            Log.d(TAG, "Bad JSON Content: $jsonString")
            emptyList()
        }
    }

    private suspend fun callGeminiApi(prompt: String, jsonMode: Boolean): String? {
        return try {
            // 1. Prepare Request
            val requestBody = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = if (jsonMode) GenerationConfig(responseMimeType = "application/json") else null
            )

            // 2. Execute Request
            val httpResponse = KtorClientProvider.client.post("$baseUrl?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestBody) // Ktor will serialize this using its own plugin, or we could send string directly
            }

            // 3. Read Raw Response
            val responseStatus = httpResponse.status
            val rawResponseBody = httpResponse.bodyAsText()

            if (responseStatus.value !in 200..299) {
                Log.e(TAG, "API Error: $responseStatus - $rawResponseBody")
                return null
            }

            // 4. Parse Response
            val geminiResponse = try {
                jsonParser.decodeFromString<GeminiResponse>(rawResponseBody)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to deserialize GeminiResponse: ${e.message}")
                return null
            }

            // 5. Handle Errors returned in JSON (even with 200 OK sometimes)
            if (geminiResponse.error != null) {
                Log.e(TAG, "Gemini Logical Error: ${geminiResponse.error}")
                return null
            }

            val text = geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (text == null) {
                Log.e(TAG, "No candidates found in response. FinishReason: ${geminiResponse.candidates?.firstOrNull()?.finishReason}")
                return null
            }

            // 6. Cleanup Markdown
            val cleanedText = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            return cleanedText

        } catch (e: Exception) {
            Log.e(TAG, "Network or System Error: ${e.message}", e)
            return null
        }
    }
}