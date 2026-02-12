package com.seyone22.cook.provider

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Singleton HttpClient
object KtorClientProvider {
    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // allows extra fields in responses
                    isLenient = true
                })
            }
        }
    }
}
