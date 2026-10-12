package com.eninetworks.notas.data.repository

import com.eninetworks.notas.data.dao.NoteDao
import com.eninetworks.notas.data.entity.Note
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.entity.noteType
import com.eninetworks.notas.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class NoteRepository(private val dao: NoteDao) {
    fun getAll(): Flow<List<Note>> = dao.getAll()
    fun getByType(type: NoteType): Flow<List<Note>> = dao.getByType(type.name)
    fun search(query: String): Flow<List<Note>> = dao.search(query)
    fun getByLabel(labelId: Long): Flow<List<Note>> = dao.getByLabel(labelId)
    fun getDeleted(): Flow<List<Note>> = dao.getDeleted()
    suspend fun getNotesWithReminder(since: Long): List<Note> = dao.getNotesWithReminder(since)

    suspend fun save(note: Note): Long {
        val toSave = if (note.noteType() == NoteType.SECURE && note.content.isNotBlank())
            note.copy(content = CryptoManager.encrypt(note.content))
        else note
        return dao.insert(toSave)
    }

    suspend fun update(note: Note) {
        val toUpdate = if (note.noteType() == NoteType.SECURE && note.content.isNotBlank())
            note.copy(content = CryptoManager.encrypt(note.content))
        else note
        dao.update(toUpdate)
    }

    suspend fun softDelete(note: Note) = dao.softDelete(note.id, System.currentTimeMillis())

    suspend fun restore(note: Note) = dao.restore(note.id)

    suspend fun emptyTrash(notesDir: File?) {
        dao.getDeleted().first().forEach { note ->
            note.imagePath?.let { path -> File(path).delete() }
        }
        dao.emptyTrash()
    }

    suspend fun deletePermanently(note: Note) {
        note.imagePath?.let { path -> File(path).delete() }
        dao.delete(note)
    }

    suspend fun purgeOldTrash(daysOld: Int = 30) {
        val cutoff = System.currentTimeMillis() - daysOld * 24L * 60 * 60 * 1000
        dao.purgeOldTrash(cutoff)
    }

    fun decryptContent(content: String): String = try {
        CryptoManager.decrypt(content)
    } catch (e: Exception) {
        content
    }

    suspend fun delete(note: Note) = dao.delete(note)
}
