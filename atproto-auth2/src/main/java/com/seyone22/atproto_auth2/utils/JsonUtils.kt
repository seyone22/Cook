package com.seyone22.atproto_auth2.utils

fun extractJsonValue(json: String, key: String): String? {
    return Regex("\"$key\":\"(.*?)\"").find(json)?.groups?.get(1)?.value
}
