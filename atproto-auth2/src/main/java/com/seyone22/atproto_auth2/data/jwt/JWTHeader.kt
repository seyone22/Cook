package com.seyone22.atproto_auth2.data.jwt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JWTHeader(
    @SerialName("typ") val typ: String,
    @SerialName("alg") val alg: String,
    @SerialName("jwk") val jwk: JWK
)