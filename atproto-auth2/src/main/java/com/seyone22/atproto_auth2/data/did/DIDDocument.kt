package com.seyone22.atproto_auth2.data.did

import com.seyone22.atproto_auth2.data.service.Service
import kotlinx.serialization.Serializable

@Serializable
data class DIDDocument(
    val id: String,
    val alsoKnownAs: List<String>,
    val service: List<Service>
)