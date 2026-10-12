package com.eninetworks.notas.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_items",
    foreignKeys = [ForeignKey(
        entity = CheckList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("listId")]
)
data class CheckListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val text: String,
    val isChecked: Boolean = false,
    val itemOrder: Int = 0
)
