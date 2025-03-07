package com.seyone22.atproto_auth2.utils

import SignatureUtils.signMessage
import com.seyone22.atproto_auth2.data.jwt.JWK
import com.seyone22.atproto_auth2.data.jwt.JWTHeader
import com.seyone22.atproto_auth2.data.jwt.JWTPayload
import com.seyone22.atproto_auth2.data.service.KeyManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore
import java.util.Base64
import java.util.UUID

/**
 * Utility class to handle creation and signing of DPoP JWTs.
 */
object JwtUtils {

    /**
     * Creates a DPoP JWT from the private and public EC keys stored in the Keystore.
     *
     * @param htu The HTTP URI the JWT is associated with.
     * @param dpopNonce A unique nonce to prevent replay attacks.
     * @return The signed JWT string.
     */
    fun createDPoPJWT(
        htu: String, dpopNonce: String
    ): String {
        val uuid = UUID.randomUUID().toString()
        val iat = (System.currentTimeMillis() / 1000).toInt()

        generateKeyPair();

        // Load the Keystore and retrieve the keys
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null) // Load the Keystore

        // Retrieve the private key for signing
        val publicKey = keyStore.getCertificate(KeyManager.KEYALIAS).publicKey

        // Convert the public key to JWK
        val jwk = JWK(
            kty = "EC",
            crv = "P-256",
            x = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.encoded.copyOfRange(0, 32)), // Get the X component
            y = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.encoded.copyOfRange(32, 64)) // Get the Y component
        )

        // Create JWT header and payload
        val jwtHeader = JWTHeader(jwk = jwk, alg = "ES256", typ = "dpop+jwt")
        val jwtPayload = JWTPayload(uuid, "POST", htu, iat, dpopNonce)

        // Serialize header and payload
        val headerJson = Json.encodeToString(jwtHeader)
        val payloadJson = Json.encodeToString(jwtPayload)

        // Base64 encode the header and payload
        val base64Header =
            Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.toByteArray())
        val base64Payload =
            Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray())

        // Combine header and payload into message
        val message = "$base64Header.$base64Payload"

        // Sign the message using the Keystore private key
        val signature = signMessage(message)
        val base64Signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature)

        // Return the final JWT with the signature
        return "$message.$base64Signature"
    }
}
