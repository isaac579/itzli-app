package com.eninetworks.notas.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class Label(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "note_labels",
    primaryKeys = ["noteId", "labelId"],
    foreignKeys = [
        ForeignKey(entity = Note::class, parentColumns = ["id"], childColumns = ["noteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Label::class, parentColumns = ["id"], childColumns = ["labelId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("noteId"), Index("labelId")]
)
data class NoteLabel(
    val noteId: Long,
    val labelId: Long
)
