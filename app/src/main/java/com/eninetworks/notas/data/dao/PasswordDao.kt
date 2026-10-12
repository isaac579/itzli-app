package com.eninetworks.notas.data.dao

import androidx.room.*
import com.eninetworks.notas.data.entity.Password
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAll(): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE category = :category ORDER BY title ASC")
    fun getByCategory(category: String): Flow<List<Password>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(password: Password): Long

    @Update
    suspend fun update(password: Password)

    @Delete
    suspend fun delete(password: Password)
}
