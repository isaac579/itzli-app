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

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    init { generateKeyIfNeeded() }

    private fun generateKeyIfNeeded() {
        if (keyStore.containsAlias(KEY_ALIAS)) return
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

    private fun getKey(): SecretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getKey()) }
        val combined = cipher.iv + cipher.doFinal(plaintext.toByteArray())
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH, combined, 0, 12))
        }
        return String(cipher.doFinal(combined, 12, combined.size - 12))
    }
}
