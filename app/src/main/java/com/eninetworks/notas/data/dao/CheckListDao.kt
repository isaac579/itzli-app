package com.eninetworks.notas.data.dao

import androidx.room.*
import com.eninetworks.notas.data.entity.CheckList
import com.eninetworks.notas.data.entity.CheckListItem
import kotlinx.coroutines.flow.Flow

data class ListItemCount(val listId: Long, val total: Int, val pending: Int)

@Dao
interface CheckListDao {
    @Query("SELECT * FROM checklists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<CheckList>>

    @Query("SELECT * FROM checklist_items WHERE listId = :listId ORDER BY itemOrder ASC, id ASC")
    fun getItemsForList(listId: Long): Flow<List<CheckListItem>>

    @Query("""
        SELECT listId,
               COUNT(*) AS total,
               SUM(CASE WHEN isChecked = 0 THEN 1 ELSE 0 END) AS pending
        FROM checklist_items
        GROUP BY listId
    """)
    fun getItemCounts(): Flow<List<ListItemCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: CheckList): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CheckListItem): Long

    @Update
    suspend fun updateList(list: CheckList)

    @Update
    suspend fun updateItem(item: CheckListItem)

    @Delete
    suspend fun deleteList(list: CheckList)

    @Delete
    suspend fun deleteItem(item: CheckListItem)
}
