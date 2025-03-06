package com.seyone22.atproto_auth2.data

import kotlinx.serialization.Serializable

@Serializable
data class DIDDocument(
    val service: List<Service>
)