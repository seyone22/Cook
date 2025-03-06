package com.seyone22.atproto_auth2.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthProtectedResource(
    val resource: String,
    @SerialName("authorization_servers") val authorizationServers: List<String> = emptyList()
)