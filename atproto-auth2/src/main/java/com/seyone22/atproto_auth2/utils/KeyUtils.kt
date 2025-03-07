package com.seyone22.atproto_auth2.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.seyone22.atproto_auth2.data.service.KeyManager
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

fun generateKeyPair(): KeyPair {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)

    // Clear any existing key if present
    keyStore.deleteEntry(KeyManager.KEYALIAS)

    val keyPairGenerator =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        KeyManager.KEYALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    ).run {
        setDigests(KeyProperties.DIGEST_SHA256)
        setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        build()
    }

    keyPairGenerator.initialize(keyGenParameterSpec)

    val keyPair = keyPairGenerator.generateKeyPair()

    // Check key type after generation
    val key = keyStore.getEntry(KeyManager.KEYALIAS, null) as? KeyStore.PrivateKeyEntry

    if (key != null) {
        Log.d("KeyType", "Key Algorithm: ${key.privateKey.algorithm}")
        Log.d("KeyType", "Key Format: ${key.privateKey.format}")
    } else {
        Log.e("KeyType", "Key not found or not of expected type")
    }

    return keyPair
}

// Initialize Keystore and get the key
fun getPrivateKeyFromKeystore(): KeyStore.PrivateKeyEntry? {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    // Check that the key is actually ECPrivateKey
    val entry: KeyStore.Entry = keyStore.getEntry(KeyManager.KEYALIAS, null)

    if (entry !is KeyStore.PrivateKeyEntry) {
        Log.w("TAG", "Not an instance of a PrivateKeyEntry")
        return null
    }

    return entry;
}
