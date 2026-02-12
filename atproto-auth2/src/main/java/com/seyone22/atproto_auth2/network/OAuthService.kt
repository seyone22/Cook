package com.seyone22.atproto_auth2.network

import com.seyone22.atproto_auth2.utils.extractJsonValue
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType

/**
 * Object responsible for handling OAuth 2.0 authorization requests in the AT Protocol.
 *
 * This service interacts with Bluesky's authorization server to initiate authorization
 * via Pushed Authorization Requests (PAR) and construct the redirect URL for user login.
 */
object OAuthService {

    /**
     * Initiates a Pushed Authorization Request (PAR) with Bluesky's OAuth 2.0 authorization server.
     *
     * A PAR request allows the client to pre-register an authorization request with the server,
     * reducing the risk of URL tampering and improving security. Upon success, the server returns
     * a `request_uri`, which can be used to redirect the user for authentication.
     *
     * @param pushedAuthorizationRequestEndpoint The endpoint for submitting a PAR request.
     * @param codeChallengeMethod The method used for PKCE (default: "S256").
     * @param scope The scope of the requested authorization (default: "atproto transition:generic").
     * @param clientId The registered client ID of the application.
     * @param redirectUri The URI to which the user will be redirected after authentication.
     * @param loginHint An optional hint for login (e.g., username or DID).
     * @param codeChallenge A PKCE code challenge to enhance security.
     * @param state A unique state parameter for CSRF protection.
     *
     * @return A [Pair] containing the `request_uri` (needed for redirection) and the `DPoP-Nonce`.
     * @throws Exception if the request fails or the response does not contain a valid request URI.
     */
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

        // Send a POST request to the PAR endpoint with the required OAuth parameters
        val response: HttpResponse = KtorClient.client.post(pushedAuthorizationRequestEndpoint) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build {
                append("response_type", "code") // Standard OAuth 2.0 response type
                append("code_challenge_method", codeChallengeMethod) // PKCE method (S256)
                append("scope", scope) // Requested OAuth scope
                append("client_id", clientId) // Application's client ID
                append("redirect_uri", redirectUri) // Redirection after authentication
                append("code_challenge", codeChallenge) // PKCE security measure
                append("state", state) // CSRF protection parameter
                append("login_hint", loginHint) // Optional login hint (e.g., username)
            }))
        }

        // If the request is successful, extract and return the request URI and nonce
        if (response.status == HttpStatusCode.Created) {
            val json = response.bodyAsText()
            val requestUri =
                extractJsonValue(json, "request_uri") // Extract request URI from JSON response
            val nonce = response.headers["DPoP-Nonce"] ?: "" // Extract DPoP nonce from headers

            if (requestUri != null) {
                return requestUri to nonce
            } else {
                throw Exception("Request URI is null") // Ensure request URI is present
            }
        } else {
            throw Exception("Failed to initiate authorization request: ${response.status}")
        }
    }

    /**
     * Constructs the redirect URL for user authentication.
     *
     * After obtaining the `request_uri` from the PAR request, this function generates the final
     * URL that the user must visit to complete the OAuth 2.0 authorization process.
     *
     * @param authorizationEndpoint The base authorization endpoint provided by the OAuth server.
     * @param clientId The registered client ID of the application.
     * @param requestUri The request URI obtained from the PAR response.
     *
     * @return A complete URL that redirects the user to the authorization server for login.
     */
    suspend fun buildRedirectURL(
        authorizationEndpoint: String, clientId: String, requestUri: String
    ): String {
        return "$authorizationEndpoint?client_id=$clientId&request_uri=$requestUri"
    }
}
