package com.eninetworks.notas.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.entity.noteType
import com.eninetworks.notas.ui.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(viewModel: NoteViewModel, onBack: () -> Unit) {
    val trashed by viewModel.trashed.collectAsState()
    var showConfirmEmpty by remember { mutableStateOf(false) }

    if (showConfirmEmpty) {
        AlertDialog(
            onDismissRequest = { showConfirmEmpty = false },
            title = { Text("Vaciar papelera") },
            text = { Text("Se eliminarán permanentemente todas las notas en la papelera. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.emptyTrash(); showConfirmEmpty = false }) {
                    Text("Vaciar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showConfirmEmpty = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papelera") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (trashed.isNotEmpty()) {
                        IconButton(onClick = { showConfirmEmpty = true }) {
                            Icon(Icons.Default.DeleteForever, "Vaciar papelera", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (trashed.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RestoreFromTrash, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text("La papelera está vacía", color = MaterialTheme.colorScheme.outline)
                    Text("Las notas eliminadas aparecen aquí 30 días", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Las notas se eliminan automáticamente después de 30 días",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(trashed, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (note.title.isNotBlank()) {
                                    Text(note.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                val preview = when (note.noteType()) {
                                    NoteType.SECURE -> "Contenido protegido"
                                    else -> note.content.take(60).ifBlank { "Sin contenido" }
                                }
                                Text(preview, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val deletedFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                                note.deletedAt?.let { ts ->
                                    Text("Eliminada el ${deletedFmt.format(Date(ts))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            IconButton(onClick = { viewModel.restore(note) }) {
                                Icon(Icons.Default.RestoreFromTrash, "Restaurar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deletePermanently(note) }) {
                                Icon(Icons.Default.DeleteForever, "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
