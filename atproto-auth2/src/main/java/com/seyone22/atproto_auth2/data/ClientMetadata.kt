package com.seyone22.atproto_auth2.data

import kotlinx.serialization.Serializable

@Serializable
data class ClientMetadata(
    val client_id: String,
    val application_type: String,
    val grant_types: List<String>,
    val scope: String,
    val response_types: List<String>,
    val redirect_uris: List<String>,
    val dpop_bound_access_tokens: Boolean,
    val token_endpoint_auth_method: String,
    val client_name: String,
    val client_uri: String
)
