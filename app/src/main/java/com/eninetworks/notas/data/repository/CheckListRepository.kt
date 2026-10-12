package com.eninetworks.notas.data.repository

import com.eninetworks.notas.data.dao.CheckListDao
import com.eninetworks.notas.data.dao.ListItemCount
import com.eninetworks.notas.data.entity.CheckList
import com.eninetworks.notas.data.entity.CheckListItem
import kotlinx.coroutines.flow.Flow

class CheckListRepository(private val dao: CheckListDao) {
    fun getAllLists(): Flow<List<CheckList>> = dao.getAllLists()
    fun getItemsForList(listId: Long): Flow<List<CheckListItem>> = dao.getItemsForList(listId)
    fun getItemCounts(): Flow<List<ListItemCount>> = dao.getItemCounts()
    suspend fun saveList(list: CheckList): Long = dao.insertList(list)
    suspend fun saveItem(item: CheckListItem): Long = dao.insertItem(item)
    suspend fun updateList(list: CheckList) = dao.updateList(list)
    suspend fun updateItem(item: CheckListItem) = dao.updateItem(item)
    suspend fun reorderItems(items: List<CheckListItem>) {
        items.forEachIndexed { index, item ->
            if (item.itemOrder != index) dao.updateItem(item.copy(itemOrder = index))
        }
    }
    suspend fun deleteList(list: CheckList) = dao.deleteList(list)
    suspend fun deleteItem(item: CheckListItem) = dao.deleteItem(item)
}
