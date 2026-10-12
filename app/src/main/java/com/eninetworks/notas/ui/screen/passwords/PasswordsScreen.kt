package com.eninetworks.notas.ui.screen.passwords

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eninetworks.notas.ui.viewmodel.PasswordViewModel

private val FILTER_CATEGORIES = listOf("", "Redes sociales", "Banco", "Trabajo", "Correo", "Compras", "Juegos", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(viewModel: PasswordViewModel, onPasswordClick: (Long) -> Unit, onAddPassword: () -> Unit, onBack: () -> Unit) {
    val passwords      by viewModel.passwords.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Contraseñas") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddPassword) { Icon(Icons.Default.Add, "Agregar") } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            // Categorías
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FILTER_CATEGORIES) { cat ->
                    FilterChip(
                        selected = categoryFilter == cat,
                        onClick  = { viewModel.setCategoryFilter(if (categoryFilter == cat && cat.isNotBlank()) "" else cat) },
                        label    = { Text(if (cat.isBlank()) "Todas" else cat) }
                    )
                }
            }

            if (passwords.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin contraseñas guardadas", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(passwords, key = { it.id }) { pw ->
                        Card(onClick = { onPasswordClick(pw.id) }, modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                leadingContent  = { Icon(Icons.Default.Lock, null) },
                                headlineContent = { Text(pw.title) },
                                supportingContent = {
                                    val sub = listOfNotNull(pw.username.ifBlank { null }, pw.category.ifBlank { null }).joinToString(" · ")
                                    if (sub.isNotBlank()) Text(sub)
                                },
                                trailingContent = { if (pw.website.isNotEmpty()) Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.outline) }
                            )
                        }
                    }
                }
            }
        }
    }
}
