package com.eninetworks.notas.data.dao

import androidx.room.*
import com.eninetworks.notas.data.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY CASE WHEN type = 'PINNED' THEN 0 ELSE 1 END, updatedAt DESC")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND type = :type ORDER BY updatedAt DESC")
    fun getByType(type: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun search(query: String): Flow<List<Note>>

    @Query("""
        SELECT n.* FROM notes n
        INNER JOIN note_labels nl ON n.id = nl.noteId
        WHERE nl.labelId = :labelId AND n.deletedAt IS NULL
        ORDER BY CASE WHEN n.type = 'PINNED' THEN 0 ELSE 1 END, n.updatedAt DESC
    """)
    fun getByLabel(labelId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getDeleted(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE reminderAt IS NOT NULL AND reminderAt > :since AND deletedAt IS NULL")
    suspend fun getNotesWithReminder(since: Long): List<Note>

    @Query("UPDATE notes SET deletedAt = :time WHERE id = :noteId")
    suspend fun softDelete(noteId: Long, time: Long)

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :noteId")
    suspend fun restore(noteId: Long)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrash()

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeOldTrash(cutoff: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
