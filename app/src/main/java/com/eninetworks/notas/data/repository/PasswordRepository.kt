package com.eninetworks.notas.data.repository

import com.eninetworks.notas.data.dao.PasswordDao
import com.eninetworks.notas.data.entity.Password
import com.eninetworks.notas.security.CryptoManager
import kotlinx.coroutines.flow.Flow

class PasswordRepository(private val dao: PasswordDao) {
    fun getAll(): Flow<List<Password>> = dao.getAll()
    fun getByCategory(category: String): Flow<List<Password>> = dao.getByCategory(category)

    suspend fun save(password: Password): Long =
        dao.insert(password.copy(encryptedPassword = CryptoManager.encrypt(password.encryptedPassword)))

    suspend fun update(password: Password) =
        dao.update(password.copy(encryptedPassword = CryptoManager.encrypt(password.encryptedPassword)))

    fun decrypt(encryptedPassword: String): String = CryptoManager.decrypt(encryptedPassword)

    suspend fun delete(password: Password) = dao.delete(password)
}
