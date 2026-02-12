package com.seyone22.cook.helper

import com.seyone22.cook.data.model.GenerativeAiResult

interface GenerativeAiClient {
    suspend fun generateContent(prompt: String): GenerativeAiResult<String>
}