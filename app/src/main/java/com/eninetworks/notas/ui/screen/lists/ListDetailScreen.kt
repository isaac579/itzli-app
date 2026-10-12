package com.eninetworks.notas.ui.screen.lists

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.eninetworks.notas.data.entity.CheckListItem
import com.eninetworks.notas.ui.viewmodel.CheckListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(listId: Long, viewModel: CheckListViewModel, onBack: () -> Unit) {
    val items     by viewModel.getItemsForList(listId).collectAsState()
    val lists     by viewModel.lists.collectAsState()
    var newText   by remember { mutableStateOf("") }
    val listTitle  = lists.find { it.id == listId }?.title ?: "Lista"

    val pending by remember(items) { derivedStateOf { items.filter { !it.isChecked } } }
    val done    by remember(items) { derivedStateOf { items.filter { it.isChecked } } }

    val localItems  = remember { mutableStateListOf<CheckListItem>() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var offsetY       by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val itemHeightPx = remember { with(density) { 56.dp.toPx() } }

    LaunchedEffect(pending) {
        if (draggingIndex == -1) {
            localItems.clear()
            localItems.addAll(pending)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listTitle) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Add item row
            item(key = "add_row") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newText, onValueChange = { newText = it },
                        placeholder = { Text("Agregar elemento...") },
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    IconButton(onClick = {
                        if (newText.isNotBlank()) {
                            viewModel.addItem(listId, newText.trim(), pending.size)
                            newText = ""
                        }
                    }) { Icon(Icons.Default.Add, "Agregar") }
                }
            }

            // Pending items — draggable, virtualized
            itemsIndexed(localItems, key = { _, item -> item.id }) { index, item ->
                val isDragging = index == draggingIndex
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragging) offsetY else 0f
                            shadowElevation = if (isDragging) 8f else 0f
                        }
                ) {
                    Icon(
                        Icons.Default.DragHandle, null,
                        modifier = Modifier
                            .size(24.dp)
                            .pointerInput(item.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = localItems.indexOfFirst { it.id == item.id }
                                        offsetY = 0f
                                    },
                                    onDragEnd = {
                                        viewModel.reorderItems(localItems.toList())
                                        draggingIndex = -1; offsetY = 0f
                                    },
                                    onDragCancel = { draggingIndex = -1; offsetY = 0f },
                                    onDrag = { _, dragAmount ->
                                        offsetY += dragAmount.y
                                        val threshold = itemHeightPx / 2
                                        val cur = draggingIndex
                                        when {
                                            offsetY > threshold && cur < localItems.size - 1 -> {
                                                localItems.add(cur + 1, localItems.removeAt(cur))
                                                draggingIndex = cur + 1; offsetY -= itemHeightPx
                                            }
                                            offsetY < -threshold && cur > 0 -> {
                                                localItems.add(cur - 1, localItems.removeAt(cur))
                                                draggingIndex = cur - 1; offsetY += itemHeightPx
                                            }
                                        }
                                    }
                                )
                            },
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Checkbox(checked = false, onCheckedChange = { viewModel.toggleItem(item) })
                    Text(item.text, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteItem(item) }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Done section — virtualized in the same LazyColumn
            if (done.isNotEmpty()) {
                item(key = "done_header") {
                    Text(
                        "Completados (${done.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                items(done, key = { it.id }) { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.size(24.dp))
                        Checkbox(checked = true, onCheckedChange = { viewModel.toggleItem(item) })
                        Text(
                            item.text,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                        IconButton(onClick = { viewModel.deleteItem(item) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
