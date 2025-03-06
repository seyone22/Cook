package com.seyone22.atproto_auth2

import com.seyone22.atproto_auth2.data.ClientMetadata
import com.seyone22.atproto_auth2.data.DIDDocument
import com.seyone22.atproto_auth2.data.OAuthProtectedResource
import com.seyone22.atproto_auth2.data.OAuthServerMetadata
import com.seyone22.atproto_auth2.data.ResolveHandleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Initialize Ktor client
val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun fetchClientMetadata(metadataUrl: String): ClientMetadata {
    try {
        val response: HttpResponse = client.get(metadataUrl)
        if (response.status == HttpStatusCode.OK) {
            val jsonResponse = response.bodyAsText()
            return Json.decodeFromString(jsonResponse)
        } else {
            throw Exception("Failed to fetch client metadata: HTTP ${response.status}")
        }
    } catch (e: Exception) {
        throw Exception("Error fetching client metadata from $metadataUrl: ${e.message}")
    }
}

// Fetch DID from handle
suspend fun resolveHandle(handle: String): String? {
    return try {
        val response: HttpResponse =
            client.get("https://bsky.social/xrpc/com.atproto.identity.resolveHandle") {
                parameter("handle", handle)
            }

        if (response.status == HttpStatusCode.OK) {
            val jsonResponse = Json.decodeFromString<ResolveHandleResponse>(response.bodyAsText())
            jsonResponse.did
        } else {
            null // Return null for non-200 responses
        }
    } catch (e: Exception) {
        null // Handle network failures gracefully
    }
}

suspend fun fetchServiceEndpoint(did: String, client: HttpClient = HttpClient()): String? {
    val url = "https://plc.directory/$did"
    return try {
        val response: DIDDocument = client.get(url).body()
        response.service.firstOrNull { it.type == "AtprotoPersonalDataServer" }?.endpoint
    } catch (e: Exception) {
        println("Error fetching service endpoint: ${e.message}")
        null
    }
}

suspend fun fetchAuthorizationServer(serviceEndpoint: String, client: HttpClient): String? {
    return try {
        val url = "$serviceEndpoint/.well-known/oauth-protected-resource"
        val response: OAuthProtectedResource = client.get(url).body()
        response.authorizationServers.firstOrNull() // Get the first auth server, if available
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchOAuthServerMetadata(serverUrl: String, client: HttpClient): OAuthServerMetadata? {
    return try {
        val url = "$serverUrl/.well-known/oauth-authorization-server"
        client.get(url).body<OAuthServerMetadata>()
    } catch (e: Exception) {
        null
    }
}