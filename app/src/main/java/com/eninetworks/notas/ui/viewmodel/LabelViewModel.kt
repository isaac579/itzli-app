package com.eninetworks.notas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eninetworks.notas.data.entity.Label
import com.eninetworks.notas.data.repository.LabelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LabelViewModel(private val repo: LabelRepository) : ViewModel() {
    val labels: StateFlow<List<Label>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getLabelsForNote(noteId: Long): Flow<List<Label>> = repo.getLabelsForNote(noteId)

    fun createLabel(name: String) = viewModelScope.launch { repo.createLabel(name) }

    fun linkLabel(noteId: Long, labelId: Long) = viewModelScope.launch { repo.linkLabel(noteId, labelId) }

    fun unlinkLabel(noteId: Long, labelId: Long) = viewModelScope.launch { repo.unlinkLabel(noteId, labelId) }

    fun deleteLabel(label: Label) = viewModelScope.launch { repo.deleteLabel(label) }

    class Factory(private val repo: LabelRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = LabelViewModel(repo) as T
    }
}
