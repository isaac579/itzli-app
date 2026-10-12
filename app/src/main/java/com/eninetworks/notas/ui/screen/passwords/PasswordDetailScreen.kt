package com.eninetworks.notas.ui.screen.passwords

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eninetworks.notas.ui.viewmodel.PasswordViewModel
import java.security.SecureRandom

private val PASSWORD_CATEGORIES = listOf("", "Redes sociales", "Banco", "Trabajo", "Correo", "Compras", "Juegos", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(passwordId: Long, viewModel: PasswordViewModel, onBack: () -> Unit) {
    val passwords by viewModel.passwords.collectAsState()
    var pwRef    by remember { mutableStateOf<com.eninetworks.notas.data.entity.Password?>(null) }
    var title    by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var website  by remember { mutableStateOf("") }
    var notes    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(passwordId, passwords) {
        if (passwordId != 0L && pwRef == null) {
            passwords.find { it.id == passwordId }?.let { p ->
                pwRef = p; title = p.title; username = p.username
                password = viewModel.decrypt(p.encryptedPassword)
                website = p.website; notes = p.notes; category = p.category
            }
        }
    }

    BackHandler {
        if (title.isNotBlank()) {
            val ref = pwRef
            if (ref != null) viewModel.update(ref, title, username, password, website, notes, category)
            else viewModel.save(title, username, password, website, notes, category)
        }
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (passwordId == 0L) "Nueva contraseña" else "Editar") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            val ref = pwRef
                            if (ref != null) viewModel.update(ref, title, username, password, website, notes, category)
                            else viewModel.save(title, username, password, website, notes, category)
                        }
                        onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    pwRef?.let { p ->
                        IconButton(onClick = { viewModel.delete(p); onBack() }) { Icon(Icons.Default.Delete, "Eliminar") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
                .verticalScroll(rememberScrollState()).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Categoría
            Text("Categoría", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PASSWORD_CATEGORIES) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(if (cat.isBlank()) "General" else cat, fontSize = 12.sp) }
                    )
                }
            }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nombre / Sitio") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario / Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            // Contraseña + indicador de seguridad
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showPass = !showPass }) { Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) }
                        IconButton(onClick = { clipboard.setText(AnnotatedString(password)) }) { Icon(Icons.Default.ContentCopy, "Copiar") }
                        IconButton(onClick = { password = generatePassword() }) { Icon(Icons.Default.Refresh, "Generar") }
                    }
                }
            )

            // Indicador de seguridad
            val strength = remember(password) { if (password.isNotEmpty()) passwordStrength(password) else null }
            if (strength != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { strength.score },
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = strength.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(strength.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = strength.color)
                }
            }

            OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Sitio web (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas (opcional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        }
    }
}

private data class StrengthResult(val score: Float, val label: String, val color: Color)

private fun passwordStrength(password: String): StrengthResult {
    var score = 0
    if (password.length >= 8)  score++
    if (password.length >= 12) score++
    if (password.length >= 16) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() })     score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return when {
        score <= 2 -> StrengthResult(score / 7f, "Débil",       Color(0xFFE53935))
        score <= 4 -> StrengthResult(score / 7f, "Media",       Color(0xFFFB8C00))
        score <= 6 -> StrengthResult(score / 7f, "Fuerte",      Color(0xFF43A047))
        else       -> StrengthResult(1f,          "Muy fuerte",  Color(0xFF1565C0))
    }
}

private val rng = SecureRandom()

private fun generatePassword(length: Int = 16): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*"
    return (1..length).map { chars[rng.nextInt(chars.length)] }.joinToString("")
}
