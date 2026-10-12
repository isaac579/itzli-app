package com.eninetworks.notas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eninetworks.notas.data.dao.ListItemCount
import com.eninetworks.notas.data.entity.CheckList
import com.eninetworks.notas.data.entity.CheckListItem
import com.eninetworks.notas.data.repository.CheckListRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ListWithCount(val list: CheckList, val total: Int, val pending: Int)

class CheckListViewModel(private val repo: CheckListRepository) : ViewModel() {
    val lists = repo.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listsWithCounts: StateFlow<List<ListWithCount>> = combine(
        repo.getAllLists(),
        repo.getItemCounts()
    ) { lists, counts ->
        val countMap = counts.associateBy { it.listId }
        lists.map { list ->
            val c = countMap[list.id]
            ListWithCount(list, c?.total ?: 0, c?.pending ?: 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val itemFlows = mutableMapOf<Long, StateFlow<List<CheckListItem>>>()

    fun getItemsForList(listId: Long): StateFlow<List<CheckListItem>> =
        itemFlows.getOrPut(listId) {
            repo.getItemsForList(listId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun createList(title: String) = viewModelScope.launch { repo.saveList(CheckList(title = title)) }
    fun addItem(listId: Long, text: String, order: Int = 0) = viewModelScope.launch { repo.saveItem(CheckListItem(listId = listId, text = text, itemOrder = order)) }
    fun reorderItems(items: List<CheckListItem>) = viewModelScope.launch { repo.reorderItems(items) }
    fun toggleItem(item: CheckListItem) = viewModelScope.launch { repo.updateItem(item.copy(isChecked = !item.isChecked)) }
    fun deleteItem(item: CheckListItem) = viewModelScope.launch { repo.deleteItem(item) }
    fun deleteList(list: CheckList) = viewModelScope.launch { repo.deleteList(list) }

    class Factory(private val repo: CheckListRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = CheckListViewModel(repo) as T
    }
}
