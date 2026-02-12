package com.seyone22.cook.helper

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

object NetworkClient {
    private val client = OkHttpClient.Builder().build()

    // Create a custom Json instance for the JSON converter
    private val json = Json {
        ignoreUnknownKeys = true  // Optionally, ignore unknown keys when parsing the response
    }

    // Retrofit instance using the Json converter for serialization
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://bsky.social/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // Initialize your API service
    val api: BlueskyAuthApi = retrofit.create(BlueskyAuthApi::class.java)
}
