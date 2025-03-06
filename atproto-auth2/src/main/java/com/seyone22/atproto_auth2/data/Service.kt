package com.seyone22.atproto_auth2.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: String, val type: String, @SerialName("serviceEndpoint") val endpoint: String
)