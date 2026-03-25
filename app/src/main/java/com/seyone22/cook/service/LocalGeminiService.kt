package com.seyone22.cook.service

import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.seyone22.cook.data.model.RecipeIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID

// --- Data Models (Unchanged) ---
// (Paste your ParsedIngredient, toRecipeIngredient, etc. here)

// --- Service ---

class LocalGeminiService(private val apiKey: String = "") {

    private val TAG = "LocalGeminiService"

    // JSON Parser with lenient settings
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // Initialize the ML Kit GenAI Prompt client
    private val generativeModel = Generation.getClient()

    // --- State Flows for the Frontend UI ---

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgressBytes = MutableStateFlow(0L)
    val downloadProgressBytes: StateFlow<Long> = _downloadProgressBytes.asStateFlow()

    /**
     * Checks if the Gemini Nano model is available, and downloads it if it isn't.
     * Call this from your ViewModel when the app starts or before opening the scanner UI.
     * * @return true if the model is ready to use, false otherwise.
     */
    suspend fun ensureModelReady(): Boolean {
        return try {
            val status = generativeModel.checkStatus()

            when (status) {
                FeatureStatus.AVAILABLE -> {
                    Log.d(TAG, "Gemini Nano is already downloaded and ready.")
                    true
                }
                FeatureStatus.DOWNLOADING -> {
                    Log.w(TAG, "Gemini Nano is currently downloading in the background.")
                    _isDownloading.value = true
                    false // Not ready yet
                }
                FeatureStatus.DOWNLOADABLE -> {
                    Log.d(TAG, "Model is downloadable. Starting download...")
                    var downloadSuccess = false
                    _isDownloading.value = true

                    generativeModel.download().collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                Log.d(TAG, "Download started.")
                            }
                            is DownloadStatus.DownloadProgress -> {
                                _downloadProgressBytes.value = downloadStatus.totalBytesDownloaded
                                Log.d(TAG, "Downloaded: ${downloadStatus.totalBytesDownloaded} bytes")
                            }
                            is DownloadStatus.DownloadCompleted -> {
                                Log.d(TAG, "Download completed successfully.")
                                downloadSuccess = true
                            }
                            is DownloadStatus.DownloadFailed -> {
                                Log.e(TAG, "Download failed: ${downloadStatus.e.message}")
                                downloadSuccess = false
                            }
                        }
                    }
                    _isDownloading.value = false
                    downloadSuccess
                }
                FeatureStatus.UNAVAILABLE -> {
                    Log.e(TAG, "Gemini Nano is unavailable on this device (Check AICore settings).")
                    false
                }
                else -> {
                    Log.e(TAG, "Unknown FeatureStatus: $status")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check or download model: ${e.message}", e)
            _isDownloading.value = false
            false
        }
    }

    suspend fun generateStructuredRecipe(rawText: String): String? {
        val prompt = """
            ## INSTRUCTION
            You are a recipe parsing assistant. Convert the raw text below into a valid JSON-LD Recipe object (https://schema.org/Recipe).
            
            ## CONSTRAINTS
            - Use ISO 8601 durations for times (e.g., "PT30M").
            - Do not include explanations or markdown.
            - Output raw JSON only.
            
            ## RAW TEXT
            $rawText
        """.trimIndent()

        return callGeminiNano(prompt)
    }

    suspend fun parseIngredients(ingredientList: List<String>): List<ParsedIngredient> {
        if (ingredientList.isEmpty()) return emptyList()

        val prompt = """
            ## INSTRUCTION
            Extract structured data from this ingredient list. Return a JSON Array of objects.
            
            ## SCHEMA
            Fields: "ingredient" (string), "quantity" (number, 0.0 if missing), "unit" (string, null if missing), "notes" (string, null if missing).
            
            ## CONSTRAINTS
            - Output raw JSON only.
            - No markdown formatting (no ```json).
            - No conversational preamble.
            
            ## INGREDIENTS
            ${ingredientList.joinToString("\n")}
        """.trimIndent()

        val jsonString = callGeminiNano(prompt) ?: return emptyList()

        return try {
            jsonParser.decodeFromString<List<ParsedIngredient>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse final ingredient list JSON: ${e.message}")
            Log.d(TAG, "Bad JSON Content: $jsonString")
            emptyList()
        }
    }

    private suspend fun callGeminiNano(prompt: String): String? {
        // --- The Safety Net ---
        // Automatically ensure the model is ready before attempting inference.
        if (!ensureModelReady()) {
            Log.e(TAG, "Model is not ready. Aborting generation.")
            return null
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            val text = response.candidates[0].text

            if (text.isBlank()) {
                Log.e(TAG, "No output received from Gemini Nano.")
                return null
            }

            val cleanedText = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
                .extractJsonBlock()

            cleanedText

        } catch (e: Exception) {
            Log.e(TAG, "ML Kit GenAI Error: ${e.message}", e)
            null
        }
    }

    private fun String.extractJsonBlock(): String {
        val firstBrace = this.indexOf("{")
        val firstBracket = this.indexOf("[")

        return when {
            firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace) -> {
                val lastBracket = this.lastIndexOf("]")
                if (lastBracket != -1) this.substring(firstBracket, lastBracket + 1) else this
            }
            firstBrace != -1 -> {
                val lastBrace = this.lastIndexOf("}")
                if (lastBrace != -1) this.substring(firstBrace, lastBrace + 1) else this
            }
            else -> this
        }
    }
}