package com.eninetworks.notas.ui.screen.lists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eninetworks.notas.ui.viewmodel.CheckListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(viewModel: CheckListViewModel, onListClick: (Long) -> Unit, onBack: () -> Unit) {
    val listsWithCounts by viewModel.listsWithCounts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; newTitle = "" },
            title = { Text("Nueva lista") },
            text = { OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Nombre") }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) { viewModel.createList(newTitle.trim()); newTitle = ""; showDialog = false }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false; newTitle = "" }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listas") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, "Nueva lista") } }
    ) { padding ->
        if (listsWithCounts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Sin listas aún", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listsWithCounts, key = { it.list.id }) { lwc ->
                    Card(onClick = { onListClick(lwc.list.id) }, modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(lwc.list.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                if (lwc.total > 0) {
                                    Text(
                                        "${lwc.pending} pendientes · ${lwc.total} total",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteList(lwc.list) }) {
                                    Icon(Icons.Default.Delete, "Eliminar")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
