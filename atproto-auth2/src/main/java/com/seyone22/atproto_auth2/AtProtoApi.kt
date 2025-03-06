package com.seyone22.atproto_auth2

import android.util.Log
import com.seyone22.atproto_auth2.data.ClientMetadata
import com.seyone22.atproto_auth2.data.DIDDocument
import com.seyone22.atproto_auth2.data.OAuthProtectedResource
import com.seyone22.atproto_auth2.data.OAuthServerMetadata
import com.seyone22.atproto_auth2.data.ResolveHandleResponse
import com.seyone22.atproto_auth2.utils.extractJsonValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Initialize Ktor client with logging
val client = HttpClient(CIO) {  // CIO engine supports logging well
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })  // Use relaxed JSON parsing
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL  // Log everything
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
suspend fun fetchDIDFromHandle(handle: String, serviceEndpoint: String): String? {
    return try {
        val response: ResolveHandleResponse =
            client.get("${serviceEndpoint}/xrpc/com.atproto.identity.resolveHandle") {
                parameter("handle", handle)
            }.body()
        response.did
    } catch (e: Exception) {
        null // Handle network failures gracefully
    }
}

suspend fun fetchDIDDocument(did: String): DIDDocument? {
    val url = "https://plc.directory/$did"
    return try {
        val response: DIDDocument = client.get(url).body();
        response
    } catch (e: Exception) {
        println("Error fetching service endpoint: ${e.message}")
        null
    }
}

// Fetch PDS Metadata
suspend fun fetchPDSMetadata(serviceEndpoint: String): OAuthProtectedResource? {
    return try {
        val url = "$serviceEndpoint/.well-known/oauth-protected-resource"
        val response: OAuthProtectedResource = client.get(url).body()
        response // Get the first auth server, if available
    } catch (e: Exception) {
        null
    }
}

// Authorization Server Discovery
suspend fun fetchOAuthServerMetadata(serverUrl: String): OAuthServerMetadata? {
    Log.d("TAG", "fetchOAuthServerMetadata: ${serverUrl}")

    return try {
        val url = "$serverUrl/.well-known/oauth-authorization-server"
        val response: OAuthServerMetadata = client.get(url).body<OAuthServerMetadata>()
        response
    } catch (e: Exception) {
        null
    }
}

// Request authorization from Bluesky's Authorization Server (PAR)
suspend fun initiatePARRequest(
    pushedAuthorizationRequestEndpoint: String = "https://bsky.social/oauth/par",
    codeChallengeMethod: String = "S256",
    scope: String = "atproto transition:generic",
    clientId: String,
    redirectUri: String,
    loginHint: String,
    codeChallenge: String,
    state: String,
): Pair<String, String> {


    // Make PAR Request
    val response: HttpResponse = client.post(pushedAuthorizationRequestEndpoint) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(FormDataContent(Parameters.build {
            append("response_type", "code")
            append("code_challenge_method", codeChallengeMethod)
            append("scope", scope)
            append("client_id", clientId)
            append("redirect_uri", redirectUri)
            append("code_challenge", codeChallenge)
            append("state", state)
            append("login_hint", loginHint)
        }))
    }

    if (response.status == HttpStatusCode.Created) {
        val json = response.bodyAsText()
        val requestUri = extractJsonValue(json, "request_uri")
        val nonce = response.headers["DPoP-Nonce"] ?: ""

        // Return the request URI and nonce for further processing
        if (requestUri !== null) {
            // Returns the PAR response
            return requestUri to nonce
        } else {
            throw Exception("Request URI is null")
        }
    } else {
        throw Exception("Failed to initiate authorization request: ${response.status}")
    }
}

// Build Redirect URL
suspend fun buildRedirectURL(
    authorizationEndpoint: String, clientId: String, requestUri: String
): String {
    return "${authorizationEndpoint}?client_id=${clientId}&request_uri=${requestUri}"
}