package com.eninetworks.notas.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eninetworks.notas.data.dao.CheckListDao
import com.eninetworks.notas.data.dao.LabelDao
import com.eninetworks.notas.data.dao.NoteDao
import com.eninetworks.notas.data.dao.PasswordDao
import com.eninetworks.notas.data.entity.CheckList
import com.eninetworks.notas.data.entity.CheckListItem
import com.eninetworks.notas.data.entity.Label
import com.eninetworks.notas.data.entity.Note
import com.eninetworks.notas.data.entity.NoteLabel
import com.eninetworks.notas.data.entity.Password

@Database(
    entities = [Note::class, CheckList::class, CheckListItem::class, Password::class, Label::class, NoteLabel::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun checkListDao(): CheckListDao
    abstract fun passwordDao(): PasswordDao
    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN type TEXT NOT NULL DEFAULT 'TEXT'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN imagePath TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS labels (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS note_labels (
                        noteId INTEGER NOT NULL,
                        labelId INTEGER NOT NULL,
                        PRIMARY KEY(noteId, labelId),
                        FOREIGN KEY(noteId) REFERENCES notes(id) ON DELETE CASCADE,
                        FOREIGN KEY(labelId) REFERENCES labels(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_labels_noteId ON note_labels(noteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_labels_labelId ON note_labels(labelId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderAt INTEGER")
                db.execSQL("ALTER TABLE passwords ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN itemOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "notas_db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
    }
}
