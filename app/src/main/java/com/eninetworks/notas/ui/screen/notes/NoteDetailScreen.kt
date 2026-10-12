package com.eninetworks.notas.ui.screen.notes

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eninetworks.notas.data.entity.Label
import com.eninetworks.notas.data.entity.Note
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.entity.buildListContent
import com.eninetworks.notas.data.entity.noteType
import com.eninetworks.notas.data.entity.parseListItems
import com.eninetworks.notas.ui.theme.NoteColors
import com.eninetworks.notas.ui.viewmodel.LabelViewModel
import com.eninetworks.notas.ui.viewmodel.LockViewModel
import com.eninetworks.notas.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    lockViewModel: LockViewModel,
    labelViewModel: LabelViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var noteRef by remember { mutableStateOf<Note?>(null) }
    var title   by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var color   by remember { mutableIntStateOf(0) }
    var noteType by remember { mutableStateOf(NoteType.TEXT) }
    var imagePath  by remember { mutableStateOf<String?>(null) }
    var reminderAt by remember { mutableStateOf<Long?>(null) }

    var listItems   by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var newItemText by remember { mutableStateOf("") }

    var isSecureUnlocked by remember { mutableStateOf(noteId == 0L) }
    var pinInput  by remember { mutableStateOf("") }
    var pinError  by remember { mutableStateOf("") }

    // Label state
    val allLabels      by labelViewModel.labels.collectAsState()
    val noteLabelsFlow = remember(noteId) {
        if (noteId != 0L) labelViewModel.getLabelsForNote(noteId) else flowOf(emptyList())
    }
    val noteLabels by noteLabelsFlow.collectAsState(emptyList())
    var selectedLabels by remember { mutableStateOf(setOf<Label>()) }
    var showLabelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteLabels) { selectedLabels = noteLabels.toSet() }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val dir  = File(context.filesDir, "note_images").also { d -> d.mkdirs() }
            val dest = File(dir, "${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(it)?.use { inp -> dest.outputStream().use { out -> inp.copyTo(out) } }
            imagePath?.let { old -> File(old).delete() }
            imagePath = dest.absolutePath
        }
    }

    // Notification permission launcher (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showDateTimePicker(context) { ts -> reminderAt = ts }
    }

    LaunchedEffect(noteId) {
        if (noteId == 0L) return@LaunchedEffect
        val n = viewModel.notes.mapNotNull { list -> list.find { it.id == noteId } }.first()
        noteRef    = n
        title      = n.title
        color      = n.color
        noteType   = n.noteType()
        imagePath  = n.imagePath
        reminderAt = n.reminderAt
        when (noteType) {
            NoteType.LIST   -> listItems = parseListItems(n.content)
            NoteType.SECURE -> content   = ""
            else            -> content   = n.content
        }
    }

    fun buildContentToSave() = when (noteType) {
        NoteType.LIST -> buildListContent(listItems)
        else          -> content
    }

    fun saveAndBack() {
        val hasContent = title.isNotBlank() || content.isNotBlank() || listItems.isNotEmpty()
        scope.launch {
            if (hasContent) {
                val savedId = if (noteRef != null) {
                    viewModel.update(noteRef!!, title, buildContentToSave(), noteType, imagePath, reminderAt)
                    noteRef!!.id
                } else {
                    viewModel.saveAndReturn(title, buildContentToSave(), color, noteType, imagePath, reminderAt)
                }
                val currentIds  = noteLabels.map { it.id }.toSet()
                val selectedIds = selectedLabels.map { it.id }.toSet()
                (selectedIds - currentIds).forEach { labelViewModel.linkLabel(savedId, it) }
                (currentIds - selectedIds).forEach { labelViewModel.unlinkLabel(savedId, it) }
            }
            onBack()
        }
    }

    fun shareNote() {
        val text = buildString {
            if (title.isNotBlank()) append("$title\n\n")
            append(if (noteType == NoteType.LIST) buildListContent(listItems) else content)
        }
        context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text.trim()) },
            "Compartir nota"
        ))
    }

    // Label selection dialog
    if (showLabelDialog) {
        LabelPickerDialog(
            allLabels    = allLabels,
            selected     = selectedLabels,
            onToggle     = { label ->
                selectedLabels = if (label in selectedLabels) selectedLabels - label else selectedLabels + label
            },
            onCreateNew  = { name -> labelViewModel.createLabel(name) },
            onDismiss    = { showLabelDialog = false }
        )
    }

    val bg = remember(color) { NoteColors.getOrElse(color) { Color.White } }
    val onBg = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color(0xFFE6E1E5)
    val onBgMuted = onBg.copy(alpha = 0.5f)

    if (noteType == NoteType.SECURE && !isSecureUnlocked) {
        SecurePinOverlay(
            pinInput   = pinInput,
            error      = pinError,
            onPinDigit = { digit ->
                if (pinInput.length < 4) {
                    pinInput += digit; pinError = ""
                    if (pinInput.length == 4) {
                        if (lockViewModel.verifyPin(pinInput)) {
                            noteRef?.let { n -> content = viewModel.decryptContent(n.content) }
                            isSecureUnlocked = true
                        } else { pinError = "PIN incorrecto"; pinInput = "" }
                    }
                }
            },
            onDelete = { if (pinInput.isNotEmpty()) { pinInput = pinInput.dropLast(1); pinError = "" } },
            onBack   = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = { saveAndBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (noteType != NoteType.SECURE) {
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) { Icon(Icons.Default.Image, "Imagen") }
                        IconButton(onClick = { shareNote() }) { Icon(Icons.Default.Share, "Compartir") }
                    }
                    // Reminder button
                    IconButton(onClick = {
                        if (reminderAt != null) { reminderAt = null } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                showDateTimePicker(context) { ts -> reminderAt = ts }
                            }
                        }
                    }) {
                        Icon(
                            if (reminderAt != null) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            "Recordatorio",
                            tint = if (reminderAt != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    noteRef?.let { n ->
                        IconButton(onClick = { viewModel.softDelete(n); onBack() }) { Icon(Icons.Default.Delete, "Eliminar") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        },
        containerColor = bg
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp).imePadding()) {

            // Color selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(NoteColors) { i, c ->
                    Box(
                        Modifier.size(28.dp).clip(CircleShape).background(c)
                            .then(if (i == color) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            .clickable { color = i }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            // Type selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(NoteType.entries, key = { it.name }) { type ->
                    FilterChip(
                        selected = noteType == type,
                        onClick = {
                            if (noteType != type) {
                                when {
                                    type == NoteType.LIST    -> listItems = parseListItems(content)
                                    noteType == NoteType.LIST -> content = buildListContent(listItems)
                                    type == NoteType.SECURE  -> isSecureUnlocked = true
                                }
                                noteType = type
                            }
                        },
                        label = { Text("${type.emoji} ${type.label}", fontSize = 12.sp) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            // Label chips row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(selectedLabels.toList(), key = { it.id }) { label ->
                    InputChip(
                        selected = true,
                        onClick = { selectedLabels = selectedLabels - label },
                        label = { Text(label.name, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { showLabelDialog = true },
                        label = { Text("+ Etiqueta", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null, Modifier.size(14.dp)) }
                    )
                }
            }

            // Reminder chip
            val reminderFmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
            reminderAt?.let { ts ->
                Spacer(Modifier.height(4.dp))
                InputChip(
                    selected = true,
                    onClick = { reminderAt = null },
                    label = { Text("⏰ ${reminderFmt.format(Date(ts))}", fontSize = 11.sp) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                )
            }

            Spacer(Modifier.height(4.dp))

            // Image preview
            imagePath?.let { path ->
                Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(File(path)).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(onClick = { File(path).delete(); imagePath = null }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Title
            TextField(
                value = title, onValueChange = { title = it },
                placeholder = { Text("Título", style = MaterialTheme.typography.titleLarge, color = onBgMuted) },
                textStyle = MaterialTheme.typography.titleLarge.copy(color = onBg),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = onBg, unfocusedTextColor = onBg,
                    cursorColor = onBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            when (noteType) {
                NoteType.LIST   -> ListEditor(listItems, newItemText, { newItemText = it }, {
                    if (newItemText.isNotBlank()) { listItems = listItems + Pair(newItemText.trim(), false); newItemText = "" }
                }, { idx -> listItems = listItems.mapIndexed { i, item -> if (i == idx) item.copy(second = !item.second) else item } },
                    { idx -> listItems = listItems.filterIndexed { i, _ -> i != idx } })
                NoteType.SECURE -> TextEditor(content, { content = it }, "Contenido privado...", onBg, onBgMuted)
                else            -> TextEditor(content, { content = it }, "Escribe tu nota aquí...", onBg, onBgMuted)
            }
        }
    }
}

private fun showDateTimePicker(context: android.content.Context, onResult: (Long) -> Unit) {
    val cal = Calendar.getInstance()
    DatePickerDialog(context, { _, y, m, d ->
        cal.set(y, m, d)
        TimePickerDialog(context, { _, h, min ->
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            onResult(cal.timeInMillis)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
}

@Composable
private fun LabelPickerDialog(
    allLabels: List<Label>,
    selected: Set<Label>,
    onToggle: (Label) -> Unit,
    onCreateNew: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Etiquetas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allLabels.forEach { label ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggle(label) }) {
                        Checkbox(checked = label in selected, onCheckedChange = { onToggle(label) })
                        Text(label.name, modifier = Modifier.weight(1f))
                    }
                }
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Nueva etiqueta...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (newName.isNotBlank()) {
                            IconButton(onClick = { onCreateNew(newName.trim()); newName = "" }) {
                                Icon(Icons.Default.Add, "Crear")
                            }
                        }
                    }
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Listo") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextEditor(content: String, onContentChange: (String) -> Unit, placeholder: String, textColor: Color, hintColor: Color) {
    TextField(
        value = content, onValueChange = onContentChange,
        placeholder = { Text(placeholder, color = hintColor) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = textColor, unfocusedTextColor = textColor,
            cursorColor = textColor
        ),
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ListEditor(
    items: List<Pair<String, Boolean>>,
    newItemText: String,
    onNewItemChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
            @OptIn(ExperimentalMaterial3Api::class)
            TextField(
                value = newItemText, onValueChange = onNewItemChange,
                placeholder = { Text("Agregar elemento...", fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true, modifier = Modifier.weight(1f),
                trailingIcon = { if (newItemText.isNotBlank()) IconButton(onClick = onAddItem) { Icon(Icons.Default.Check, "Agregar") } }
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(Modifier.height(4.dp))
        val pending = remember(items) { items.mapIndexed { i, item -> Pair(i, item) }.filter { !it.second.second } }
        val done    = remember(items) { items.mapIndexed { i, item -> Pair(i, item) }.filter { it.second.second } }
        LazyColumn {
            items(pending, key = { (idx, _) -> idx }) { (idx, item) -> ListItemRow(item.first, false, { onToggle(idx) }, { onDelete(idx) }) }
            if (done.isNotEmpty()) {
                item { Text("Completados · ${done.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                items(done, key = { (idx, _) -> idx }) { (idx, item) -> ListItemRow(item.first, true, { onToggle(idx) }, { onDelete(idx) }) }
            }
        }
    }
}

@Composable
private fun ListItemRow(text: String, checked: Boolean, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Text(text, fontSize = 15.sp, modifier = Modifier.weight(1f),
            color = if (checked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None)
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun SecurePinOverlay(pinInput: String, error: String, onPinDigit: (String) -> Unit, onDelete: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Nota segura", style = MaterialTheme.typography.titleLarge)
        Text("Ingresa tu PIN para continuar", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (error.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { repeat(4) { i -> Box(Modifier.size(16.dp).clip(CircleShape).background(if (i < pinInput.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) } }
        Spacer(Modifier.height(32.dp))
        val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        for (row in 0..3) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (col in 0..2) {
                    val k = keys[row * 3 + col]
                    when {
                        k == "⌫" -> IconButton(onClick = onDelete, modifier = Modifier.size(64.dp)) { Icon(Icons.AutoMirrored.Filled.Backspace, null) }
                        k.isEmpty() -> Spacer(Modifier.size(64.dp))
                        else -> ElevatedButton(onClick = { onPinDigit(k) }, shape = CircleShape, modifier = Modifier.size(64.dp), contentPadding = PaddingValues(0.dp)) { Text(k, fontSize = 20.sp) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        TextButton(onClick = onBack) { Text("Cancelar") }
    }
}
