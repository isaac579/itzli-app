package com.eninetworks.notas.data.dao

import androidx.room.*
import com.eninetworks.notas.data.entity.Label
import com.eninetworks.notas.data.entity.NoteLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAll(): Flow<List<Label>>

    @Query("SELECT l.* FROM labels l INNER JOIN note_labels nl ON l.id = nl.labelId WHERE nl.noteId = :noteId")
    fun getLabelsForNote(noteId: Long): Flow<List<Label>>

    @Query("SELECT noteId FROM note_labels WHERE labelId = :labelId")
    fun getNoteIdsForLabel(labelId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: Label): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkNoteLabel(link: NoteLabel)

    @Delete
    suspend fun unlinkNoteLabel(link: NoteLabel)

    @Delete
    suspend fun deleteLabel(label: Label)
}
