package com.eninetworks.notas.ui.screen.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eninetworks.notas.data.entity.Note
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.entity.noteType
import com.eninetworks.notas.data.entity.parseListItems
import com.eninetworks.notas.ui.theme.NoteColors
import com.eninetworks.notas.ui.viewmodel.LabelViewModel
import com.eninetworks.notas.ui.viewmodel.NoteSort
import com.eninetworks.notas.ui.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    labelViewModel: LabelViewModel,
    onNoteClick: (Long) -> Unit,
    onAddNote: () -> Unit,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val labelFilter by viewModel.labelFilter.collectAsState()
    val currentSort by viewModel.sort.collectAsState()
    val allLabels by labelViewModel.labels.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, "Ordenar")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            val sortOptions = listOf(
                                NoteSort.DATE_DESC to "Más recientes",
                                NoteSort.DATE_ASC to "Más antiguas",
                                NoteSort.TITLE_AZ to "Título A–Z",
                                NoteSort.COLOR to "Por color"
                            )
                            sortOptions.forEach { (sort, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { viewModel.setSort(sort); showSortMenu = false },
                                    trailingIcon = {
                                        if (currentSort == sort) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) { Icon(Icons.Default.Add, "Nueva nota") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearch,
                placeholder = { Text("Buscar...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { viewModel.setSearch("") }) { Icon(Icons.Default.Clear, null) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = typeFilter == null && labelFilter == null,
                        onClick = { viewModel.setTypeFilter(null); viewModel.setLabelFilter(null) },
                        label = { Text("Todas") }
                    )
                }
                items(NoteType.entries) { type ->
                    FilterChip(
                        selected = typeFilter == type,
                        onClick = { viewModel.setTypeFilter(if (typeFilter == type) null else type) },
                        label = { Text("${type.emoji} ${type.label}") }
                    )
                }
                if (allLabels.isNotEmpty()) {
                    item { VerticalDivider(modifier = Modifier.height(32.dp).padding(horizontal = 4.dp)) }
                    items(allLabels) { label ->
                        FilterChip(
                            selected = labelFilter == label.id,
                            onClick = { viewModel.setLabelFilter(if (labelFilter == label.id) null else label.id) },
                            label = { Text("🏷 ${label.name}") }
                        )
                    }
                }
            }

            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin notas aún", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(notes, key = { it.id }) { note ->
                        when (note.noteType()) {
                            NoteType.PINNED -> PinnedNoteCard(note) { onNoteClick(note.id) }
                            NoteType.LIST   -> ListNoteCard(note) { onNoteClick(note.id) }
                            NoteType.SECURE -> SecureNoteCard(note) { onNoteClick(note.id) }
                            NoteType.TEXT   -> TextNoteCard(note) { onNoteClick(note.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextNoteCard(note: Note, onClick: () -> Unit) {
    val bg = remember(note.color) { NoteColors.getOrElse(note.color) { NoteColors[0] } }
    val onBg = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color(0xFFE6E1E5)
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg, contentColor = onBg)) {
        Column(Modifier.padding(12.dp)) {
            NoteCardHeader(note)
            if (note.content.isNotEmpty()) {
                Text(note.content, fontSize = 13.sp, maxLines = 8, overflow = TextOverflow.Ellipsis, color = LocalContentColor.current.copy(alpha = 0.7f))
                Spacer(Modifier.height(6.dp))
            }
            NoteCardDate(note)
        }
    }
}

@Composable
private fun PinnedNoteCard(note: Note, onClick: () -> Unit) {
    val bg = remember(note.color) { NoteColors.getOrElse(note.color) { NoteColors[0] } }
    val onBg = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color(0xFFE6E1E5)
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg, contentColor = onBg)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                NoteCardHeaderInline(note, modifier = Modifier.weight(1f))
                Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(4.dp))
            if (note.content.isNotEmpty()) {
                Text(note.content, fontSize = 13.sp, maxLines = 6, overflow = TextOverflow.Ellipsis, color = LocalContentColor.current.copy(alpha = 0.7f))
                Spacer(Modifier.height(6.dp))
            }
            NoteCardDate(note)
        }
    }
}

@Composable
private fun ListNoteCard(note: Note, onClick: () -> Unit) {
    val bg = remember(note.color) { NoteColors.getOrElse(note.color) { NoteColors[0] } }
    val onBg = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color(0xFFE6E1E5)
    val items = remember(note.content) { parseListItems(note.content) }
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg, contentColor = onBg)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                NoteCardHeaderInline(note, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Checklist, null, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(6.dp))
            items.take(4).forEach { (text, checked) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = if (checked) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    Text(
                        text,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (checked) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
            if (items.size > 4) Text("+ ${items.size - 4} más", fontSize = 11.sp, color = LocalContentColor.current.copy(alpha = 0.5f))
            Spacer(Modifier.height(4.dp))
            NoteCardDate(note)
        }
    }
}

@Composable
private fun SecureNoteCard(note: Note, onClick: () -> Unit) {
    val bg = remember(note.color) { NoteColors.getOrElse(note.color) { NoteColors[0] } }
    val onBg = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color(0xFFE6E1E5)
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg, contentColor = onBg)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                NoteCardHeaderInline(note, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Contenido protegido", fontSize = 12.sp, color = LocalContentColor.current.copy(alpha = 0.5f))
            Spacer(Modifier.height(6.dp))
            NoteCardDate(note)
        }
    }
}

@Composable
private fun NoteCardHeader(note: Note) {
    if (note.title.isNotEmpty()) {
        Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun NoteCardHeaderInline(note: Note, modifier: Modifier = Modifier) {
    if (note.title.isNotEmpty()) {
        Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
    }
}

@Composable
private fun NoteCardDate(note: Note) {
    val fmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    Text(
        fmt.format(Date(note.updatedAt)),
        fontSize = 11.sp,
        color = LocalContentColor.current.copy(alpha = 0.5f)
    )
}
