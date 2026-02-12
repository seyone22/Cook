package com.seyone22.atproto_auth2.utils

import com.seyone22.atproto_auth2.data.jwt.JWK
import com.seyone22.atproto_auth2.data.jwt.JWTHeader
import com.seyone22.atproto_auth2.data.jwt.JWTPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.util.Base64
import java.util.UUID

/**
 * Utility class to handle creation and signing of DPoP JWTs.
 */
object JwtUtils {

    /**
     * Creates a DPoP JWT from the private and public EC keys.
     *
     * @param privateKey The private EC key used to sign the JWT.
     * @param publicKey The public EC key used to create the JWK for the JWT.
     * @param htu The HTTP URI the JWT is associated with.
     * @param dpopNonce A unique nonce to prevent replay attacks.
     * @return The signed JWT string.
     */
    fun createDPoPJWT(
        htu: String, dpopNonce: String
    ): String {
        val keyPair = generateKeyPair()

        val privateKey = keyPair.private as ECPrivateKey;
        val publicKey = keyPair.public as ECPublicKey;

        val uuid = UUID.randomUUID().toString()
        val iat = (System.currentTimeMillis() / 1000).toInt()

        // Convert public key to JWK
        val jwk = JWK(
            kty = "EC",
            crv = "P-256",
            x = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.w.affineX.toByteArray()),
            y = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.w.affineY.toByteArray())
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

        // Sign the message and return the final JWT
        val signature = SignatureUtils.signMessage(privateKey, message)
        val base64Signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature)

        return "$message.$base64Signature"
    }
}
