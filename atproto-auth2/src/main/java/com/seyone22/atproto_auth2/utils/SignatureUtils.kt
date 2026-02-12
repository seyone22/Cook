package com.seyone22.atproto_auth2.utils

import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.Signature
import java.security.interfaces.ECPrivateKey

object SignatureUtils {
    /**
     * Signs a message using ECDSA with SHA-256.
     *
     * @param privateKey The private EC key used for signing.
     * @param message The message to sign.
     * @return The ECDSA signature in raw R || S format.
     */
    fun signMessage(privateKey: ECPrivateKey, message: String): ByteArray {
        val signature = Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
            update(message.toByteArray())
        }.sign()

        return derToConcatenated(signature)
    }

    /**
     * Converts DER-encoded ECDSA signature to raw R || S format.
     *
     * @param derSignature The DER-encoded ECDSA signature.
     * @return The raw R || S format signature.
     */
    private fun derToConcatenated(derSignature: ByteArray): ByteArray {
        val byteBuffer = ByteBuffer.wrap(derSignature)

        // Read ASN.1 structure
        require(byteBuffer.get().toInt() == 0x30) { "Invalid ECDSA signature format" }
        byteBuffer.get() // Skip length byte

        // Read R
        require(byteBuffer.get().toInt() == 0x02) { "Invalid ECDSA R integer marker" }
        val rLength = byteBuffer.get().toInt()
        val rBytes = ByteArray(rLength)
        byteBuffer.get(rBytes)
        val r = BigInteger(1, rBytes).toByteArray()

        // Read S
        require(byteBuffer.get().toInt() == 0x02) { "Invalid ECDSA S integer marker" }
        val sLength = byteBuffer.get().toInt()
        val sBytes = ByteArray(sLength)
        byteBuffer.get(sBytes)
        val s = BigInteger(1, sBytes).toByteArray()

        // Ensure R and S are exactly 32 bytes each
        val rFixed = r.copyOfRange(r.size - 32, r.size)
        val sFixed = s.copyOfRange(s.size - 32, s.size)

        return rFixed + sFixed
    }
}