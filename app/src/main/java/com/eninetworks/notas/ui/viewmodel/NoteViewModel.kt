package com.eninetworks.notas.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eninetworks.notas.data.entity.Note
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.repository.NoteRepository
import com.eninetworks.notas.util.ReminderManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class NoteSort { DATE_DESC, DATE_ASC, TITLE_AZ, COLOR }

class NoteViewModel(private val repo: NoteRepository, app: Application) : AndroidViewModel(app) {

    private val _search = MutableStateFlow("")
    val searchQuery = _search.asStateFlow()

    private val _typeFilter = MutableStateFlow<NoteType?>(null)
    val typeFilter = _typeFilter.asStateFlow()

    private val _labelFilter = MutableStateFlow<Long?>(null)
    val labelFilter = _labelFilter.asStateFlow()

    private val _sort = MutableStateFlow(NoteSort.DATE_DESC)
    val sort = _sort.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val notes = combine(_search.debounce(300L), _typeFilter, _labelFilter) { q, t, l -> Triple(q, t, l) }
        .flatMapLatest { (query, type, labelId) ->
            when {
                labelId != null -> repo.getByLabel(labelId)
                type != null    -> repo.getByType(type)
                query.isNotBlank() -> repo.search(query)
                else            -> repo.getAll()
            }
        }
        .combine(_sort) { list, sortOrder ->
            val pinned = list.filter { it.type == NoteType.PINNED.name }
            val rest   = list.filter { it.type != NoteType.PINNED.name }
            val sorted = when (sortOrder) {
                NoteSort.DATE_DESC -> rest.sortedByDescending { it.updatedAt }
                NoteSort.DATE_ASC  -> rest.sortedBy { it.updatedAt }
                NoteSort.TITLE_AZ  -> rest.sortedBy { it.title.lowercase() }
                NoteSort.COLOR     -> rest.sortedBy { it.color }
            }
            pinned + sorted
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashed: StateFlow<List<Note>> = repo.getDeleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(q: String) { _search.value = q; _typeFilter.value = null; _labelFilter.value = null }
    fun setTypeFilter(type: NoteType?) { _typeFilter.value = type; _search.value = ""; _labelFilter.value = null }
    fun setLabelFilter(labelId: Long?) { _labelFilter.value = labelId; _search.value = ""; _typeFilter.value = null }
    fun setSort(sort: NoteSort) { _sort.value = sort }

    fun save(title: String, content: String, color: Int = 0, type: NoteType = NoteType.TEXT,
             imagePath: String? = null, reminderAt: Long? = null) =
        viewModelScope.launch {
            val id = repo.save(Note(title = title, content = content, color = color,
                type = type.name, imagePath = imagePath, reminderAt = reminderAt))
            reminderAt?.let { ReminderManager.schedule(getApplication(), id, title, it) }
        }

    suspend fun saveAndReturn(title: String, content: String, color: Int, type: NoteType,
                              imagePath: String?, reminderAt: Long?): Long {
        val id = repo.save(Note(title = title, content = content, color = color,
            type = type.name, imagePath = imagePath, reminderAt = reminderAt))
        reminderAt?.let { ReminderManager.schedule(getApplication(), id, title, it) }
        return id
    }

    fun update(note: Note, title: String, content: String, type: NoteType,
               imagePath: String? = note.imagePath, reminderAt: Long? = note.reminderAt) =
        viewModelScope.launch {
            repo.update(note.copy(title = title, content = content, type = type.name,
                updatedAt = System.currentTimeMillis(), imagePath = imagePath, reminderAt = reminderAt))
            when {
                reminderAt != null -> ReminderManager.schedule(getApplication(), note.id, title, reminderAt)
                note.reminderAt != null -> ReminderManager.cancel(getApplication(), note.id)
            }
        }

    fun softDelete(note: Note) = viewModelScope.launch {
        note.reminderAt?.let { ReminderManager.cancel(getApplication(), note.id) }
        repo.softDelete(note)
    }

    fun restore(note: Note) = viewModelScope.launch { repo.restore(note) }

    fun emptyTrash() = viewModelScope.launch {
        repo.emptyTrash(getApplication<Application>().filesDir.resolve("note_images"))
    }

    fun deletePermanently(note: Note) = viewModelScope.launch {
        note.reminderAt?.let { ReminderManager.cancel(getApplication(), note.id) }
        repo.deletePermanently(note)
    }

    fun delete(note: Note) = softDelete(note)

    fun decryptContent(content: String): String = repo.decryptContent(content)

    class Factory(private val repo: NoteRepository, private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>) = NoteViewModel(repo, app) as T
    }
}
