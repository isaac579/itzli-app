package com.eninetworks.notas.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.entity.noteType
import com.eninetworks.notas.ui.viewmodel.CheckListViewModel
import com.eninetworks.notas.ui.viewmodel.NoteViewModel
import com.eninetworks.notas.ui.viewmodel.PasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    noteVm: NoteViewModel,
    listVm: CheckListViewModel,
    passVm: PasswordViewModel,
    onNoteClick: (Long) -> Unit,
    onListClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val allNotes     by noteVm.notes.collectAsState()
    val allLists     by listVm.lists.collectAsState()
    val allPasswords by passVm.passwords.collectAsState()

    // Debounce: only filter 200ms after the user stops typing
    LaunchedEffect(query) {
        delay(200)
        debouncedQuery = query.trim().lowercase()
    }

    val q = debouncedQuery
    val filteredNotes = remember(q, allNotes) {
        if (q.isBlank()) emptyList() else allNotes.filter {
            it.title.lowercase().contains(q) || (it.noteType() != NoteType.SECURE && it.content.lowercase().contains(q))
        }
    }
    val filteredLists = remember(q, allLists) {
        if (q.isBlank()) emptyList() else allLists.filter { it.title.lowercase().contains(q) }
    }
    val filteredPasswords = remember(q, allPasswords) {
        if (q.isBlank()) emptyList() else allPasswords.filter {
            it.title.lowercase().contains(q) || it.username.lowercase().contains(q)
        }
    }
    val hasResults = filteredNotes.isNotEmpty() || filteredLists.isNotEmpty() || filteredPasswords.isNotEmpty()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar en Itzli...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, null) }
                            }
                        }
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (query.isBlank()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(12.dp))
                            Text("Escribe para buscar", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else if (!hasResults) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin resultados para \"${query.trim()}\"", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                if (filteredNotes.isNotEmpty()) {
                    item { SectionHeader("Notas", filteredNotes.size) }
                    items(filteredNotes, key = { "note_${it.id}" }) { note ->
                        SearchResultCard(
                            title = note.title.ifBlank { "Sin título" },
                            subtitle = when (note.noteType()) {
                                NoteType.SECURE -> "🔒 Contenido protegido"
                                NoteType.LIST -> "☑️ Lista de elementos"
                                NoteType.PINNED -> "📌 ${note.content.take(60)}"
                                NoteType.TEXT -> note.content.take(60).ifBlank { "Sin contenido" }
                            },
                            icon = { Icon(Icons.Default.EditNote, null, modifier = Modifier.size(20.dp)) },
                            onClick = { onNoteClick(note.id) }
                        )
                    }
                }
                if (filteredLists.isNotEmpty()) {
                    item { SectionHeader("Listas", filteredLists.size) }
                    items(filteredLists, key = { "list_${it.id}" }) { list ->
                        SearchResultCard(
                            title = list.title,
                            subtitle = "Lista de tareas",
                            icon = { Icon(Icons.Default.Checklist, null, modifier = Modifier.size(20.dp)) },
                            onClick = { onListClick(list.id) }
                        )
                    }
                }
                if (filteredPasswords.isNotEmpty()) {
                    item { SectionHeader("Contraseñas", filteredPasswords.size) }
                    items(filteredPasswords, key = { "pass_${it.id}" }) { pass ->
                        SearchResultCard(
                            title = pass.title,
                            subtitle = pass.username.ifBlank { pass.website.ifBlank { "Sin usuario" } },
                            icon = { Icon(Icons.Default.ShieldMoon, null, modifier = Modifier.size(20.dp)) },
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Text(
        "$title ($count)",
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchResultCard(title: String, subtitle: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
        }
    }
}
