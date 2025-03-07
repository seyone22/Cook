package com.seyone22.atproto_auth2.utils

import com.seyone22.atproto_auth2.data.auth.CallbackParams
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UrlUtils {
    fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }

    fun parseCallbackUrl(callbackUrl: String): CallbackParams? {
        val regex = "https?://([^/]+)/[^?]+\\?iss=([^&]+)&state=([^&]+)&code=([^&]+)".toRegex()
        val matchResult = regex.find(callbackUrl)

        return if (matchResult != null) {
            val iss = matchResult.groupValues[2]
            val state = matchResult.groupValues[3]
            val code = matchResult.groupValues[4]

            CallbackParams(iss, state, code)
        } else {
            null // Invalid URL format
        }
    }

}
