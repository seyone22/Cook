package com.seyone22.atproto_auth2.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DidResponse(
    @SerialName("did") val did: String
)
