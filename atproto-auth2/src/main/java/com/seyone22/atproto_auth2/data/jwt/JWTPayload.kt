package com.seyone22.atproto_auth2.data.jwt

import kotlinx.serialization.Serializable

@Serializable
data class JWTPayload(
    val jti: String, val htm: String, val htu: String, val iat: Int, val nonce: String
)