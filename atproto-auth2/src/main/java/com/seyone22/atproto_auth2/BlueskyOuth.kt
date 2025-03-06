package com.seyone22.atproto_auth2

import com.seyone22.atproto_auth2.utils.PkceUtils
import com.seyone22.atproto_auth2.utils.UrlUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType

class BlueskyOAuth(
    private val clientId: String,
    private val redirectUri: String,
    private val client: HttpClient // client is passed here
) {
    suspend fun requestPushedAuthorization(loginHint: String): Pair<String, String> {
        val state = PkceUtils.generateState()
        val codeVerifier = PkceUtils.generateCodeVerifier()
        val codeChallenge = PkceUtils.generateCodeChallenge(codeVerifier)

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
                append("login_hint", loginHint)
            })
        }

        if (response.status == HttpStatusCode.Created) {
            val json = response.bodyAsText()
            val requestUri = extractJsonValue(json, "request_uri")
            val nonce = response.headers["DPoP-Nonce"] ?: ""

            return requestUri to nonce
        } else {
            throw Exception("Failed to get PAR response: ${response.status}")
        }
    }

    private fun extractJsonValue(json: String, key: String): String {
        return Regex("\"$key\":\"(.*?)\"").find(json)?.groups?.get(1)?.value ?: ""
    }

    suspend fun getAuthorizationUrl(serviceEndpoint: String, requestUri: String): String {
        // Fetch OAuth server metadata from the service endpoint
        val metadata = fetchOAuthServerMetadata(serviceEndpoint, client)

        return if (metadata != null) {
            // Use the authorization_endpoint from the metadata instead of hardcoding the URL
            val authorizationUrl = metadata.authorizationEndpoint
            val encodedClientId = UrlUtils.encode(clientId)
            val encodedRequestUri = UrlUtils.encode(requestUri)

            "$authorizationUrl?client_id=$encodedClientId&request_uri=$encodedRequestUri"
        } else {
            throw Exception("Failed to fetch OAuth server metadata")
        }
    }

}
