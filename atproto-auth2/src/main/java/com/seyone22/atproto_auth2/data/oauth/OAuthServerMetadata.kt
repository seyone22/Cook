package com.seyone22.atproto_auth2.data.oauth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthServerMetadata(
    @SerialName("code_challenge_methods_supported") val CodeChallengeMethodsSupported: List<String>,
    @SerialName("authorization_endpoint") val authorizationEndpoint: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
    @SerialName("pushed_authorization_request_endpoint") val pushedAuthorizationRequestEndpoint: String
)