package com.seyone22.atproto_auth2

import com.seyone22.atproto_auth2.utils.PkceUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType

class AtProtoAuthManager(
    private val client: HttpClient, // HttpClient to make network requests
    private val metadataUrl: String // URL of the client metadata JSON file
) {

    suspend fun startAuthFlow(): Pair<String, String> {
        // Step 1: Fetch and parse client metadata from the provided URL
        val clientMetadata = fetchClientMetadata(metadataUrl)

        // Step 2: Extract necessary fields from the metadata
        val clientId = clientMetadata.client_id
        val redirectUri = clientMetadata.redirect_uris.firstOrNull()
            ?: throw Exception("No redirect URI found in client metadata")

        // Step 3: Continue with the OAuth flow using clientId and redirectUri
        return initiateAuthorizationRequest(clientId, redirectUri)
    }


    private suspend fun initiateAuthorizationRequest(
        clientId: String, redirectUri: String
    ): Pair<String, String> {
        val state = PkceUtils.generateState() // Generate a unique state value
        val codeVerifier = PkceUtils.generateCodeVerifier() // Generate a code verifier
        val codeChallenge =
            PkceUtils.generateCodeChallenge(codeVerifier) // Generate code challenge based on code verifier

        // Step 4: Request authorization from Bluesky's Authorization Server
        val response: HttpResponse = client.post("https://bsky.social/oauth/par") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("response_type", "code")
                append("code_challenge_method", "S256")
                append("scope", "atproto transition:generic")
                append("client_id", clientId)
                append("redirect_uri", redirectUri)
                append("code_challenge", codeChallenge)
                append("state", state)
            })
        }

        if (response.status == HttpStatusCode.Created) {
            val json = response.bodyAsText()
            val requestUri = extractJsonValue(json, "request_uri")
            val nonce = response.headers["DPoP-Nonce"] ?: ""

            // Return the request URI and nonce for further processing
            if (requestUri !== null) {
                return requestUri to nonce
            } else {
                throw Exception("Request URI is null")
            }
        } else {
            throw Exception("Failed to initiate authorization request: ${response.status}")
        }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        return Regex("\"$key\":\"(.*?)\"").find(json)?.groups?.get(1)?.value
    }

    suspend fun handleAuthorizationCallback(code: String, state: String): String {
        val clientMetadata = fetchClientMetadata(metadataUrl)

        // Step 5: Handle the callback and exchange authorization code for an access token
        val response: HttpResponse = client.post("https://bsky.social/oauth/token") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("state", state)
                append("client_id", clientMetadata.client_id)
                append("redirect_uri", clientMetadata.redirect_uris.first())
            })
        }

        if (response.status == HttpStatusCode.OK) {
            val jsonResponse = response.bodyAsText()
            val accessToken = extractJsonValue(jsonResponse, "access_token")
            if (accessToken !== null) {
                return accessToken
            } else {
                throw Exception("Access token is null")
            }
        } else {
            throw Exception("Failed to exchange authorization code for access token")
        }
    }
}
