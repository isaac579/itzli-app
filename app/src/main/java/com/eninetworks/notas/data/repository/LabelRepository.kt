package com.eninetworks.notas.data.repository

import com.eninetworks.notas.data.dao.LabelDao
import com.eninetworks.notas.data.entity.Label
import com.eninetworks.notas.data.entity.NoteLabel
import kotlinx.coroutines.flow.Flow

class LabelRepository(private val dao: LabelDao) {
    fun getAll(): Flow<List<Label>> = dao.getAll()
    fun getLabelsForNote(noteId: Long): Flow<List<Label>> = dao.getLabelsForNote(noteId)
    fun getNoteIdsForLabel(labelId: Long): Flow<List<Long>> = dao.getNoteIdsForLabel(labelId)
    suspend fun createLabel(name: String): Long = dao.insertLabel(Label(name = name))
    suspend fun linkLabel(noteId: Long, labelId: Long) = dao.linkNoteLabel(NoteLabel(noteId, labelId))
    suspend fun unlinkLabel(noteId: Long, labelId: Long) = dao.unlinkNoteLabel(NoteLabel(noteId, labelId))
    suspend fun deleteLabel(label: Label) = dao.deleteLabel(label)
}
