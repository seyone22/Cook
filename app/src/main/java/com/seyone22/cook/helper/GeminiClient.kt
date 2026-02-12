package com.seyone22.cook.helper

import com.google.ai.client.generativeai.GenerativeModel
import com.seyone22.cook.data.model.GenerativeAiResult

class GeminiClient(apiKey: String, modelName: String) : GenerativeAiClient {
    private val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey,
    )

    override suspend fun generateContent(prompt: String): GenerativeAiResult<String> {
        return try {
            val response = generativeModel.generateContent(prompt)
            GenerativeAiResult.Success(response.text ?: "")
        } catch (e: Exception) {
            GenerativeAiResult.Error(e.localizedMessage ?: "An error occurred with Gemini")
        }
    }
}