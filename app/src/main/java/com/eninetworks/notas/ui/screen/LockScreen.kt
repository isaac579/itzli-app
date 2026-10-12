package com.eninetworks.notas.ui.screen

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.eninetworks.notas.ui.viewmodel.LockViewModel

@Composable
fun LockScreen(viewModel: LockViewModel, onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val hasPinSet by viewModel.hasPinSet.collectAsState()

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var waitingConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val isUnlocked by viewModel.isUnlocked.collectAsState()

    LaunchedEffect(Unit) { viewModel.initialize() }
    LaunchedEffect(hasPinSet) { if (hasPinSet && !isUnlocked) tryBiometric(context, viewModel, onUnlocked) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                waitingConfirm -> "Confirmar PIN"
                !hasPinSet -> "Crear PIN de 4 dígitos"
                else -> "Ingresa tu PIN"
            },
            fontSize = 22.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        Spacer(Modifier.height(24.dp))

        val current = if (waitingConfirm) confirmPin else pin
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier.size(18.dp).clip(CircleShape).background(
                        if (i < current.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
        Spacer(Modifier.height(40.dp))

        val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        for (row in 0..3) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (col in 0..2) {
                    val k = keys[row * 3 + col]
                    when {
                        k == "⌫" -> IconButton(onClick = {
                            error = ""
                            if (waitingConfirm) confirmPin = confirmPin.dropLast(1)
                            else pin = pin.dropLast(1)
                        }, modifier = Modifier.size(72.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, null)
                        }
                        k.isEmpty() -> Spacer(Modifier.size(72.dp))
                        else -> ElevatedButton(
                            onClick = {
                                error = ""
                                if (waitingConfirm) {
                                    if (confirmPin.length < 4) {
                                        confirmPin += k
                                        if (confirmPin.length == 4) {
                                            if (confirmPin == pin) { viewModel.setPin(pin); onUnlocked() }
                                            else { error = "Los PIN no coinciden"; pin = ""; confirmPin = ""; waitingConfirm = false }
                                        }
                                    }
                                } else {
                                    if (pin.length < 4) {
                                        pin += k
                                        if (pin.length == 4) {
                                            if (hasPinSet) {
                                                if (!viewModel.verifyPin(pin)) { error = "PIN incorrecto"; pin = "" }
                                                else onUnlocked()
                                            } else {
                                                waitingConfirm = true
                                            }
                                        }
                                    }
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(k, fontSize = 22.sp, fontWeight = FontWeight.Medium) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (hasPinSet) {
            Spacer(Modifier.height(8.dp))
            IconButton(onClick = { tryBiometric(context, viewModel, onUnlocked) }) {
                Icon(Icons.Default.Fingerprint, "Huella digital", modifier = Modifier.size(40.dp))
            }
        }
    }
}

private fun tryBiometric(context: android.content.Context, vm: LockViewModel, onUnlocked: () -> Unit) {
    val mgr = BiometricManager.from(context)
    if (mgr.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) return
    val prompt = BiometricPrompt(
        context as FragmentActivity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                vm.unlock(); onUnlocked()
            }
        }
    )
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear Notas")
            .setSubtitle("Usa tu huella o rostro")
            .setNegativeButtonText("Usar PIN")
            .build()
    )
}
