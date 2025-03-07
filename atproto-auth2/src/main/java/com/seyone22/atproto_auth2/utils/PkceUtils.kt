package com.seyone22.atproto_auth2.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

object PkceUtils {
    fun generateCodeVerifier(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 32 bytes → ~43+ base64 chars
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun generateState(): String {
        return UUID.randomUUID().toString().replace("-", "") // 32-char random string
    }
}
