package com.eninetworks.notas.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class LockViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(app)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                app, "notas_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            app.getSharedPreferences("notas_prefs_plain", Context.MODE_PRIVATE)
        }
    }

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    private val _hasPinSet = MutableStateFlow(false)
    val hasPinSet = _hasPinSet.asStateFlow()

    fun initialize() {
        _hasPinSet.value = prefs.contains("pin_hash")
    }

    fun setPin(pin: String) {
        prefs.edit().putString("pin_hash", hash(pin)).apply()
        _hasPinSet.value = true
        _isUnlocked.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString("pin_hash", null) ?: return false
        return (stored == hash(pin)).also { if (it) _isUnlocked.value = true }
    }

    fun unlock() { _isUnlocked.value = true }

    private fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
