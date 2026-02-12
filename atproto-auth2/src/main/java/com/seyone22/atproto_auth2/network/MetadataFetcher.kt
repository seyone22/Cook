package com.seyone22.atproto_auth2.network

import android.util.Log
import com.seyone22.atproto_auth2.data.oauth.ClientMetadata
import com.seyone22.atproto_auth2.data.oauth.OAuthProtectedResource
import com.seyone22.atproto_auth2.data.oauth.OAuthServerMetadata
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

/**
 * Object responsible for fetching metadata related to OAuth and authentication services.
 */
object MetadataFetcher {

    /**
     * Fetches client metadata from the given URL.
     *
     * Client metadata typically includes information required for OAuth authentication,
     * such as supported grant types, token endpoints, and other relevant configuration details.
     *
     * @param metadataUrl The URL to fetch the client metadata from.
     * @return A [ClientMetadata] object containing the fetched metadata.
     * @throws Exception if the request fails or the response is not successful.
     */
    suspend fun fetchClientMetadata(metadataUrl: String): ClientMetadata {
        try {
            val response: HttpResponse = KtorClient.client.get(metadataUrl)
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

    /**
     * Fetches OAuth-protected resource metadata from a Personal Data Server (PDS).
     *
     * This metadata typically includes information about OAuth token introspection,
     * revocation endpoints, and access policies for the PDS.
     *
     * @param serviceEndpoint The base URL of the PDS where metadata is stored.
     * @return An [OAuthProtectedResource] object containing the metadata, or `null` if an error occurs.
     */
    suspend fun fetchPDSMetadata(serviceEndpoint: String): OAuthProtectedResource? {
        return try {
            val url = "$serviceEndpoint/.well-known/oauth-protected-resource"
            val response: OAuthProtectedResource = KtorClient.client.get(url).body()
            response // Successfully retrieved metadata
        } catch (e: Exception) {
            null // Return null in case of an error to allow graceful handling
        }
    }

    /**
     * Fetches OAuth Authorization Server metadata from the given server URL.
     *
     * The metadata provides details about the authorization server, including supported OAuth flows,
     * endpoints for authorization and token issuance, and other relevant OAuth capabilities.
     *
     * @param serverUrl The base URL of the OAuth authorization server.
     * @return An [OAuthServerMetadata] object containing the metadata, or `null` if an error occurs.
     */
    suspend fun fetchOAuthServerMetadata(serverUrl: String): OAuthServerMetadata? {
        Log.d("TAG", "fetchOAuthServerMetadata: $serverUrl")

        return try {
            val url = "$serverUrl/.well-known/oauth-authorization-server"
            val response: OAuthServerMetadata = KtorClient.client.get(url).body()
            response // Successfully retrieved metadata
        } catch (e: Exception) {
            null // Return null if fetching fails
        }
    }
}
