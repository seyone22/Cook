package com.seyone22.atproto_auth2.data.jwt

import kotlinx.serialization.Serializable

@Serializable
data class JWK(
    val kty: String, val crv: String, val x: String, val y: String
)