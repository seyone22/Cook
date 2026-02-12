package com.seyone22.cook.helper

import androidx.compose.runtime.collectAsState
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.seyone22.cook.data.model.GenerativeAiResult
import kotlin.time.Duration.Companion.seconds

class OpenAiGeminiClient(
    apiKey: String,
    private val modelName: String = "gemini-2.5-flash-lite"
) : GenerativeAiClient {

    private val client: OpenAI = OpenAI(
        OpenAIConfig(
            token = apiKey,
            host = OpenAIHost("https://generativelanguage.googleapis.com/v1beta/openai/")
        )
    )

    override suspend fun generateContent(prompt: String): GenerativeAiResult<String> {
        return try {
            val response = client.chatCompletion(
                requestOptions = RequestOptions(timeout = Timeout(socket = 60.seconds)),
                request = ChatCompletionRequest(
                    model = ModelId("gemini-2.5-flash"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = prompt
                        )
                    )
                )
            )
            val text = response.choices.firstOrNull()?.message?.content ?: ""
            GenerativeAiResult.Success(text)
        } catch (e: Exception) {
            GenerativeAiResult.Error(e.localizedMessage ?: "OpenAI/Gemini error")
        }
    }
}
