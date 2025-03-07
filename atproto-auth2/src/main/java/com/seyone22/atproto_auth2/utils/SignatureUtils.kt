import android.util.Log
import com.seyone22.atproto_auth2.data.service.KeyManager
import com.seyone22.atproto_auth2.utils.getPrivateKeyFromKeystore
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.Signature

object SignatureUtils {
    /**
     * Signs a message using ECDSA with SHA-256, using the private key stored in the Keystore.
     *
     * @param message The message to sign.
     * @return The ECDSA signature in raw R || S format.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun signMessage(message: String): ByteArray {

        Log.d("TAG", "requestTokenDPoP: Got to here 1!")

        // Check if key exists
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        Log.d("TAG", "Keystore loaded: ${keyStore.containsAlias(KeyManager.KEYALIAS)}")

        val privateKey = getPrivateKeyFromKeystore()?.privateKey
        if (privateKey == null) {
            Log.e("TAG", "Private key is not available.")
        } else {
            Log.d("TAG", "Private key successfully retrieved.")
        }

        Log.d("TAG", "Private key type: ${privateKey?.javaClass?.name}")

        try {
            val signature = Signature.getInstance("SHA256withECDSA").apply {
                initSign(privateKey)
                update(message.toByteArray())
            }.sign()

            Log.d("TAG", "Signature created: ${signature.toHexString()}")
            return signature
        } catch (e: Exception) {
            Log.e("TAG", "Keystore operation failed: ${e.message}", e)
            return ByteArray(3)
        }

        Log.d("TAG", "requestTokenDPoP: Got to here 2!")


    }

    /**
     * Converts DER-encoded ECDSA signature to raw R || S format.
     *
     * @param derSignature The DER-encoded ECDSA signature.
     * @return The raw R || S format signature.
     */
    fun derToConcatenated(derSignature: ByteArray): ByteArray {
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
