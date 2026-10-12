package com.eninetworks.notas.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "notas_vault_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128

    private val keyStore: KeyStore? = try {
        KeyStore.getInstance(KEYSTORE).apply { load(null) }
    } catch (_: Exception) { null }

    init {
        try { generateKeyIfNeeded() } catch (_: Exception) { }
    }

    private fun generateKeyIfNeeded() {
        val ks = keyStore ?: return
        if (ks.containsAlias(KEY_ALIAS)) return
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    private fun getKey(): SecretKey? = try {
        keyStore?.getKey(KEY_ALIAS, null) as? SecretKey
    } catch (_: Exception) { null }

    fun encrypt(plaintext: String): String {
        return try {
            val key = getKey() ?: return plaintext
            val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key) }
            val combined = cipher.iv + cipher.doFinal(plaintext.toByteArray())
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (_: Exception) { plaintext }
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            if (combined.size <= 12) return encryptedText
            val key = getKey() ?: return encryptedText
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, combined, 0, 12))
            }
            String(cipher.doFinal(combined, 12, combined.size - 12))
        } catch (_: Exception) { encryptedText }
    }
}
