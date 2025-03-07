package com.seyone22.atproto_auth2.utils

import java.security.KeyPair
import java.security.KeyPairGenerator

fun generateKeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("EC")
    keyGen.initialize(256)
    return keyGen.generateKeyPair()
}