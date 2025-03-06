package com.seyone22.atproto_auth2.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.util.Base64
import java.util.UUID

@Serializable
data class JWK(
    val kty: String = "EC", val crv: String = "P-256", val x: String, val y: String
)

private fun convertPublicKeyToJWK(publicKey: ECPublicKey): String {
    val x =
        Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.w.affineX.toByteArray())
    val y =
        Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.w.affineY.toByteArray())
    val jwk = JWK(x = x, y = y)
    return Json.encodeToString(jwk)
}

fun generateKeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("EC")
    keyGen.initialize(256)
    return keyGen.generateKeyPair()
}

// TODO: Fix Bug here
fun createDPoPJWT(
    privateKey: ECPrivateKey, publicKey: ECPublicKey, htu: String, dpopNonce: String
): String {
    val uuid = UUID.randomUUID().toString()
    val iat = (System.currentTimeMillis() / 1000).toInt()

    // Convert public key to JWK format
    val jwk = convertPublicKeyToJWK(publicKey)

    // Serialize JWK directly without decoding it
    val header = Json.encodeToString(
        mapOf(
            "typ" to "dpop+jwt",
            "alg" to "ES256",
            "jwk" to jwk
        )
    )

    val payload = Json.encodeToString(
        mapOf(
            "jti" to uuid, "htm" to "POST", "htu" to htu, "iat" to iat, "nonce" to dpopNonce
        )
    )

    val base64Header = Base64.getUrlEncoder().withoutPadding().encodeToString(header.toByteArray())
    val base64Payload =
        Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
    val message = "$base64Header.$base64Payload"

    val signature = Signature.getInstance("SHA256withECDSA").apply {
        initSign(privateKey)
        update(message.toByteArray())
    }.sign()

    val base64Signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature)
    return "$message.$base64Signature"
}