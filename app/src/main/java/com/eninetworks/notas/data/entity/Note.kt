package com.eninetworks.notas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val color: Int = 0,
    val type: String = NoteType.TEXT.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val deletedAt: Long? = null,
    val reminderAt: Long? = null
)
