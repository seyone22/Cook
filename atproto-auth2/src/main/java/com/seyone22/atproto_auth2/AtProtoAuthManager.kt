package com.seyone22.atproto_auth2

import android.util.Log
import com.seyone22.atproto_auth2.data.auth.AuthServerResponse
import com.seyone22.atproto_auth2.network.DIDFetcher.fetchDIDDocument
import com.seyone22.atproto_auth2.network.DIDFetcher.fetchDIDFromHandle
import com.seyone22.atproto_auth2.network.KtorClient.client
import com.seyone22.atproto_auth2.network.MetadataFetcher.fetchClientMetadata
import com.seyone22.atproto_auth2.network.MetadataFetcher.fetchOAuthServerMetadata
import com.seyone22.atproto_auth2.network.MetadataFetcher.fetchPDSMetadata
import com.seyone22.atproto_auth2.network.OAuthService.buildRedirectURL
import com.seyone22.atproto_auth2.network.OAuthService.initiatePARRequest
import com.seyone22.atproto_auth2.utils.JwtUtils.createDPoPJWT
import com.seyone22.atproto_auth2.utils.PkceUtils
import com.seyone22.atproto_auth2.utils.generateKeyPair
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

class AtProtoAuthManager(
    private val metadataUrl: String = "https://seyone22.github.io/cook-oauth-metadata/client-metadata.json", // URL of the client metadata JSON file
    private val userHandle: String
) {
    private var dpop_nonce: String? = null
    private var client_id: String? = null
    private var userTokenEndpoint: String? = null
    private var redirectURI: String? = null

    // Generated values
    val state = PkceUtils.generateState() // Generate a unique state value
    val codeVerifier = PkceUtils.generateCodeVerifier() // Generate a code verifier
    val codeChallenge =
        PkceUtils.generateCodeChallenge(codeVerifier) // Generate code challenge based on code verifier

    suspend fun fetchRedirectURI(): String {
        // STAGE 1: Fetch Data
        // 1: Get Client Metadata
        val clientMetadata = fetchClientMetadata(metadataUrl)
        client_id = clientMetadata.client_id;
        redirectURI = clientMetadata.redirect_uris.first()

        // 2. Get User's DID
        val userDID = fetchDIDFromHandle(userHandle, "https://bsky.social");

        // 3. Get User's DID document
        val didDocument = fetchDIDDocument(userDID!!);

        // 4. Get PDS Metadata
        val pdsMetadata = fetchPDSMetadata(didDocument!!.service[0].serviceEndpoint);

        // 5. Fetch Authorization Server Metadata (need authorization_endpoint, token_endpoint, pushed_authorization_request_endpoint)
        val oAuthServerMetadata = fetchOAuthServerMetadata(pdsMetadata!!.authorizationServers[0]);
        userTokenEndpoint = oAuthServerMetadata!!.tokenEndpoint;

        // STAGE 2: AUTHENTICATION
        // 6: Send the PAR Request
        val (requestUri, _nonce) = initiatePARRequest(
            pushedAuthorizationRequestEndpoint = oAuthServerMetadata!!.pushedAuthorizationRequestEndpoint,
            codeChallengeMethod = oAuthServerMetadata.CodeChallengeMethodsSupported[0],
            scope = "atproto transition:generic",
            clientId = clientMetadata.client_id,
            redirectUri = redirectURI!!,
            loginHint = userHandle,
            state = state,
            codeChallenge = codeChallenge
        )
        dpop_nonce = _nonce;

        // 7: User Authentication
        // of the form https://bsky.social/oauth/authorize?client_id=https://oauthbluesky.onrender.com/oauth/client-metadata.json&request_uri=urn:ietf:params:oauth:request_uri:req-34rcew23e
        val redirectURI = buildRedirectURL(
            authorizationEndpoint = oAuthServerMetadata.authorizationEndpoint,
            clientId = clientMetadata.client_id,
            requestUri = requestUri
        )
        return redirectURI;
    }

    suspend fun requestTokenDPoP(
        receivedCode: String,
    ) {
        Log.d("TAG", "requestTokenDPoP: Got to here!")

        Log.d("TAG", "requestTokenDPoP: Got to here middle!")
        val dpopProof = createDPoPJWT(
            htu = userTokenEndpoint!!,
            dpopNonce = dpop_nonce!!
        )

        val response: AuthServerResponse = client.post(userTokenEndpoint!!) {
            contentType(ContentType.Application.FormUrlEncoded)
            headers {
                append("DPOP", dpopProof)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("DPoP-Nonce", dpop_nonce!!)
            }
            setBody(
                Parameters.build {
                    append("grant_type", "authorization_code")
                    append("client_id", client_id!!)
                    append("redirect_uri", redirectURI!!)
                    append("code", receivedCode)
                    append("code_verifier", codeVerifier)
                }.formUrlEncode()
            )
        }.body()

        Log.d("TAG", "requestTokenDPoP: $response");

    }
}
