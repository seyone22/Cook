package com.seyone22.atproto_auth2.data.did

import kotlinx.serialization.Serializable

// Define response model
@Serializable
data class ResolveHandleResponse(
    val did: String
)