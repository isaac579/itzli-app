package com.eninetworks.notas.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eninetworks.notas.ui.viewmodel.LockViewModel
import kotlinx.coroutines.delay

private enum class ChangePinStep { VERIFY, NEW, CONFIRM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(viewModel: LockViewModel, onBack: () -> Unit) {
    var step by remember { mutableStateOf(ChangePinStep.VERIFY) }
    var pinInput by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    LaunchedEffect(success) {
        if (success) { delay(1500L); onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar PIN") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        if (success) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("PIN actualizado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tu nuevo PIN está activo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Indicador de progreso
            val stepIndex = step.ordinal + 1
            Text(
                "Paso $stepIndex de 3",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { stepIndex / 3f },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            Text(
                text = when (step) {
                    ChangePinStep.VERIFY  -> "Ingresa tu PIN actual"
                    ChangePinStep.NEW     -> "Ingresa el nuevo PIN"
                    ChangePinStep.CONFIRM -> "Confirma el nuevo PIN"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            if (error.isNotEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            } else {
                Spacer(Modifier.height(16.sp.value.dp))
            }
            Spacer(Modifier.height(20.dp))

            // Puntos de progreso del PIN
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier.size(16.dp).clip(CircleShape).background(
                            if (i < pinInput.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
            Spacer(Modifier.height(40.dp))

            // Teclado numérico
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
            for (row in 0..3) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (col in 0..2) {
                        val k = keys[row * 3 + col]
                        when {
                            k == "⌫" -> IconButton(
                                onClick = {
                                    error = ""
                                    if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                },
                                modifier = Modifier.size(68.dp)
                            ) { Icon(Icons.AutoMirrored.Filled.Backspace, null) }

                            k.isEmpty() -> Spacer(Modifier.size(68.dp))

                            else -> ElevatedButton(
                                onClick = {
                                    error = ""
                                    if (pinInput.length < 4) {
                                        pinInput += k
                                        if (pinInput.length == 4) {
                                            when (step) {
                                                ChangePinStep.VERIFY -> {
                                                    if (!viewModel.verifyPin(pinInput)) {
                                                        error = "PIN incorrecto"
                                                        pinInput = ""
                                                    } else {
                                                        pinInput = ""
                                                        step = ChangePinStep.NEW
                                                    }
                                                }
                                                ChangePinStep.NEW -> {
                                                    newPin = pinInput
                                                    pinInput = ""
                                                    step = ChangePinStep.CONFIRM
                                                }
                                                ChangePinStep.CONFIRM -> {
                                                    if (pinInput != newPin) {
                                                        error = "Los PIN no coinciden"
                                                        newPin = ""
                                                        pinInput = ""
                                                        step = ChangePinStep.NEW
                                                    } else {
                                                        viewModel.setPin(pinInput)
                                                        success = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(68.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text(k, fontSize = 20.sp, fontWeight = FontWeight.Medium) }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
