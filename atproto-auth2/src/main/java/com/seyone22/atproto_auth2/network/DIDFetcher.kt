package com.seyone22.atproto_auth2.network

import com.seyone22.atproto_auth2.data.did.DIDDocument
import com.seyone22.atproto_auth2.data.did.ResolveHandleResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Object responsible for fetching Decentralized Identifiers (DIDs) and related metadata.
 *
 * This utility provides functions to resolve a DID from a handle and to fetch the DID document
 * containing cryptographic keys, authentication methods, and service endpoints.
 */
object DIDFetcher {

    /**
     * Resolves a DID (Decentralized Identifier) from a given handle.
     *
     * In the AT Protocol, handles (e.g., "user.bsky.social") can be resolved into DIDs,
     * which serve as stable, unique identifiers for users. This function queries the
     * identity resolution service to retrieve the associated DID.
     *
     * @param handle The human-readable handle to be resolved (e.g., "example.bsky.social").
     * @param serviceEndpoint The base URL of the identity resolution service.
     * @return A DID as a [String], or `null` if resolution fails.
     */
    suspend fun fetchDIDFromHandle(handle: String, serviceEndpoint: String): String? {
        return try {
            val response: ResolveHandleResponse =
                KtorClient.client.get("$serviceEndpoint/xrpc/com.atproto.identity.resolveHandle") {
                    parameter("handle", handle)
                }.body()
            response.did // Return the resolved DID
        } catch (e: Exception) {
            null // Return null to gracefully handle network or parsing errors
        }
    }

    /**
     * Fetches the DID document associated with a given DID.
     *
     * A DID document contains cryptographic keys, authentication methods, and service endpoints
     * necessary for interacting with decentralized identity systems. This function retrieves
     * the document from the AT Protocol's PLC (Persistent Logical Clock) directory.
     *
     * @param did The Decentralized Identifier (e.g., "did:plc:abcd1234xyz").
     * @return A [DIDDocument] object containing the DID's metadata, or `null` if fetching fails.
     */
    suspend fun fetchDIDDocument(did: String): DIDDocument? {
        val url = "https://plc.directory/$did"
        return try {
            val response: DIDDocument = KtorClient.client.get(url).body()
            response // Return the successfully fetched DID document
        } catch (e: Exception) {
            println("Error fetching DID document: ${e.message}")
            null // Return null to indicate failure
        }
    }
}
