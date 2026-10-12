package com.eninetworks.notas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklists")
data class CheckList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
